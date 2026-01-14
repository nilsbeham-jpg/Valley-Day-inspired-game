package de.tum.cit.aet.valleyday.map.player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Animations;
import de.tum.cit.aet.valleyday.texture.Drawable;

/**
 * Represents the player character in the game.
 * The player has a hitbox, so it can collide with other objects in the game.
 */
public class Player implements Drawable {
    
    /** Total time elapsed since the game started. We use this for calculating the player movement and animating it. */
    private float elapsedTime; 
    
    private boolean moving; //Detect whether a person is moving

    private Direction facing= Direction.DOWN;  //The default character faces downward.

    /** The Box2D hitbox of the player, used for position and collision detection. */
    private final Body hitbox;

    private final GameMap map;

    private static final float RADIUS = 0.25f;

    private boolean hasShovel = false;

    public void enableShovel() {
        hasShovel = true;
    }




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
    // calculate were the player is next
        float nextX = hitbox.getPosition().x + xVelocity * frameTime;
        float nextY = hitbox.getPosition().y + yVelocity * frameTime;




        float currentX = hitbox.getPosition().x;
        float currentY = hitbox.getPosition().y;

// --- Horizontal collision ---
        if (xVelocity > 0) { // right
            int tileX = worldToTile(nextX + RADIUS);
            int tileY1 = worldToTile(currentY + RADIUS * 0.9f);
            int tileY2 = worldToTile(currentY - RADIUS * 0.9f);

            if (map.isBlocked(tileX, tileY1) || map.isBlocked(tileX, tileY2)) {
                xVelocity = 0;
            }
        }

        if (xVelocity < 0) { // left
            int tileX = worldToTile(nextX - RADIUS);
            int tileY1 = worldToTile(currentY + RADIUS * 0.9f);
            int tileY2 = worldToTile(currentY - RADIUS * 0.9f);

            if (map.isBlocked(tileX, tileY1) || map.isBlocked(tileX, tileY2)) {
                xVelocity = 0;
            }
        }


// --- Vertical collision ---
        if (yVelocity > 0) { // up
            int tileY = worldToTile(nextY + RADIUS);
            int tileX1 = worldToTile(currentX + RADIUS * 0.9f);
            int tileX2 = worldToTile(currentX - RADIUS * 0.9f);

            if (map.isBlocked(tileX1, tileY) || map.isBlocked(tileX2, tileY)) {
                yVelocity = 0;
            }
        }

        if (yVelocity < 0) { // down
            int tileY = worldToTile(nextY - RADIUS);
            int tileX1 = worldToTile(currentX + RADIUS * 0.9f);
            int tileX2 = worldToTile(currentX - RADIUS * 0.9f);

            if (map.isBlocked(tileX1, tileY) || map.isBlocked(tileX2, tileY)) {
                yVelocity = 0;
            }
        }






        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {

            int px = worldToTile(hitbox.getPosition().x);
            int py = worldToTile(hitbox.getPosition().y);

            int tx = px;
            int ty = py;

            switch (facing) {
                case UP -> ty += 1;
                case DOWN -> ty -= 1;
                case LEFT -> tx -= 1;
                case RIGHT -> tx += 1;
            }

            map.interactWithTile(tx, ty); //Interaction is directional
        }


        this.hitbox.setLinearVelocity(xVelocity, yVelocity);
        this.moving= (xVelocity!=0f)||(yVelocity!=0f);



    }
    public Direction getFacing(){ //check direction and return the facing direction
        return facing;
    }


    //find the front x-coordinate of the player 
    public int getTileX(){
        return (int) Math.round(getX());
    }
    public int getTileY(){
        return (int) Math.round(getY());
    }
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

    


    @Override
    public TextureRegion getCurrentAppearance() {
        // Get the frame of the walk down animation that corresponds to the current time. Let the player move.
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
