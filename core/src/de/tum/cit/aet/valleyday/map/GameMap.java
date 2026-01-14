package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.ValleyDayGame;

import java.util.Arrays;
import java.util.List;

import java.io.IOException;
import java.util.Properties;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

import com.badlogic.gdx.Input;


/**
 * Represents the game map.
 * Holds all the objects and entities in the game.
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




    public void printMapToConsole() {
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
    }




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
                tiles[x][y] = new Tile(TileType.EMPTY);
            }
        }

        // 3. Read actual tiles from file
        for (String key : props.stringPropertyNames()) {
            if (!key.contains(",")) continue;

            String[] parts = key.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int value = Integer.parseInt(props.getProperty(key));

            tiles[x][y] = createTileFromValue(value); // add the values to the tile if not EMPTY


        }
        // Check if there is an exit else
        boolean exitFound = false;
        ArrayList<int[]> debrisPositions = new ArrayList<>();

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                Tile tile = tiles[x][y];
                if (tile.hiddenType == TileType.EXIT) {
                    exitFound = true;
                }
                if (tile.type == TileType.DEBRIS) {
                    debrisPositions.add(new int[]{x, y});
                }
            }
        }

        if (!exitFound && !debrisPositions.isEmpty()) {
            int[] pos = debrisPositions.get(MathUtils.random(debrisPositions.size()-1));
            tiles[pos[0]][pos[1]].hiddenType = TileType.EXIT;
        } //If the map does not already contain an exit, then pick one existing debris tile at random and hide the exit underneath that debris.


        //  Initialize crops grid AFTER tiles are ready
    crops = new CropTile[mapWidth][mapHeight];

    for (int x = 0; x < mapWidth; x++) {
    for (int y = 0; y < mapHeight; y++) {

        // 暂时约定：EMPTY = 可种植的地
        if (tiles[x][y].type == TileType.EMPTY) {
            crops[x][y] = new CropTile();
        }
    }
}


    }

    private int[] findEntrancePosition() { // finds the entrance of the map
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (tiles[x][y].type == TileType.ENTRANCE) {
                    return new int[]{x, y};
                }
            }
        }
        throw new IllegalStateException("No entrance found in map!");
    }

    private Tile createTileFromValue(int value) {
        switch (value) {

            case 0:
                // Fence (indestructible)
                return new Tile(TileType.FENCE);

            case 1:
                // Debris (branch)
                return new Tile(TileType.DEBRIS);

            case 2:
                // Entrance (player start)
                return new Tile(TileType.ENTRANCE);

            case 3: {
                // Wildlife visitor (hidden under debris)
                Tile t = new Tile(TileType.DEBRIS);
                t.hiddenType = TileType.WILDLIFE;
                return t;
            }

            case 4: {
                // Exit (hidden under debris)
                Tile t = new Tile(TileType.DEBRIS);
                t.hiddenType = TileType.EXIT;
                return t;
            }

            case 5: {
                // Fertilizer (hidden under debris)
                Tile t = new Tile(TileType.DEBRIS);
                t.hiddenType = TileType.FERTILIZER;
                return t;
            }

            case 6: {
                // Watering can (hidden under debris)
                Tile t = new Tile(TileType.DEBRIS);
                t.hiddenType = TileType.WATERING_CAN;
                return t;
            }

            case 7: {
                // Shovel (hidden under debris)
                Tile t = new Tile(TileType.DEBRIS);
                t.hiddenType = TileType.SHOVEL;
                return t;
            }

            default:
                return new Tile(TileType.EMPTY);
        }
    }


    public void interactWithTile(int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return; //safeguard that only coordinates that make sense are entered
        }

        Tile tile = tiles[x][y];

        // Only debris can be interacted with (for now)
        if (tile.type == TileType.DEBRIS) {

            // If something was hidden, reveal it
            if (tile.hiddenType != null) {
                tile.type = tile.hiddenType;
                tile.hiddenType = null;
            } else {
                // Otherwise, debris is simply removed
                tile.type = TileType.EMPTY;
            }
            System.out.println("Interacted with tile at " + x + "," + y +
                    " -> " + tile.type); //debug

        }
    }










    public GameMap(ValleyDayGame game) {
        this.game = game;
        this.world = new World(Vector2.Zero, true); //Vector2.Zero：重力向量为 (0,0)，表示无重力（俯视角游戏常这样）。
        // true：允许“sleep”（不动的物体会休眠，省计算）
        // Create a player with initial position (1, 3)

       //this.player = new Player(this.world, 1, 3); //创建玩家位置

        loadMap("maps/map-2.properties");
        printMapToConsole();
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
        tickCrops(frameTime);
        doPhysicsStep(frameTime);
    }
    //-----------------------------------------------------------------------------
    private void handleAKey() {
    if (!Gdx.input.isKeyJustPressed(Input.Keys.A)) {
        return;
    }

    int fx = player.getFrontTileX();
    int fy = player.getFrontTileY();

    if (fx < 0 || fy < 0 || fx >= mapWidth || fy >= mapHeight) {
        return;
    }

    // 只允许在 EMPTY 这种“地面”上种/收
    if (tiles[fx][fy].type != TileType.EMPTY) {
        return;
    }

    CropTile crop = crops[fx][fy];
    if (crop == null) {
        return;
    }

    if (crop.isEmpty()) {
        crop.plant();
        System.out.println("Plant at (" + fx + "," + fy + ")");
        return;
    }

    if (crop.isMature()) {
        crop.harvest();
        harvested += 1;
        System.out.println("Harvest at (" + fx + "," + fy + "), harvested=" + harvested);
        return;
    }

    System.out.println("A pressed but stage=" + crop.getStage() + " at (" + fx + "," + fy + ")");
}

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


    public boolean isBlocked(int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return true; // outside map is solid
        }

        TileType type = tiles[x][y].type;
        return type == TileType.FENCE || type == TileType.DEBRIS;
    }


    public boolean isExit(int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return false;
        }
        return tiles[x][y].type == TileType.EXIT;
    }

    private boolean gameWon = false;

    public boolean hasPlayerReachedExit() {
        if (gameWon) return true;

        int px = worldToTile(player.getX());
        int py = worldToTile(player.getY());

        if (isExit(px, py)) {
            gameWon = true;
            return true;
        }

        return false;
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


    public Tile[][] getTiles() {
        return tiles;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    private int worldToTile(float value) {
        return (int) Math.floor(value);
    }


}
