package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.ValleyDayGame;

import de.tum.cit.aet.valleyday.map.Items.Fertilizer;
import de.tum.cit.aet.valleyday.map.Items.WateringCan;
import de.tum.cit.aet.valleyday.map.Items.Shovel;


import java.io.IOException;
import java.util.Properties;
import java.util.ArrayList;

import com.badlogic.gdx.math.MathUtils;

import com.badlogic.gdx.Input;
import de.tum.cit.aet.valleyday.map.crops.CropTile;
import de.tum.cit.aet.valleyday.map.player.Player;
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

            tiles[x][y] = createTileFromValue(value, x, y); // add the values to the tile if not EMPTY


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
        tickCrops(frameTime);
        doPhysicsStep(frameTime);
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

    // empty to plant
    if (crop.isEmpty()) {
        crop.plant();
        System.out.println("Plant at (" + x + "," + y + ")");
        return;
    }

    //  Mature to harvest
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

        return tiles[x][y].isBlocked();
    }



    public boolean isExit(int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return false;
        }
        return tiles[x][y].getObject() instanceof Exit;

    }

    private boolean gameWon = false;

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

    public CropTile[][] getCrops() {
    return crops;
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
