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
    // Decision timer
    // ----------------------------
    private float moveTimer = 0f;

    private static final float WANDER_DECISION_INTERVAL = 0.55f;
    private static final float CHASE_DECISION_INTERVAL  = 0.35f;

    // ----------------------------
    // Walk animation timing
    // ----------------------------
    private static final float WALK_TIME_WANDER = 0.7f;
    private static final float WALK_TIME_CHASE  = 0.15f;
    private static final float WALK_TIME_FLEE   = 0.1f;

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

    // ✅ NEW: face direction for sprite flip (方案A核心)
    private boolean faceLeft = false;

    private enum State { WANDER, CHASE, FLEE }
    private State state = State.WANDER;

    // chase target
    private int targetX = -1;
    private int targetY = -1;

    // ----------------------------
    // Flee behavior
    // ----------------------------
    private static final float FLEE_DURATION = 1.2f;
    private static final float FLEE_DECISION_INTERVAL = 0.18f;
    private float fleeTimeLeft = 0f;
    private int fleeFromX = -1;
    private int fleeFromY = -1;

    public WildlifeVisitor(int x, int y) {
        this.x = x;
        this.y = y;

        this.renderX = x;
        this.renderY = y;

        this.stepFromX = x;
        this.stepFromY = y;
        this.stepToX = x;
        this.stepToY = y;

        this.moveTimer = MathUtils.random(0f, WANDER_DECISION_INTERVAL);
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

    // ✅ CHANGED: flipX based on faceLeft
    public TextureRegion getCurrentAppearance() {
        TextureRegion frame = Animations.CHICKEN_WALK.getKeyFrame(animTime, true);

        // Important: copy, don't mutate shared frames
        TextureRegion out = new TextureRegion(frame);

        boolean flipped = out.isFlipX();
        if (faceLeft && !flipped) {
            out.flip(true, false);
        }
        if (!faceLeft && flipped) {
            out.flip(true, false);
        }

        return out;
    }

    public void startFleeFrom(int playerTileX, int playerTileY) {
        if (!alive) {
            return;
        }
        state = State.FLEE;
        fleeFromX = playerTileX;
        fleeFromY = playerTileY;
        fleeTimeLeft = FLEE_DURATION;

        moveTimer = 0f;

        targetX = -1;
        targetY = -1;
    }

    public void tick(float dt, GameMap map) {
        if (!alive) {
            return;
        }

        animTime += dt;

        updateStepAnimation(dt);

        checkPlayerCollision(map);

        if (state == State.FLEE) {
            tickFlee(dt, map);
            return;
        }

        moveTimer -= dt;
        if (moveTimer > 0f) {
            return;
        }

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

        if (state == State.CHASE) {
            moveChaseOneStep(map);
        } else {
            moveWanderOneStep(map);
        }

        CropTile crop = map.getCropAt(x, y);
        if (crop != null && crop.getStage() == CropStage.MATURE) {
            crop.harvest();
        }

        checkPlayerCollision(map);
    }

    // ----------------------------
    // Walking animation
    // ----------------------------
    private void updateStepAnimation(float dt) {
        if (stepTimeLeft <= 0f) {
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

        renderX = stepFromX + (stepToX - stepFromX) * t;
        renderY = stepFromY + (stepToY - stepFromY) * t;
    }

    private void beginStepTo(int nx, int ny, float walkTime) {
        stepFromX = renderX;
        stepFromY = renderY;

        stepToX = nx;
        stepToY = ny;

        stepDuration = walkTime;
        stepTimeLeft = walkTime;

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

        // ✅ Update facing BEFORE beginStepTo (because beginStepTo changes x)
        int dxMove = bestNx - x;
        if (dxMove < 0) {
            faceLeft = true;
        } else if (dxMove > 0) {
            faceLeft = false;
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
            moveWanderOneStep(map);
            state = State.WANDER;
            targetX = -1;
            targetY = -1;
            return;
        }

        int nx = next[0];
        int ny = next[1];

        // ✅ Update facing BEFORE beginStepTo
        int dxMove = nx - x;
        if (dxMove < 0) {
            faceLeft = true;
        } else if (dxMove > 0) {
            faceLeft = false;
        }

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

            if (dx == -lastDx && dy == -lastDy && MathUtils.randomBoolean(0.7f)) {
                continue;
            }

            int nx = x + dx;
            int ny = y + dy;

            if (map.isBlocked(nx, ny)) {
                continue;
            }

            // ✅ Update facing BEFORE beginStepTo
            if (dx < 0) {
                faceLeft = true;
            } else if (dx > 0) {
                faceLeft = false;
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
        if (map.isLostByWildlife()) {
            return;
        }

        int px = map.worldToTile(map.getPlayer().getX());
        int py = map.worldToTile(map.getPlayer().getY());

        if (px == x && py == y) {
            map.loseByWildlife(); // trigger scared (not instant gameover)
        }
    }
}
