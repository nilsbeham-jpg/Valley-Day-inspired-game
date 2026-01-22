package de.tum.cit.aet.valleyday.map.player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.audio.Effectmusic;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Animations;
import de.tum.cit.aet.valleyday.texture.Drawable;

/**
 * Represents the player character in the game world.
 *
 * The player is a physics-based entity that reacts to user input,
 * queries the {@link GameMap} for collisions and interactions, and
 * maintains its own movement, animation, and item-related state.
 *
 * This class is responsible only for player-side logic; it does not
 * decide global game rules.
 */
public class Player implements Drawable {


      // --------------------
// Tool inventory (new)
// --------------------
public enum SelectedTool {
    NONE,
    FERTILIZER,
    WATERING_CAN
}

private int fertilizerCount = 0;
private int wateringCanCount = 0;
private SelectedTool selectedTool = SelectedTool.NONE;

public void addFertilizer(int amount) {
    if (amount <= 0) {
        return;
    }
    fertilizerCount += amount;
}

public void addWateringCan(int amount) {
    if (amount <= 0) {
        return;
    }
    wateringCanCount += amount;
}

public int getFertilizerCount() {
    return fertilizerCount;
}

public int getWateringCanCount() {
    return wateringCanCount;
}

public SelectedTool getSelectedTool() {
    return selectedTool;
}

public void cycleSelectedTool() {
    if (selectedTool == SelectedTool.NONE) {
        selectedTool = SelectedTool.FERTILIZER;
    } else if (selectedTool == SelectedTool.FERTILIZER) {
        selectedTool = SelectedTool.WATERING_CAN;
    } else {
        selectedTool = SelectedTool.NONE;
    }
}

public boolean canUseSelectedTool() {
    if (selectedTool == SelectedTool.FERTILIZER) {
        return fertilizerCount > 0;
    }
    if (selectedTool == SelectedTool.WATERING_CAN) {
        return wateringCanCount > 0;
    }
    return false;
}

public void consumeSelectedToolOnce() {
    if (selectedTool == SelectedTool.FERTILIZER) {
        if (fertilizerCount > 0) {
            fertilizerCount -= 1;
        }
    } else if (selectedTool == SelectedTool.WATERING_CAN) {
        if (wateringCanCount > 0) {
            wateringCanCount -= 1;
        }
    }
}

    /**
     * Total time elapsed since the game started. We use this for calculating the player movement and animating it.
     */
    private float elapsedTime;

    private boolean moving; //Detect whether a person is moving

    private Direction facing = Direction.DOWN;  //The default character faces downward.

    private boolean scared = false; //player can be scared
    private float forcedVX = 0f;
    private float forcedVY = 0f;

    private float walkSoundTimer = 0f;
    private static final float WALK_SOUND_INTERVAL = 0.35f;

    /**
     * The Box2D hitbox of the player, used for position and collision detection.
     */
    private final Body hitbox;

    private final GameMap map;

    private static final float RADIUS = 0.2f;

    private boolean hasShovel = false;
    private float fertilizerTimer = 0f;
    private float wateringCanTimer = 0f;


    // interaction state
    private float interactionTimer = 0f;
    private int interactingX = -1;
    private int interactingY = -1;
    private boolean interacting = false;


    /**
     * Returns the required interaction time for destroying objects.
     * Having a shovel reduces the required time.
     */
    private float getRequiredInteractionTime() {
        return hasShovel ? 0.5f : 1.0f;
    }

    /**
     * Enables shovel usage for the player.
     */
    public void enableShovel() {
        hasShovel = true;
    }

    /**
     * Creates a new player instance and spawns it into the physics world.
     *
     * @param world Box2D world
     * @param map   reference to the game map
     * @param x     spawn x-coordinate
     * @param y     spawn y-coordinate
     */
    public Player(World world, GameMap map, float x, float y) {
        this.map = map;
        this.hitbox = createHitbox(world, x, y);
    }

    /**
     * Creates a Box2D body for the player.
     * This is what the physics engine uses to move the player around and detect collisions with other bodies.
     *
     * @param world  The Box2D world to add the body to.
     * @param startX The initial X position.
     * @param startY The initial Y position.
     * @return The created body.
     */
    private Body createHitbox(World world, float startX, float startY) {
        // BodyDef is like a blueprint for the movement properties of the body.
        BodyDef bodyDef = new BodyDef(); //身体蓝图
        // Dynamic bodies are affected by forces and collisions.
        bodyDef.type = BodyDef.BodyType.KinematicBody; //会受速度、力、碰撞影响适合：玩家敌人可移动物体
        // Set the initial position of the body.
        bodyDef.position.set(startX, startY);
        // Create the body in the world using the body definition.
        Body body = world.createBody(bodyDef); //在world中 创建真正的身体
        // Now we need to give the body a shape so the physics engine knows how to collide with it.
        // We'll use a circle shape for the player.
        CircleShape circle = new CircleShape(); //创建一个碰撞形状
        // Give the circle a radius of 0.3 tiles (the player is 0.6 tiles wide).
        circle.setRadius(0.3f);
        // Attach the shape to the body as a fixture.
        // Bodies can have multiple fixtures, but we only need one for the player.
        body.createFixture(circle, 1.0f); //将碰撞形状给身体
        // We're done with the shape, so we should dispose of it to free up memory.
        circle.dispose(); //释放 shape 内存
        // Set the player as the user data of the body so we can look up the player from the body later.
        body.setUserData(this);
        return body;
    }


    /**
     * Main per-frame update of the player.
     *
     * Processes input, applies collision checks, updates movement,
     * handles interactions, sound effects, and animation timing.
     *
     * @param frameTime time since last frame (seconds)
     */
    public void tick(float frameTime) { // tick() tells physics how the player wants to move
        if (moving) {
            this.elapsedTime += frameTime;

        }
        // Make the player move in a circle with radius 2 tiles
        // You can change this to make the player move differently, e.g. in response to user input.
        // See Gdx.input.isKeyPressed() for keyboard input
        float speed = 3f;
        float xVelocity = 0f;
        float yVelocity = 0f;

        if (scared) {
        xVelocity = forcedVX;
        yVelocity = forcedVY;
        } else {
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            yVelocity = speed;
            facing = Direction.UP;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            yVelocity = -speed;
            facing = Direction.DOWN;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            xVelocity = -speed;
            facing = Direction.LEFT;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            xVelocity = speed;
            facing = Direction.RIGHT;
        }
    }
            // calculate were the player is next
            float nextX = hitbox.getPosition().x + xVelocity * frameTime;
            float nextY = hitbox.getPosition().y + yVelocity * frameTime;


            float currentX = hitbox.getPosition().x;
            float currentY = hitbox.getPosition().y;
    if(!scared){
    // --- Horizontal collision ---
            if (xVelocity > 0) { // right
                int tileX = map.worldToTile(nextX + RADIUS);
                int tileY1 = map.worldToTile(currentY + RADIUS * 0.9f);
                int tileY2 = map.worldToTile(currentY - RADIUS * 0.9f);


                if (map.isBlocked(tileX, tileY1) || map.isBlocked(tileX, tileY2)) {
                    xVelocity = 0;
                }
            }

            if (xVelocity < 0) { // left
                int tileX = map.worldToTile(nextX - RADIUS);
                int tileY1 = map.worldToTile(currentY + RADIUS * 0.9f);
                int tileY2 = map.worldToTile(currentY - RADIUS * 0.9f);

                if (map.isBlocked(tileX, tileY1) || map.isBlocked(tileX, tileY2)) {
                    xVelocity = 0;
                }
            }


        // --- Vertical collision ---
        if (yVelocity > 0) { // up
            int tileY = map.worldToTile(nextY + RADIUS);
            int tileX1 = map.worldToTile(currentX + RADIUS * 0.9f);
            int tileX2 = map.worldToTile(currentX - RADIUS * 0.9f);

            if (map.isBlocked(tileX1, tileY) || map.isBlocked(tileX2, tileY)) {
                yVelocity = 0;
            }
        }

        if (yVelocity < 0) { // down
            int tileY = map.worldToTile(nextY - RADIUS);
            int tileX1 = map.worldToTile(currentX + RADIUS * 0.9f);
            int tileX2 = map.worldToTile(currentX - RADIUS * 0.9f);

            if (map.isBlocked(tileX1, tileY) || map.isBlocked(tileX2, tileY)) {
                yVelocity = 0;
            }
        }
    }


        if (!scared) {
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                int[] front = map.getFrontTile(this);
                int fx = front[0];
                int fy = front[1];

                if (!interacting || fx != interactingX || fy != interactingY) {
                    interacting = true;
                    interactingX = fx;
                    interactingY = fy;
                    interactionTimer = 0f;
                }

                interactionTimer += frameTime;

                if (interactionTimer >= getRequiredInteractionTime()) {
                    map.interactWithTile(interactingX, interactingY);
                    interacting = false;
                    interactionTimer = 0f;
                }
            } else {
                interacting = false;
                interactionTimer = 0f;
            }

            if (fertilizerTimer > 0f) {
                fertilizerTimer -= frameTime;
                if (fertilizerTimer < 0f) fertilizerTimer = 0f;
            }

            if (wateringCanTimer > 0f) {
                wateringCanTimer -= frameTime;
                if (wateringCanTimer < 0f) wateringCanTimer = 0f;
            }
        }


        this.hitbox.setLinearVelocity(xVelocity, yVelocity);
        this.moving = (xVelocity != 0f) || (yVelocity != 0f);
        if (moving && !scared) {
            walkSoundTimer -= frameTime;
            if (walkSoundTimer <= 0f) {
                Effectmusic.Walking.play();
                walkSoundTimer = WALK_SOUND_INTERVAL;
            }
        } else {
            walkSoundTimer = 0f;
        }

}

    /**
     * @return the direction the player is currently facing
     */
    public Direction getFacing() { //check direction and return the facing direction
        return facing;
    }


    //find the front x-coordinate of the player
    /**
     * @return rounded tile x-coordinate of the player
     */
    public int getTileX() {
        return (int) Math.round(getX());
    }

    /**
     * @return rounded tile y-coordinate of the player
     */
    public int getTileY() {
        return (int) Math.round(getY());
    }

    /**
     * @return x-coordinate of the tile in front of the player
     */
    public int getFrontTileX() {
        int tileX = getTileX();
        if (facing == Direction.LEFT) {
            return tileX - 1;
        }
        if (facing == Direction.RIGHT) {
            return tileX + 1;
        }
        return tileX;
    }

    /**
     * @return y-coordinate of the tile in front of the player
     */
    public int getFrontTileY() {
        int tileY = getTileY();
        if (facing == Direction.DOWN) {
            return tileY - 1;
        }
        if (facing == Direction.UP) {
            return tileY + 1;
        }
        return tileY;
    }

    /**
     * Sets whether the player is scared.
     * Reset forced movement when calming down.
     */
    public void setScared(boolean scared) {
    this.scared = scared;
    if (!scared) {
        forcedVX = 0f;
        forcedVY = 0f;
    }
}

    /**
     * @return true if the player is currently scared
     */
    public boolean isScared() {
        return scared;
    }


    /**
     * Forces a velocity on the player, ignoring input.
     */
    public void setForcedVelocity(float vx, float vy) {
        this.forcedVX = vx;
        this.forcedVY = vy;
    }



    @Override
    public TextureRegion getCurrentAppearance() {
        // Get the frame of the walk down animation that corresponds to the current time. Let the player move.
        switch (facing) {
            case UP:
                return Animations.CHARACTER_WALK_UP.getKeyFrame(elapsedTime, true);
            case LEFT:
                return Animations.CHARACTER_WALK_LEFT.getKeyFrame(elapsedTime, true);
            case RIGHT:
                return Animations.CHARACTER_WALK_RIGHT.getKeyFrame(elapsedTime, true);
            case DOWN:
            default:
                return Animations.CHARACTER_WALK_DOWN.getKeyFrame(elapsedTime, true);
        }
    }

    @Override
    public float getX() {
        // The x-coordinate of the player is the x-coordinate of the hitbox (this can change every frame).
        return hitbox.getPosition().x;
    }

    @Override
    public float getY() {
        // The y-coordinate of the player is the y-coordinate of the hitbox (this can change every frame).
        return hitbox.getPosition().y;
    }


    public boolean hasShovel() {
        return hasShovel;
    }

    public void activateFertilizer(float duration) {
        fertilizerTimer = duration;
    }

    public void activateWateringCan(float duration) {
        wateringCanTimer = duration;
    }

    public boolean isFertilizerActive() {
        return fertilizerTimer > 0f;
    }

    public boolean isWateringCanActive() {
        return wateringCanTimer > 0f;
    }

}

