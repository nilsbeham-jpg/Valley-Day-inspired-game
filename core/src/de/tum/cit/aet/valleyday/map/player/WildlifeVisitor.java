package de.tum.cit.aet.valleyday.map.player;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.crops.CropStage;
import de.tum.cit.aet.valleyday.map.crops.CropTile;
import de.tum.cit.aet.valleyday.texture.Animations;

public class WildlifeVisitor {

    // ----------------------------
    // Logical (tile) position
    // ----------------------------
    private int x;
    private int y;

    private boolean alive = true;

    // ----------------------------
    // Animation timing
    // ----------------------------
    private float animTime = 0f;

    // ----------------------------
    // Decision timer: when to choose the next tile step
    // (this is NOT the walking animation time)
    // ----------------------------
    private float moveTimer = 0f;

    private static final float WANDER_DECISION_INTERVAL = 0.55f; // 中等自然：游荡时多久决定走一步
    private static final float CHASE_DECISION_INTERVAL  = 0.35f; // 中等自然：追逐时更频繁决定走一步

    // ----------------------------
    // Walk animation (tile-to-tile walking)
    // Each move step plays for WALK_TIME_* seconds
    // ----------------------------
    private static final float WALK_TIME_WANDER = 0.22f; // 中等自然：走一格耗时
    private static final float WALK_TIME_CHASE  = 0.16f; // 追菜略快
    private static final float WALK_TIME_FLEE   = 0.12f; // 逃跑更快

    // Render position in tile units (smooth walking)
    private float renderX;
    private float renderY;

    // Step animation state
    private float stepDuration = 0f;
    private float stepTimeLeft = 0f;

    private float stepFromX;
    private float stepFromY;
    private float stepToX;
    private float stepToY;

    // ----------------------------
    // Vision & jitter control
    // ----------------------------
    private static final int VISION_RANGE = 10;

    private int lastDx = 0;
    private int lastDy = 0;

    private enum State { WANDER, CHASE, FLEE }
    private State state = State.WANDER;

    // chase target
    private int targetX = -1;
    private int targetY = -1;

    // ----------------------------
    // Flee behavior
    // ----------------------------
    private static final float FLEE_DURATION = 1.2f; // 跑多久后消失
    private static final float FLEE_DECISION_INTERVAL = 0.18f; // 逃跑时多久走一步
    private float fleeTimeLeft = 0f;
    private int fleeFromX = -1;
    private int fleeFromY = -1;

    public WildlifeVisitor(int x, int y) {
        this.x = x;
        this.y = y;

        // init render at current tile
        this.renderX = x;
        this.renderY = y;

        this.stepFromX = x;
        this.stepFromY = y;
        this.stepToX = x;
        this.stepToY = y;

        // offset different chickens
        this.moveTimer = MathUtils.random(0f, WANDER_DECISION_INTERVAL);
    }

    public boolean isAlive() {
        return alive;
    }

    public void despawn() {
        alive = false;
    }

    // logical coordinates (for collisions/logic)
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // render coordinates (for drawing)
    public float getRenderX() {
        return renderX;
    }

    public float getRenderY() {
        return renderY;
    }

    public TextureRegion getCurrentAppearance() {
        return Animations.CHICKEN_WALK.getKeyFrame(animTime, true);
    }

    /**
     * Called when the player successfully shoos the wildlife.
     * The chicken will run away for a short while and then disappear.
     */
    public void startFleeFrom(int playerTileX, int playerTileY) {
        if (!alive) {
            return;
        }
        state = State.FLEE;
        fleeFromX = playerTileX;
        fleeFromY = playerTileY;
        fleeTimeLeft = FLEE_DURATION;

        // start fleeing immediately
        moveTimer = 0f;

        // clear chase target
        targetX = -1;
        targetY = -1;
    }

    public void tick(float dt, GameMap map) {
        if (!alive) {
            return;
        }

        animTime += dt;

        // 1) update walking animation every frame
        updateStepAnimation(dt);

        // 2) collision check (tile-based)
        checkPlayerCollision(map);

        // 3) state updates / decisions
        if (state == State.FLEE) {
            tickFlee(dt, map);
            return;
        }

        moveTimer -= dt;
        if (moveTimer > 0f) {
            return;
        }

        // Decide CHASE vs WANDER
        int[] seen = map.findNearestMatureCrop(x, y, VISION_RANGE);
        if (seen != null) {
            state = State.CHASE;
            targetX = seen[0];
            targetY = seen[1];
        } else {
            state = State.WANDER;
            targetX = -1;
            targetY = -1;
        }

        moveTimer = (state == State.CHASE) ? CHASE_DECISION_INTERVAL : WANDER_DECISION_INTERVAL;

        // move one tile step
        if (state == State.CHASE) {
            moveChaseOneStep(map);
        } else {
            moveWanderOneStep(map);
        }

        // steal crop if on mature
        CropTile crop = map.getCropAt(x, y);
        if (crop != null && crop.getStage() == CropStage.MATURE) {
            crop.harvest();
        }

        checkPlayerCollision(map);
    }

    // ----------------------------
    // Walking animation: from -> to within stepDuration seconds
    // ----------------------------
    private void updateStepAnimation(float dt) {
        if (stepTimeLeft <= 0f) {
            // not walking currently
            renderX = stepToX;
            renderY = stepToY;
            return;
        }

        stepTimeLeft -= dt;
        if (stepTimeLeft < 0f) {
            stepTimeLeft = 0f;
        }

        float t;
        if (stepDuration <= 0f) {
            t = 1f;
        } else {
            t = 1f - (stepTimeLeft / stepDuration);
        }

        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;

        // Linear walk: looks like "walk", not "float"
        renderX = stepFromX + (stepToX - stepFromX) * t;
        renderY = stepFromY + (stepToY - stepFromY) * t;
    }

    // start a tile-to-tile walking step
    private void beginStepTo(int nx, int ny, float walkTime) {
        // start from current render position (so it stays smooth even if interrupted)
        stepFromX = renderX;
        stepFromY = renderY;

        stepToX = nx;
        stepToY = ny;

        stepDuration = walkTime;
        stepTimeLeft = walkTime;

        // update logical position immediately
        x = nx;
        y = ny;
    }

    // ----------------------------
    // FLEE
    // ----------------------------
    private void tickFlee(float dt, GameMap map) {
        fleeTimeLeft -= dt;
        if (fleeTimeLeft <= 0f) {
            despawn();
            return;
        }

        moveTimer -= dt;
        if (moveTimer > 0f) {
            return;
        }
        moveTimer = FLEE_DECISION_INTERVAL;

        int bestNx = x;
        int bestNy = y;
        int bestScore = Integer.MIN_VALUE;

        int[][] dirs = new int[][]{{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];

            if (map.isBlocked(nx, ny)) {
                continue;
            }

            int score = Math.abs(nx - fleeFromX) + Math.abs(ny - fleeFromY);
            score += MathUtils.random(0, 1);

            if (score > bestScore) {
                bestScore = score;
                bestNx = nx;
                bestNy = ny;
            }
        }

        lastDx = bestNx - x;
        lastDy = bestNy - y;

        beginStepTo(bestNx, bestNy, WALK_TIME_FLEE);

        checkPlayerCollision(map);
    }

    // ----------------------------
    // CHASE
    // ----------------------------
    private void moveChaseOneStep(GameMap map) {
        if (targetX < 0) {
            return;
        }

        int[] next = map.nextStepBfs(x, y, targetX, targetY);
        if (next == null) {
            // fallback
            moveWanderOneStep(map);
            state = State.WANDER;
            targetX = -1;
            targetY = -1;
            return;
        }

        int nx = next[0];
        int ny = next[1];

        lastDx = nx - x;
        lastDy = ny - y;

        beginStepTo(nx, ny, WALK_TIME_CHASE);
    }

    // ----------------------------
    // WANDER
    // ----------------------------
    private void moveWanderOneStep(GameMap map) {
        int[][] dirs = new int[][]{{1,0},{-1,0},{0,1},{0,-1}};

        for (int tries = 0; tries < 8; tries++) {
            int i = MathUtils.random(3);
            int dx = dirs[i][0];
            int dy = dirs[i][1];

            // avoid instant backtracking most of the time
            if (dx == -lastDx && dy == -lastDy && MathUtils.randomBoolean(0.7f)) {
                continue;
            }

            int nx = x + dx;
            int ny = y + dy;

            if (map.isBlocked(nx, ny)) {
                continue;
            }

            lastDx = dx;
            lastDy = dy;

            beginStepTo(nx, ny, WALK_TIME_WANDER);
            return;
        }

        lastDx = 0;
        lastDy = 0;
    }

    // ----------------------------
    // Collision
    // ----------------------------
    private void checkPlayerCollision(GameMap map) {
        int px = map.worldToTile(map.getPlayer().getX());
        int py = map.worldToTile(map.getPlayer().getY());
        if (px == x && py == y) {
            map.loseByWildlife();
        }
    }
}
