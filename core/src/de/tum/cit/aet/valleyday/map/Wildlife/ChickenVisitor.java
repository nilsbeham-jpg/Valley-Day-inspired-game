package de.tum.cit.aet.valleyday.map.Wildlife;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.aet.valleyday.audio.Effectmusic;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.crops.CropStage;
import de.tum.cit.aet.valleyday.map.crops.CropTile;
import de.tum.cit.aet.valleyday.texture.Animations;

public class ChickenVisitor extends WildlifeBase {

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

    // tiny anti-backtrack
    private int lastWanderDx = 0;
    private int lastWanderDy = 0;

    public ChickenVisitor(int x, int y, float speedMultiplier) {
        super(x, y, speedMultiplier);
        this.moveTimer = MathUtils.random(0f, DECIDE_WANDER);
    }

    @Override
    protected TextureRegion getFrame(float animTime) {
        return Animations.CHICKEN_WALK.getKeyFrame(animTime, true);
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

    @Override
    protected void decide(float dt, GameMap map) {
        if (!alive) {
            return;
        }

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
            moveToward(px, py, map, WALK_CHASE, () -> moveWanderOneStep(map));
            return;
        }

        // 2) else: chase mature crop if seen, otherwise wander
        int[] cropPos = map.findNearestMatureCrop(x, y, CROP_VISION_RANGE);
        if (cropPos != null) {
            moveTimer = DECIDE_CHASE;
            moveToward(cropPos[0], cropPos[1], map, WALK_CHASE, () -> moveWanderOneStep(map));
            return;
        }

        moveTimer = DECIDE_WANDER;
        moveWanderOneStep(map);
    }

    @Override
    protected void onStealOrInteract(GameMap map) {
        // steal crop if on mature
        CropTile crop = map.getCropAt(x, y);
        if (crop != null && crop.getStage() == CropStage.MATURE) {
            crop.harvest();
            Effectmusic.ChickenPickup.play();
        }
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
            if (dx == -lastWanderDx && dy == -lastWanderDy && MathUtils.randomBoolean(0.7f)) {
                continue;
            }

            int nx = x + dx;
            int ny = y + dy;

            if (map.isBlocked(nx, ny)) {
                continue;
            }

            
            lastWanderDx = dx;
            lastWanderDy = dy;

            beginStepTo(nx, ny, WALK_WANDER * speedMultiplier);
            return;
        }

        // stuck
        lastWanderDx = 0;
        lastWanderDy = 0;
    }
    @Override
public void shoo(int playerTileX, int playerTileY) {
    startFleeFrom(playerTileX, playerTileY);
}

}
