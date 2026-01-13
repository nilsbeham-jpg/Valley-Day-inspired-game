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


/**
 * Represents the game map.
 * Holds all the objects and entities in the game.
 */
public class GameMap {
    
    // A static block is executed once when the class is referenced for the first time.
    static {
        // Initialize the Box2D physics engine.
        com.badlogic.gdx.physics.box2d.Box2D.init(); //box2d模块初始化
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
    
    private final Chest chest;
    
    private final Flowers[][] flowers;


    private Tile[][] tiles;
    private int mapWidth;
    private int mapHeight;






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



        // Create a chest in the middle of the map
        this.chest = new Chest(world, 3, 3); //初始箱子位置
        // Create flowers in a 7x7 grid
        this.flowers = new Flowers[7][7];
        for (int i = 0; i < flowers.length; i++) {
            for (int j = 0; j < flowers[i].length; j++) {
                this.flowers[i][j] = new Flowers(i, j);
            }
        }
    }
    
    /**
     * Updates the game state. This is called once per frame.
     * Every dynamic object in the game should update its state here.
     * @param frameTime the time that has passed since the last update
     *///先更新玩家再 step 物理，是常见做法（玩家把力/速度设置进 body，然后物理推进）
    public void tick(float frameTime) {
        this.player.tick(frameTime);
        doPhysicsStep(frameTime);
    }

    public boolean isFence(int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return true; // treat outside map as solid
        }
        return tiles[x][y].type == TileType.FENCE;
    }


    /**
     * Performs as many physics steps as necessary to catch up to the given frame time.
     * This will update the Box2D world by the given time step.
     * @param frameTime Time since last frame in seconds
     */ //这个东西是为了即使帧率波动，物理仍然以固定步长运行，结果更稳定
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

    /** Returns the chest on the map. */
    public Chest getChest() {
        return chest;
    }
    
    /** Returns the flowers on the map. */
    public List<Flowers> getFlowers() {
        return Arrays.stream(flowers).flatMap(Arrays::stream).toList();
    }
}
