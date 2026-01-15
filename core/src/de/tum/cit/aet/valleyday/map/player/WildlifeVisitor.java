package de.tum.cit.aet.valleyday.map.player;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import de.tum.cit.aet.valleyday.audio.Effectmusic;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.crops.CropStage;
import de.tum.cit.aet.valleyday.map.crops.CropTile;
import de.tum.cit.aet.valleyday.texture.Animations;

public class WildlifeVisitor {

    // tile position (logic)
    private int x;
    private int y;

    // smooth render position
    private float renderX;
    private float renderY;

    private boolean alive = true;
    private float animTime = 0f;

    // step animation
    private float stepFromX, stepFromY;
    private float stepToX, stepToY;
    private float stepTimeLeft = 0f;
    private float stepDuration = 0f;

    // decision timer
    private float moveTimer = 0f;

    // simple behavior params
    private static final int PLAYER_CHASE_RANGE = 4;   // 玩家靠近多少格就追
    private static final int CROP_VISION_RANGE  = 10;  // 找成熟作物的范围

    private static final float DECIDE_WANDER = 1.5f;
    private static final float DECIDE_CHASE  = 1f;
    private static final float DECIDE_FLEE   = 0.18f;

    private static final float WALK_WANDER = 0.6f;
    private static final float WALK_CHASE  = 0.3f;
    private static final float WALK_FLEE   = 0.12f;

    // flee
    private boolean fleeing = false;
    private float fleeTimeLeft = 0f;
    private static final float FLEE_DURATION = 1.2f;
    private int fleeFromX = -1;
    private int fleeFromY = -1;

    // tiny anti-backtrack + facing
    private int lastDx = 0;
    private int lastDy = 0;

    // cache for flipping (avoid flipping original regions)
    private TextureRegion cachedFrame;
    private TextureRegion cachedFlipX;

    // difficulty scaling
    private final float speedMultiplier;


    public WildlifeVisitor(int x, int y, float speedMultiplier) {
        this.x = x;
        this.y = y;
        this.renderX = x;
        this.renderY = y;
    
        this.stepFromX = x;
        this.stepFromY = y;
        this.stepToX = x;
        this.stepToY = y;

        this.moveTimer = MathUtils.random(0f, DECIDE_WANDER);
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

    /**
     */
    public TextureRegion getCurrentAppearance() {
        TextureRegion frame = Animations.CHICKEN_WALK.getKeyFrame(animTime, true);

        // refresh cache when animation frame changes
        if (cachedFrame != frame) {
            cachedFrame = frame;

            // make a flipped copy (DO NOT flip original)
            cachedFlipX = new TextureRegion(frame);
            cachedFlipX.flip(true, false);
        }

        // moving left -> flipped
        return (lastDx < 0) ? cachedFlipX : cachedFrame;
    }

    // called by S key
    public void startFleeFrom(int playerTileX, int playerTileY) {
        if (!alive) {
            return;
        }
        fleeing = true;
        fleeFromX = playerTileX;
        fleeFromY = playerTileY;
        fleeTimeLeft = FLEE_DURATION;
        moveTimer = 0f; // flee immediately
    }

    public void tick(float dt, GameMap map) {
        if (!alive) {
            return;
        }

        animTime += dt;
        updateStepAnimation(dt);

        // collision (triggers scared-run in GameMap)
        checkPlayerCollision(map);

        // flee behavior
        if (fleeing) {
            tickFlee(dt, map);
            return;
        }

        // decide next step
        moveTimer -= dt;
        if (moveTimer > 0f) {
            return;
        }

        int px = map.worldToTile(map.getPlayer().getX());
        int py = map.worldToTile(map.getPlayer().getY());
        int distPlayer = Math.abs(px - x) + Math.abs(py - y);

        // 1) player near -> chase player (simple greedy)
        if (distPlayer <= PLAYER_CHASE_RANGE) {
            moveTimer = DECIDE_CHASE;
            moveToward(px, py, map, WALK_CHASE * speedMultiplier);
        } else {
            // 2) else: chase mature crop if seen, otherwise wander
            int[] cropPos = map.findNearestMatureCrop(x, y, CROP_VISION_RANGE);
            if (cropPos != null) {
                moveTimer = DECIDE_CHASE;
                moveToward(cropPos[0], cropPos[1], map, WALK_CHASE * speedMultiplier);
            } else {
                moveTimer = DECIDE_WANDER;
                moveWanderOneStep(map);
            }
        }

        // steal crop if on mature
        CropTile crop = map.getCropAt(x, y);
        if (crop != null && crop.getStage() == CropStage.MATURE) {
            crop.harvest();
             Effectmusic.ChickenPickup.play();
        }

        checkPlayerCollision(map);
    }

    // ----------------------------
    // Smooth walking animation
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

        float t = (stepDuration <= 0f) ? 1f : 1f - (stepTimeLeft / stepDuration);
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

        // update direction for facing
        lastDx = nx - x;
        lastDy = ny - y;

        x = nx;
        y = ny;
    }

    // ----------------------------
    // FLEE (run away from player who shooed)
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
        moveTimer = DECIDE_FLEE;

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

        beginStepTo(bestNx, bestNy, WALK_FLEE * speedMultiplier);
        checkPlayerCollision(map);
    }

    // ----------------------------
    // CHASE helper (super simple)
    // ----------------------------
    private void moveToward(int tx, int ty, GameMap map, float walkTime) {
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

        moveWanderOneStep(map);
    }

    private boolean tryStep(int nx, int ny, GameMap map, float walkTime) {
        if (map.isBlocked(nx, ny)) {
            return false;
        }
        beginStepTo(nx, ny, walkTime * speedMultiplier);
        return true;
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

            beginStepTo(nx, ny, WALK_WANDER * speedMultiplier);
            return;
        }

        // stuck
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
            map.loseByWildlife(); // triggers scared-run (GameMap handles actual lose)
        }
    }
}
