package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.ValleyDayGame;

import de.tum.cit.aet.valleyday.map.Items.Fertilizer;
import de.tum.cit.aet.valleyday.map.Items.Item;
import de.tum.cit.aet.valleyday.map.Items.WateringCan;
import de.tum.cit.aet.valleyday.map.Items.Shovel;
import java.util.List;
import java.util.ArrayList;
import de.tum.cit.aet.valleyday.map.player.WildlifeVisitor;


import java.io.IOException;
import java.util.Properties;
import java.util.ArrayList;

import com.badlogic.gdx.math.MathUtils;

import com.badlogic.gdx.Input;
import de.tum.cit.aet.valleyday.map.crops.CropTile;
import de.tum.cit.aet.valleyday.map.player.Player;
import de.tum.cit.aet.valleyday.map.player.WildlifeVisitor;
import de.tum.cit.aet.valleyday.map.structures.Entrance;
import de.tum.cit.aet.valleyday.map.structures.Exit;
import de.tum.cit.aet.valleyday.map.terrain.Debris;
import de.tum.cit.aet.valleyday.map.terrain.Fence;


/**
 * The class is responsbible for:
 * loading the map layout
 * owning tiles, crops, and terrain objects
 * owning the physics World
 * owning the Player
 * advancing world simulation (physics + crops)
 * answering questions like “is this blocked?”, “is this an exit?”
 */
public class GameMap {
    
    // A static block is executed once when the class is referenced for the first time.
    static {
        // Initialize the Box2D physics engine.
        com.badlogic.gdx.physics.box2d.Box2D.init();
    }
    
    // Box2D physics simulation parameters (you can experiment with these if you want, but they work well as they are)
    /**
     * The time step for the physics simulation.
     * This is the amount of time that the physics simulation advances by in each frame.
     * It is set to 1/refreshRate, where refreshRate is the refresh rate of the monitor, e.g., 1/60 for 60 Hz.
     */
    private static final float TIME_STEP = 1f / Gdx.graphics.getDisplayMode().refreshRate; //物理时间推进秒1/显示器刷新率
    /** The number of velocity iterations for the physics simulation. */
    private static final int VELOCITY_ITERATIONS = 6;  //速度约束（碰撞反弹、摩擦等）
    /** The number of position iterations for the physics simulation. */
    private static final int POSITION_ITERATIONS = 2; //解决穿透修正（物体重叠）
    /**
     * The accumulated time since the last physics step.
     * We use this to keep the physics simulation at a constant rate even if the frame rate is variable.
     */
    private float physicsTime = 0; //解决问题：帧率不稳定时，物理仍能按固定 TIME_STEP 前进。
    
    /** The game, in case the map needs to access it. */
    private final ValleyDayGame game;
    /** The Box2D world for physics simulation. */
    private final World world;
    
    // Game objects
    private final Player player;
    

    private Tile[][] tiles;
    private int mapWidth;
    private int mapHeight;

    private CropTile[][] crops; // Creat CropTile and init the harvest and task(quota)
    private int harvested = 0;
    private int quota = 10;

    private final List<WildlifeVisitor> wildlife = new ArrayList<>();
    private static final int MAX_WILDLIFE = 3; 




    /*public void printMapToConsole() {
        System.out.println("=== MAP DEBUG VIEW ===");

        for (int y = mapHeight - 1; y >= 0; y--) {
            for (int x = 0; x < mapWidth; x++) {
                Tile tile = tiles[x][y];

                char symbol = switch (tile.type) {
                    case FENCE -> '#';
                    case DEBRIS -> 'D';
                    case ENTRANCE -> 'E';
                    default -> '.';
                };

                System.out.print(symbol);
            }
            System.out.println();
        }

        System.out.println("=====================");
    } */


    /**
     * Read map file
     * Determine map size
     * Create tiles
     * Post-process logic (exit hiding, crops)
     */

    private void loadMap(String path) {
        Properties props = new Properties();

        try {
            props.load(Gdx.files.internal(path).reader()); // reads the file given a certain path
        } catch (IOException e) {
            throw new RuntimeException("Failed to load map file: " + path, e);
        }

        // 1. Determine map size from max x/y found, because the provided maps does not specify width/height
        int maxX = 0;
        int maxY = 0;

        for (String key : props.stringPropertyNames()) { // loops threw all keys
            if (!key.contains(",")) continue;

            String[] parts = key.split(","); // split each line in x and y value
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);

            if (x > maxX) maxX = x; // if the current x or y coordinates are greater than the max they become the new max
            if (y > maxY) maxY = y;
        }

        mapWidth  = maxX + 1;
        mapHeight = maxY + 1;

        tiles = new Tile[mapWidth][mapHeight]; // A two-dimensional grid of Tile references

        // 2. Fill everything with EMPTY first
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                tiles[x][y] = new Tile(null);
            }
        }

        // 3. Read actual tiles from file
        for (String key : props.stringPropertyNames()) {
            if (!key.contains(",")) continue;

            String[] parts = key.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int value = Integer.parseInt(props.getProperty(key));

            if (value == 3) {
    if (wildlife.size() < MAX_WILDLIFE) {
        wildlife.add(new WildlifeVisitor(x, y));
    }
    tiles[x][y] = new Tile(null);
} else {
    tiles[x][y] = createTileFromValue(value, x, y);
}



        }
        // Check if there is an exit else
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

            // Only hide exit under debris (safety check)
            if (tile.getObject() instanceof Debris) {
                tile.setHiddenObject(new Exit(pos[0], pos[1]));
            }
        }
//If the map does not already contain an exit, then pick one existing debris tile at random and hide the exit underneath that debris.


        //  Initialize crops grid AFTER tiles are ready
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


    }

    private int[] findEntrancePosition() { // finds the entrance of the map
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
            case 1 -> new Tile(new Debris(x, y));
            case 2 -> new Tile(new Entrance(x, y));
            case 4 -> {
                Tile t = new Tile(new Debris(x, y));
                t.setHiddenObject(new Exit(x, y));
                yield t;
            }
            case 5 -> {
                Tile t = new Tile(new Debris(x, y));
                t.setHiddenObject(new Fertilizer(x, y));
                yield t;
            }
            case 6 -> {
                Tile t = new Tile(new Debris(x, y));
                t.setHiddenObject(new WateringCan(x, y));
                yield t;
            }
            case 7 -> {
                Tile t = new Tile(new Debris(x, y));
                t.setHiddenObject(new Shovel(x, y));
                yield t;
            }
            default -> new Tile(null);
        };
    }



    /**
     *
     * Used for:
     * destroying debris
     * revealing hidden objects
     *
     */

    public void interactWithTile(int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return;
        }

        Tile tile = tiles[x][y];

        if (tile.getObject() != null && tile.getObject().isDestructible()) {

            String objName = tile.getObject().getClass().getSimpleName();

            String hiddenName = tile.getHiddenObject() != null
                    ? tile.getHiddenObject().getClass().getSimpleName()
                    : null;

            if (hiddenName != null) {
                System.out.println(
                        "Interacted with " + objName +
                                " at (" + x + ", " + y + "), revealed " + hiddenName
                );
            } else {
                System.out.println(
                        "Interacted with " + objName +
                                " at (" + x + ", " + y + ")"
                );
            }

            tile.interact();
        }
    }













    public GameMap(ValleyDayGame game) {
        this.game = game;
        this.world = new World(Vector2.Zero, true); //Vector2.Zero：重力向量为 (0,0)，表示无重力（俯视角游戏常这样）。
        // true：允许“sleep”（不动的物体会休眠，省计算）
        // Create a player with initial position (1, 3)

       //this.player = new Player(this.world, 1, 3); //创建玩家位置

        loadMap("maps/map-2.properties");
        //printMapToConsole();
        int[] entrance = findEntrancePosition();
        this.player = new Player(this.world, this ,entrance[0], entrance[1]);




    }
    
    /**
     * Updates the game state. This is called once per frame.
     * Every dynamic object in the game should update its state here.
     * @param frameTime the time that has passed since the last update
     *///先更新玩家再 step 物理，是常见做法（玩家把力/速度设置进 body，然后物理推进）
    public void tick(float frameTime) {
        this.player.tick(frameTime);
        handleAKey();
        tickWildlife(frameTime);
        tickCrops(frameTime);
        doPhysicsStep(frameTime);
        checkItemPickup();
        handleSKey(frameTime); 
    }


    private void checkItemPickup() {
        int px = worldToTile(player.getX());
        int py = worldToTile(player.getY());

        if (px < 0 || py < 0 || px >= mapWidth || py >= mapHeight) {
            return;
        }

        Tile tile = tiles[px][py];

        if (tile.getObject() instanceof Item item) {
            item.onPickup(this);   // apply effect
            tile.clearObject();    // remove from map
        }
    }

    //-----------------------------------------------------------------------------

    /**
     * Reads keyboard input
     * Checks player-facing tile
     * Plants / harvests crops
     */
    
    //PRESS A TO PLANT AND HARVEST
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

    if (tile.getObject() != null) {
        return;
    }

    CropTile crop = crops[x][y];
    if (crop == null) {
        return;
    }

   
    if (crop.isRotten()) {
        crop.clearToEmpty();
        System.out.println("Clear rotten crop at (" + x + "," + y + ")");
        return;
    }

    // empty to plant
    if (crop.isEmpty()) {
        crop.plant();
        System.out.println("Plant at (" + x + "," + y + ")");
        return;
    }

    // Mature to harvest
    if (crop.isMature()) {
        crop.harvest();
        harvested += 1;

        System.out.println(
            "Harvest at (" + x + "," + y + "), harvested = " + harvested
        );
        return;
    }

    System.out.println(
        "A pressed but crop stage = " + crop.getStage() + " at (" + x + "," + y + ")"
    );
}

//--------------------------------------------------------------------------------------------

//-----------------------------------
// PRESS S TO SHOO WILDLIFE (only the single tile in front of the player)
private float shooCooldown = 0f;
private static final float SHOO_COOLDOWN = 0.4f; // 你可以调短/调长

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

    // Use the same precise front-tile logic as SPACE
    int[] front = getFrontTile(player);
    int fx = front[0];
    int fy = front[1];

    // bounds check
    if (fx < 0 || fy < 0 || fx >= mapWidth || fy >= mapHeight) {
        return;
    }

    // "Does not work through fences or debris":
    // If the front tile is blocked, do nothing.
    if (isBlocked(fx, fy)) {
        return;
    }

    // Player tile position (used to flee away from player)
    int px = worldToTile(player.getX());
    int py = worldToTile(player.getY());

    // Find a wildlife on exactly that tile
    for (WildlifeVisitor w : wildlife) {
        if (w.isAlive() && w.getX() == fx && w.getY() == fy) {

            
            w.startFleeFrom(px, py);

            shooCooldown = SHOO_COOLDOWN;
            System.out.println("Shoo wildlife at (" + fx + "," + fy + ")");
            return;
        }
    }

    // No wildlife there -> miss does not consume cooldown (your choice)
}
//---------------------------


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
    //-------------------------------------------------------------------

    private void tickWildlife(float dt) {
    for (WildlifeVisitor w : wildlife) {
        w.tick(dt, this);
    }
    wildlife.removeIf(w -> !w.isAlive());
}



    public boolean isBlocked(int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return true; // outside map is solid
        }

        return tiles[x][y].isBlocked();
    }



    public boolean isExit(int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return false;
        }
        return tiles[x][y].getObject() instanceof Exit;

    }

    private boolean gameWon = false;
    private boolean lostWildlife=false;
    public void loseByWildlife(){
        lostWildlife=true;
    }
    public boolean isLostByWildlife(){
        return lostWildlife;
    }
    
    public boolean hasPlayerReachedExit() {
        if (gameWon) return true;

        int[] tile = worldToTile(player.getX(), player.getY());
        int px = tile[0];
        int py = tile[1];


        if (isExit(px, py)) {
            gameWon = true;
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







    /**
     * Performs as many physics steps as necessary to catch up to the given frame time.
     * This will update the Box2D world by the given time step.
     * @param frameTime Time since last frame in seconds
     */ 
    private void doPhysicsStep(float frameTime) {
        this.physicsTime += frameTime;
        while (this.physicsTime >= TIME_STEP) {
            this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            this.physicsTime -= TIME_STEP;
        }
    }
    
    /** Returns the player on the map. */
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
//help chicken to find the food
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
            if (c == null) {
                continue;
            }
            if (c.getStage() != de.tum.cit.aet.valleyday.map.crops.CropStage.MATURE) {
                continue;
            }

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
// 

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

    int[][] dirs = new int[][]{{1,0},{-1,0},{0,1},{0,-1}};

    while (head < tail) {
        int x = qx[head];
        int y = qy[head];
        head++;

        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];

            if (nx < 0 || ny < 0 || nx >= mapWidth || ny >= mapHeight) {
                continue;
            }
            if (visited[nx][ny]) {
                continue;
            }
            if (isBlocked(nx, ny)) {
                continue;
            }

            visited[nx][ny] = true;
            prevX[nx][ny] = x;
            prevY[nx][ny] = y;

            if (nx == tx && ny == ty) {
                // 回溯到起点的下一步
                int cx = tx;
                int cy = ty;

                while (!(prevX[cx][cy] == sx && prevY[cx][cy] == sy)) {
                    int px = prevX[cx][cy];
                    int py = prevY[cx][cy];
                    if (px == -1) {
                        break;
                    }
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

    // 找不到路
    return null;
}





    public Tile[][] getTiles() {
        return tiles;
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
        return new int[] {
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

        return new int[] { tx, ty };
    }




}
