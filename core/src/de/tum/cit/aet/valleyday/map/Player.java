package de.tum.cit.aet.valleyday.map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.texture.Animations;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * Represents the player character in the game.
 * The player has a hitbox, so it can collide with other objects in the game.
 */
public class Player implements Drawable {
    
    /** Total time elapsed since the game started. We use this for calculating the player movement and animating it. */
    private float elapsedTime; //游戏累计经过的时间
    
    private boolean moving;

    private Direction facing= Direction.DOWN; 

    /** The Box2D hitbox of the player, used for position and collision detection. */
    private final Body hitbox;

    private final GameMap map;

    public Player(World world, GameMap map, float x, float y) {
        this.map = map;
        this.hitbox = createHitbox(world, x, y);
    }

    /**
     * Creates a Box2D body for the player.
     * This is what the physics engine uses to move the player around and detect collisions with other bodies.
     * @param world The Box2D world to add the body to.
     * @param startX The initial X position.
     * @param startY The initial Y position.
     * @return The created body.
     */
    private Body createHitbox(World world, float startX, float startY) {
        // BodyDef is like a blueprint for the movement properties of the body.
        BodyDef bodyDef = new BodyDef(); //身体蓝图
        // Dynamic bodies are affected by forces and collisions.
        bodyDef.type = BodyDef.BodyType.DynamicBody; //会受速度、力、碰撞影响适合：玩家敌人可移动物体
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

    private int worldToTile(float value) {
        return (int) Math.floor(value);
    }


    /**
     * Move the player around in a circle by updating the linear velocity of its hitbox every frame.
     * This doesn't actually move the player, but it tells the physics engine how the player should move next frame.
     * @param frameTime the time since the last frame.
     */
    public void tick(float frameTime) { // tick() tells physics how the player wants to move
        if(moving){
        this.elapsedTime += frameTime;
        }
        // Make the player move in a circle with radius 2 tiles
        // You can change this to make the player move differently, e.g. in response to user input.
        // See Gdx.input.isKeyPressed() for keyboard input
        float speed=3f;
        float xVelocity=0f;
        float yVelocity=0f;
        
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
        yVelocity = speed;
        facing= Direction.UP;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
        yVelocity = -speed;
        facing= Direction.DOWN;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
        xVelocity = -speed;
        facing= Direction.LEFT;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
        xVelocity = speed;
        facing= Direction.RIGHT;

    }
        float nextX = hitbox.getPosition().x + xVelocity * frameTime;
        float nextY = hitbox.getPosition().y + yVelocity * frameTime;

        int nextTileX = worldToTile(nextX);
        int nextTileY = worldToTile(nextY);

    // Check horizontal movement
        if (xVelocity != 0) {
            if (map.isFence(nextTileX, worldToTile(hitbox.getPosition().y))) {
                xVelocity = 0;
            }
        }

    // Check vertical movement
        if (yVelocity != 0) {
            if (map.isFence(worldToTile(hitbox.getPosition().x), nextTileY)) {
                yVelocity = 0;
            }
        }

        this.hitbox.setLinearVelocity(xVelocity, yVelocity);
        this.moving= (xVelocity!=0f)||(yVelocity!=0f);


    }
    public Direction getFacing(){
        return facing;
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        // Get the frame of the walk down animation that corresponds to the current time.
        switch (facing) {
        case UP:
            return Animations.CHARACTER_WALK_UP.getKeyFrame(elapsedTime,true);
        case LEFT:
            return Animations.CHARACTER_WALK_LEFT.getKeyFrame(elapsedTime,true);
        case RIGHT:
            return Animations.CHARACTER_WALK_RIGHT.getKeyFrame(elapsedTime,true);
        case DOWN:
        default:
            return Animations.CHARACTER_WALK_DOWN.getKeyFrame(elapsedTime,true);
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
}
