package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.Difficulty;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.audio.Effectmusic;
import de.tum.cit.aet.valleyday.map.Items.Fertilizer;
import de.tum.cit.aet.valleyday.map.Items.Item;
import de.tum.cit.aet.valleyday.map.Items.Shovel;
import de.tum.cit.aet.valleyday.map.Items.WateringCan;
import de.tum.cit.aet.valleyday.map.crops.CropTile;
import de.tum.cit.aet.valleyday.map.player.Player;
import de.tum.cit.aet.valleyday.map.player.WildlifeVisitor;
import de.tum.cit.aet.valleyday.map.structures.Entrance;
import de.tum.cit.aet.valleyday.map.structures.Exit;
import de.tum.cit.aet.valleyday.map.terrain.Debris;
import de.tum.cit.aet.valleyday.map.terrain.Fence;
import de.tum.cit.aet.valleyday.map.terrain.SoilType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;


/**
 * The class is responsible for:
 * loading the map layout
 * owning tiles, crops, and terrain objects
 * owning the physics World
 * owning the Player
 * advancing world simulation (physics + crops)
 * answering questions like “is this blocked?”, “is this an exit?”
 */
public class GameMap {

    static {
        com.badlogic.gdx.physics.box2d.Box2D.init();
    }

    private static final float TIME_STEP = 1f / Gdx.graphics.getDisplayMode().refreshRate;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private float physicsTime = 0;

    private final ValleyDayGame game;
    private final World world;

    private final Player player;

    private Tile[][] tiles;
    private int mapWidth;
    private int mapHeight;

    private CropTile[][] crops;
    private int harvested = 0;
    private final int quota;
    private static final float PATH_CHANCE = 0.32f; // 32%


    private final List<WildlifeVisitor> wildlife = new ArrayList<>();
    private final int maxWildlife;

    private float wildlifeRespawnTimer = 0f;
    private static final float WILDLIFE_RESPAWN_COOLDOWN = 10.0f;

    // -------------------------
    // SCARED 
    // -------------------------
    private boolean scared = false;
    private float fleeDirX = 0f;
    private float fleeDirY = 0f;
    private static final float FLEE_SPEED = 6.0f;

    // end state flags
    private boolean gameWon = false;
    private boolean lostWildlife = false;
    private final float wildlifeSpeedMultiplier;


    public GameMap(ValleyDayGame game, String mapPath) {
        this.game = game;
        this.world = new World(Vector2.Zero, true);



        Difficulty diff = game.getDifficulty();
        this.quota = diff.exitQuota;
        this.maxWildlife = diff.maxWildlife;
        this.wildlifeSpeedMultiplier = diff.speedMultiplier;

        loadMap(mapPath);

        int[] entrance = findEntrancePosition();
        this.player = new Player(this.world, this, entrance[0], entrance[1]);
    }


    // ---------------------------------
    // MAIN TICK
    // ---------------------------------
    public void tick(float frameTime) {
        // 1) wildlife moves
        tickWildlife(frameTime);

        // 2) check collision with wildlife (tile-based)
        checkTouchWildlife();

        // 3) apply scared movement (force-run toward nearest border)
        if (scared && !lostWildlife) {
            player.setScared(true);
            player.setForcedVelocity(fleeDirX * FLEE_SPEED, fleeDirY * FLEE_SPEED);
        }

        // 4) player tick
        player.tick(frameTime);

        // 5) disable interactions when scared
        if (!scared && !lostWildlife) {
            handleAKey();
            handleSKey(frameTime);
            checkItemPickup();
        }

        // 6) crops + physics
        tickCrops(frameTime);
        doPhysicsStep(frameTime);

        // 7) out of map => lose
        if (scared && !lostWildlife) {
            float px = player.getX();
            float py = player.getY();
            if (px < 0f || py < 0f || px >= mapWidth || py >= mapHeight) {
                lostWildlife = true;
            }
        }
    }

    // ---------------------------------
    // SCARED: collision trigger
    // ---------------------------------
    private void checkTouchWildlife() {
        if (lostWildlife || scared) {
            return;
        }

        int px = worldToTile(player.getX());
        int py = worldToTile(player.getY());

        for (WildlifeVisitor w : wildlife) {
            if (!w.isAlive()) {
                continue;
            }

            // Your wildlife uses tile coordinates (you also compare in handleSKey)
            if (w.getX() == px && w.getY() == py) {
                scared = true;
                computeFleeDirectionToNearestBorder();
                return;
            }
        }
    }

    private void computeFleeDirectionToNearestBorder() {
        float px = player.getX();
        float py = player.getY();

        float toLeft = px;
        float toRight = (mapWidth - 1) - px;
        float toBottom = py;
        float toTop = (mapHeight - 1) - py;

        if (toLeft <= toRight && toLeft <= toBottom && toLeft <= toTop) {
            fleeDirX = -1f;
            fleeDirY = 0f;
        } else if (toRight <= toLeft && toRight <= toBottom && toRight <= toTop) {
            fleeDirX = 1f;
            fleeDirY = 0f;
        } else if (toBottom <= toLeft && toBottom <= toRight && toBottom <= toTop) {
            fleeDirX = 0f;
            fleeDirY = -1f;
        } else {
            fleeDirX = 0f;
            fleeDirY = 1f;
        }
    }

    // ---------------------------------
    // MAP LOADING
    // ---------------------------------
    private void loadMap(String path) {
        Properties props = new Properties();
        try (InputStream in = createMapInputStream(path)) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load map file: " + path, e);
        }


        int maxX = 0;
        int maxY = 0;

        for (String key : props.stringPropertyNames()) {
            if (!key.contains(",")) continue;
            String[] parts = key.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }

        mapWidth = maxX + 1;
        mapHeight = maxY + 1;

        tiles = new Tile[mapWidth][mapHeight];

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                tiles[x][y] = new Tile(null);
            }
        }

        for (String key : props.stringPropertyNames()) {
            if (!key.contains(",")) continue;

            String[] parts = key.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int value = Integer.parseInt(props.getProperty(key));

            if (value == 3) {
                if (wildlife.size() < maxWildlife) {
                    wildlife.add(new WildlifeVisitor(x, y, wildlifeSpeedMultiplier));
                }
                tiles[x][y] = new Tile(null);
            } else {
                tiles[x][y] = createTileFromValue(value, x, y);
            }
        }

        boolean exitFound = false;
        ArrayList<int[]> debrisPositions = new ArrayList<>();

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                Tile tile = tiles[x][y];
                if (tile.getObject() instanceof Exit) {
                    exitFound = true;
                }
                if (tile.getObject() instanceof Debris) {
                    debrisPositions.add(new int[]{x, y});
                }
            }
        }

        if (!exitFound && !debrisPositions.isEmpty()) {
            int[] pos = debrisPositions.get(MathUtils.random(debrisPositions.size() - 1));
            Tile tile = tiles[pos[0]][pos[1]];
            if (tile.getObject() instanceof Debris) {
                tile.setHiddenObject(new Exit(pos[0], pos[1]));
            }
        }

        crops = new CropTile[mapWidth][mapHeight];

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                Tile tile = tiles[x][y];
                if (tile.getObject() == null) {
                    CropTile ct = new CropTile();
                    tile.setCrop(ct);
                    crops[x][y] = ct;
                } else {
                    tile.setCrop(null);
                    crops[x][y] = null;
                }
            }
        }


        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {

                Tile tile = tiles[x][y];

                // only on empty grass tiles
                if (tile.getObject() != null) continue;
                if (tile.getSoilType() != SoilType.FARMLAND) continue;

                if (Math.random() < PATH_CHANCE) {
                    tile.setSoilType(SoilType.NON_FARMLAND);
                }
            }
        }

    }

    private int[] findEntrancePosition() {
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (tiles[x][y].getObject() instanceof Entrance) {
                    return new int[]{x, y};
                }
            }
        }
        throw new IllegalStateException("No entrance found in map!");
    }

    private Tile createTileFromValue(int value, int x, int y) {
        return switch (value) {
            case 0 -> new Tile(new Fence(x, y));
            case 1 -> {
                Tile t = new Tile(new Debris(x, y));
                t.setSoilType(SoilType.NON_FARMLAND);
                yield t;
            }
            case 2 -> { // Entrance
                Tile t = new Tile(new Entrance(x, y));
                t.setSoilType(SoilType.NON_FARMLAND);
                yield t;
            }
            case 4 -> { // Debris hiding Exit
                Tile t = new Tile(new Debris(x, y));
                t.setHiddenObject(new Exit(x, y));
                t.setSoilType(SoilType.NON_FARMLAND);
                yield t;
            }
            case 5 -> { // Debris hiding Fertilizer
                Tile t = new Tile(new Debris(x, y));
                t.setHiddenObject(new Fertilizer(x, y));
                t.setSoilType(SoilType.NON_FARMLAND);
                yield t;
            }
            case 6 -> { // Debris hiding WateringCan
                Tile t = new Tile(new Debris(x, y));
                t.setHiddenObject(new WateringCan(x, y));
                t.setSoilType(SoilType.NON_FARMLAND);
                yield t;
            }
            case 7 -> { // Debris hiding Shovel
                Tile t = new Tile(new Debris(x, y));
                t.setHiddenObject(new Shovel(x, y));
                t.setSoilType(SoilType.NON_FARMLAND);
                yield t;
            }

            default -> new Tile(null);
        };
    }

    // ---------------------------------
    // INTERACT WITH TILE (D)
    // ---------------------------------
   public void interactWithTile(int x, int y) {
    if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
        return;
    }

    Tile tile = tiles[x][y];

    if (tile.getObject() == null) {
        return;
    }

    if (!tile.getObject().isDestructible()) {
        return;
    }

   
    boolean wasDebris =
            tile.getObject() instanceof de.tum.cit.aet.valleyday.map.terrain.Debris;

   
    tile.interact();

    
    boolean isDebrisNow =
            tile.getObject() instanceof de.tum.cit.aet.valleyday.map.terrain.Debris;

    if (wasDebris && !isDebrisNow) {
        Effectmusic.DebrisDestory.play();
    }
}


    private void checkItemPickup() {
        int px = worldToTile(player.getX());
        int py = worldToTile(player.getY());

        if (px < 0 || py < 0 || px >= mapWidth || py >= mapHeight) {
            return;
        }

        Tile tile = tiles[px][py];

        if (tile.getObject() instanceof Item item) {
            item.onPickup(this);
            tile.clearObject();
            Effectmusic.CollectItem.play();
        }
    }

    // ---------------------------------
    // A KEY (PLANT/HARVEST/CLEAR ROTTEN)
    // ---------------------------------
    private void handleAKey() {
        if (!Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            return;
        }

        int x = worldToTile(player.getX());
        int y = worldToTile(player.getY());

        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return;
        }

        Tile tile = tiles[x][y];

        // ❌ cannot interact if object is present
        if (tile.getObject() != null) {
            return;
        }

        // ❌ cannot plant on non-farmland (paths, entrance ground, debris ground)
        if (tile.getSoilType() != SoilType.FARMLAND) {
            return;
        }

        CropTile crop = crops[x][y];
        if (crop == null) {
            return;
        }

        if (crop.isRotten()) {
            crop.clearToEmpty();
            return;
        }

        if (crop.isEmpty()) {
        crop.plant();
        Effectmusic.Plant.play();
    return;
}


        if (crop.isMature()) {
    crop.harvest();
    harvested += 1;
    Effectmusic.Harvest.play();
}

    }


    // ---------------------------------
    // S KEY (SHOO)
    // ---------------------------------
    private float shooCooldown = 0f;
    private static final float SHOO_COOLDOWN = 0.4f;

  private void handleSKey(float dt) {
    // cooldown tick
    if (shooCooldown > 0f) {
        shooCooldown -= dt;
        if (shooCooldown < 0f) {
            shooCooldown = 0f;
        }
    }

    if (!Gdx.input.isKeyJustPressed(Input.Keys.S)) {
        return;
    }

    if (shooCooldown > 0f) {
        return;
    }

   
    int[] front = getFrontTile(player);
    int fx = front[0];
    int fy = front[1];

   
    if (fx < 0 || fy < 0 || fx >= mapWidth || fy >= mapHeight) {
        return;
    }

    
    if (isBlocked(fx, fy)) {
        return;
    }

    
    int px = worldToTile(player.getX());
    int py = worldToTile(player.getY());

    
    for (WildlifeVisitor w : wildlife) {
        if (!w.isAlive()) {
            continue;
        }

        
        int wx = (int) Math.floor(w.getRenderX() + 0.5f);
        int wy = (int) Math.floor(w.getRenderY() + 0.5f);

        if (wx == fx && wy == fy) {
            w.startFleeFrom(px, py);
             Effectmusic.Hit.play();
            shooCooldown = SHOO_COOLDOWN;
            return;
        }
    }
}


    // ---------------------------------
    // CROPS/WILDLIFE TICK
    // ---------------------------------
    private void tickCrops(float dt) {
        if (crops == null) {
            return;
        }
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (crops[x][y] != null) {
                    crops[x][y].tick(dt);
                }
            }
        }
    }

    private void tickWildlife(float dt) {
    // 1) tick all
    for (WildlifeVisitor w : wildlife) {
        w.tick(dt, this);
    }

    // 2) remove dead
    int before = wildlife.size();
    wildlife.removeIf(w -> !w.isAlive());
    int after = wildlife.size();

    // 3) if removed, start cooldown
    if (after < before) {
        wildlifeRespawnTimer = WILDLIFE_RESPAWN_COOLDOWN;
    }

    // 4) timer tick
    if (wildlifeRespawnTimer > 0f) {
        wildlifeRespawnTimer -= dt;
        if (wildlifeRespawnTimer < 0f) {
            wildlifeRespawnTimer = 0f;
        }
    }

    // 5) respawn (补到 MAX_WILDLIFE)
    if (wildlifeRespawnTimer == 0f) {
        while (wildlife.size() < maxWildlife) {
            if (!spawnOneWildlifeRandomly()) {
                break;
            }
            // 连刷多只时，给一点点间隔避免瞬间刷满很突兀
            wildlifeRespawnTimer = 0.2f;
        }
    }
}


    // ---------------------------------
    // PHYSICS
    // ---------------------------------
    private void doPhysicsStep(float frameTime) {
        this.physicsTime += frameTime;
        while (this.physicsTime >= TIME_STEP) {
            this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            this.physicsTime -= TIME_STEP;
        }
    }

    // ---------------------------------
    // HELPERS / GETTERS
    // ---------------------------------
    public boolean isBlocked(int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return true;
        }
        return tiles[x][y].isBlocked();
    }

    public boolean isExit(int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return false;
        }
        return tiles[x][y].getObject() instanceof Exit;
    }

    public boolean hasPlayerReachedExit() {
        if (gameWon) return true;

        int[] tile = worldToTile(player.getX(), player.getY());
        int px = tile[0];
        int py = tile[1];

        if (isExit(px, py) && isExitUnlocked()) {
            gameWon = true;
            return true;
        }

        return false;
    }

    public boolean isExitUnlocked() {
        return harvested >= quota;
    }

    public boolean isLostByWildlife() {
        return lostWildlife;
    }

    /**
     * IMPORTANT:
     * WildlifeVisitor may still call map.loseByWildlife().
     * We DO NOT want instant game over. We want "run out of map then lose".
     * So we convert that call into "trigger scared".
     */
    public void loseByWildlife() {
        // if already losing, ignore
        if (lostWildlife) {
            return;
        }
        // if not yet scared, start scared and compute direction
        if (!scared) {
            scared = true;
            Effectmusic.OhNo.play();
            computeFleeDirectionToNearestBorder();
            return;
        }
        // If already scared, we still don't lose instantly.
        // Actual loss happens only when player goes out of map (in tick()).
    }

    public Player getPlayer() {
        return player;
    }

    public List<WildlifeVisitor> getWildlife() {
        return wildlife;
    }

    public CropTile[][] getCrops() {
        return crops;
    }

    public CropTile getCropAt(int x, int y) {
        if (crops == null) {
            return null;
        }
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return null;
        }
        return crops[x][y];
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int worldToTile(float value) {
        return (int) Math.floor(value);
    }

    public int[] worldToTile(float worldX, float worldY) {
        return new int[]{
                worldToTile(worldX),
                worldToTile(worldY)
        };
    }

    public int[] getFrontTile(Player player) {
        int[] tile = worldToTile(player.getX(), player.getY());
        int tx = tile[0];
        int ty = tile[1];

        switch (player.getFacing()) {
            case UP -> ty += 1;
            case DOWN -> ty -= 1;
            case LEFT -> tx -= 1;
            case RIGHT -> tx += 1;
        }

        return new int[]{tx, ty};
    }

    public int getHarvestedCount() {
        return harvested;
    }

    public int getQuota() {
        return quota;
    }

    // ---------------------------------
    // You had these methods; kept for compatibility
    // ---------------------------------
private static final int MIN_SPAWN_DISTANCE = 4; 

private boolean spawnOneWildlifeRandomly() {
    int px = worldToTile(player.getX());
    int py = worldToTile(player.getY());

    for (int tries = 0; tries < 80; tries++) {
        int x = MathUtils.random(0, mapWidth - 1);
        int y = MathUtils.random(0, mapHeight - 1);

        if (isBlocked(x, y)) {
            continue;
        }

        // ✅ 不在玩家周围4格（方形区域：9x9）生成
        int dx = Math.abs(x - px);
        int dy = Math.abs(y - py);
        if (dx <= MIN_SPAWN_DISTANCE && dy <= MIN_SPAWN_DISTANCE) {
            continue;
        }

        boolean occupied = false;
        for (WildlifeVisitor w : wildlife) {
            if (w.isAlive() && w.getX() == x && w.getY() == y) {
                occupied = true;
                break;
            }
        }
        if (occupied) {
            continue;
        }

        wildlife.add(new WildlifeVisitor(x, y, wildlifeSpeedMultiplier));
        return true;
    }

    return false;
}



    public void advanceAllCrops() {
        if (crops == null) return;
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (crops[x][y] != null) {
                    crops[x][y].advanceStage();
                }
            }
        }
    }

    public void restoreAllCrops() {
        if (crops == null) return;
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (crops[x][y] != null) {
                    crops[x][y].restoreIfRotted();
                }
            }
        }
    }

    public void applyWateringCan() {
        if (crops == null) return;
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                CropTile c = crops[x][y];
                if (c != null) {
                    c.restoreIfRotted();
                    c.resetRotTimer(60f);
                }
            }
        }
    }

    // Pathfinding helpers you already have (unchanged)
    public int[] findNearestMatureCrop(int sx, int sy, int maxManhattanDist) {
        if (crops == null) {
            return null;
        }

        int bestDist = Integer.MAX_VALUE;
        int bestX = -1;
        int bestY = -1;

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                CropTile c = crops[x][y];
                if (c == null) continue;
                if (c.getStage() != de.tum.cit.aet.valleyday.map.crops.CropStage.MATURE) continue;

                int d = Math.abs(x - sx) + Math.abs(y - sy);
                if (d <= maxManhattanDist && d < bestDist) {
                    bestDist = d;
                    bestX = x;
                    bestY = y;
                }
            }
        }

        if (bestDist == Integer.MAX_VALUE) {
            return null;
        }
        return new int[]{bestX, bestY};
    }

    public int[] nextStepBfs(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) {
            return new int[]{sx, sy};
        }

        boolean[][] visited = new boolean[mapWidth][mapHeight];
        int[][] prevX = new int[mapWidth][mapHeight];
        int[][] prevY = new int[mapWidth][mapHeight];

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                prevX[x][y] = -1;
                prevY[x][y] = -1;
            }
        }

        int[] qx = new int[mapWidth * mapHeight];
        int[] qy = new int[mapWidth * mapHeight];
        int head = 0;
        int tail = 0;

        visited[sx][sy] = true;
        qx[tail] = sx;
        qy[tail] = sy;
        tail++;

        int[][] dirs = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (head < tail) {
            int x = qx[head];
            int y = qy[head];
            head++;

            for (int[] d : dirs) {
                int nx = x + d[0];
                int ny = y + d[1];

                if (nx < 0 || ny < 0 || nx >= mapWidth || ny >= mapHeight) continue;
                if (visited[nx][ny]) continue;
                if (isBlocked(nx, ny)) continue;

                visited[nx][ny] = true;
                prevX[nx][ny] = x;
                prevY[nx][ny] = y;

                if (nx == tx && ny == ty) {
                    int cx = tx;
                    int cy = ty;

                    while (!(prevX[cx][cy] == sx && prevY[cx][cy] == sy)) {
                        int px = prevX[cx][cy];
                        int py = prevY[cx][cy];
                        if (px == -1) break;
                        cx = px;
                        cy = py;
                    }

                    return new int[]{cx, cy};
                }

                qx[tail] = nx;
                qy[tail] = ny;
                tail++;
            }
        }

        return null;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    private InputStream createMapInputStream(String path) throws IOException {
        // absolute or relative filesystem path
        if (new java.io.File(path).exists()) {
            return new FileInputStream(path);
        }

        // fallback: internal asset (for default maps)
        return Gdx.files.internal(path).read();
    }

}
