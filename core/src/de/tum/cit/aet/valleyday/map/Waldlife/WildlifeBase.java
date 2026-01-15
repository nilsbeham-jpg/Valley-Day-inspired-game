package de.tum.cit.aet.valleyday.map.Waldlife;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.aet.valleyday.map.GameMap;


public abstract class WildlifeBase {

    // tile position (logic)
    protected int x;
    protected int y;

    // smooth render position
    protected float renderX;
    protected float renderY;

    protected boolean alive = true;
    protected float animTime = 0f;

    // step animation
    protected float stepFromX, stepFromY;
    protected float stepToX, stepToY;
    protected float stepTimeLeft = 0f;
    protected float stepDuration = 0f;

    // decision timer
    protected float moveTimer = 0f;

    // facing
    protected int lastDx = 0;
    protected int lastDy = 0;

    // cache for flipping (avoid flipping original regions)
    private TextureRegion cachedFrame;
    private TextureRegion cachedFlipX;

    // difficulty scaling
    protected final float speedMultiplier;

    protected WildlifeBase(int x, int y, float speedMultiplier) {
        this.x = x;
        this.y = y;
        this.renderX = x;
        this.renderY = y;

        this.stepFromX = x;
        this.stepFromY = y;
        this.stepToX = x;
        this.stepToY = y;

        this.speedMultiplier = speedMultiplier;
    }

    public boolean isAlive() {
        return alive;
    }

    public void despawn() {
        alive = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getRenderX() {
        return renderX;
    }

    public float getRenderY() {
        return renderY;
    }

    protected abstract TextureRegion getFrame(float animTime);

  
    protected abstract void decide(float dt, GameMap map);

   
    protected void onStealOrInteract(GameMap map) {
        // default: do nothing
    }

    public TextureRegion getCurrentAppearance() {
        TextureRegion frame = getFrame(animTime);

        // refresh cache when animation frame changes
        if (cachedFrame != frame) {
            cachedFrame = frame;
            cachedFlipX = new TextureRegion(frame);
            cachedFlipX.flip(true, false);
        }

        // moving left -> flipped
        return (lastDx < 0) ? cachedFlipX : cachedFrame;
    }

    public final void tick(float dt, GameMap map) {
        if (!alive) {
            return;
        }

        animTime += dt;
        updateStepAnimation(dt);

        // collision (triggers scared-run in GameMap)
        checkPlayerCollision(map);

       
        decide(dt, map);

        
        onStealOrInteract(map);

        checkPlayerCollision(map);
    }

    // ----------------------------
    // Smooth walking animation
    // ----------------------------
    protected void updateStepAnimation(float dt) {
        if (stepTimeLeft <= 0f) {
            renderX = stepToX;
            renderY = stepToY;
            return;
        }

        stepTimeLeft -= dt;
        if (stepTimeLeft < 0f) {
            stepTimeLeft = 0f;
        }

        float t = (stepDuration <= 0f) ? 1f : 1f - (stepTimeLeft / stepDuration);
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;

        renderX = stepFromX + (stepToX - stepFromX) * t;
        renderY = stepFromY + (stepToY - stepFromY) * t;
    }

    protected void beginStepTo(int nx, int ny, float walkTime) {
        stepFromX = renderX;
        stepFromY = renderY;

        stepToX = nx;
        stepToY = ny;

        stepDuration = walkTime;
        stepTimeLeft = walkTime;

        // update direction for facing
        lastDx = nx - x;
        lastDy = ny - y;

        x = nx;
        y = ny;
    }

    protected boolean tryStep(int nx, int ny, GameMap map, float walkTime) {
        if (map.isBlocked(nx, ny)) {
            return false;
        }
        beginStepTo(nx, ny, walkTime * speedMultiplier);
        return true;
    }

   
    protected void moveToward(int tx, int ty, GameMap map, float walkTime, Runnable fallbackIfStuck) {
        int dx = Integer.compare(tx, x); // -1/0/1
        int dy = Integer.compare(ty, y);

        boolean tryXFirst = Math.abs(tx - x) >= Math.abs(ty - y);

        if (tryXFirst) {
            if (tryStep(x + dx, y, map, walkTime)) return;
            if (tryStep(x, y + dy, map, walkTime)) return;
        } else {
            if (tryStep(x, y + dy, map, walkTime)) return;
            if (tryStep(x + dx, y, map, walkTime)) return;
        }

        if (fallbackIfStuck != null) {
            fallbackIfStuck.run();
        }
    }

    protected void checkPlayerCollision(GameMap map) {
        if (map.isLostByWildlife()) {
            return;
        }

        int px = map.worldToTile(map.getPlayer().getX());
        int py = map.worldToTile(map.getPlayer().getY());

        if (px == x && py == y) {
            map.loseByWildlife();
        }
    }

    protected int rand01() {
        return MathUtils.random(0, 1);
    }
}

