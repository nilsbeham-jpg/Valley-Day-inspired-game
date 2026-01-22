package de.tum.cit.aet.valleyday.map.Wildlife;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.aet.valleyday.audio.Effectmusic;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.crops.CropStage;
import de.tum.cit.aet.valleyday.map.crops.CropTile;
import de.tum.cit.aet.valleyday.texture.Animations;

/**
 * SnailVisitor:
 * - harmless (touching player does NOT end game)
 * - never chases player
 * - slowly moves to mature crops and eats them
 */
public class SnailVisitor extends WildlifeBase {

    private static final int CROP_VISION_RANGE = 14;

    private static final float DECIDE_WANDER = 1.8f;
    private static final float DECIDE_SEEK   = 0.9f;

    // Snail is slow
    private static final float WALK_WANDER  = 1.0f;
    private static final float WALK_TO_CROP = 0.75f;

    // small pause after eating to make it feel "chewy"
    private float eatPauseLeft = 0f;
    private static final float EAT_PAUSE = 0.5f;

    private int lastWanderDx = 0;
    private int lastWanderDy = 0;

    public SnailVisitor(int x, int y, float speedMultiplier) {
        super(x, y, speedMultiplier);
        this.moveTimer = MathUtils.random(0f, DECIDE_WANDER);
    }

    @Override
    protected TextureRegion getFrame(float animTime) {
        return Animations.SNAIL_WALK.getKeyFrame(animTime, true);
    }

    @Override
    protected void decide(float dt, GameMap map) {
        if (!alive) {
            return;
        }

        // Pause after eating (no movement during pause)
        if (eatPauseLeft > 0f) {
            eatPauseLeft -= dt;
            if (eatPauseLeft < 0f) {
                eatPauseLeft = 0f;
            }
            return;
        }

        moveTimer -= dt;
        if (moveTimer > 0f) {
            return;
        }

        // Only seeks mature crops (never checks player position)
        int[] cropPos = map.findNearestMatureCrop(x, y, CROP_VISION_RANGE);
        if (cropPos != null) {
            moveTimer = DECIDE_SEEK;
            moveToward(cropPos[0], cropPos[1], map, WALK_TO_CROP, () -> moveWanderOneStep(map));
            return;
        }

        moveTimer = DECIDE_WANDER;
        moveWanderOneStep(map);
    }

    @Override
    protected void onStealOrInteract(GameMap map) {
        // Avoid repeated sound spam while pausing
        if (eatPauseLeft > 0f) {
            return;
        }

        CropTile crop = map.getCropAt(x, y);
        if (crop != null && crop.getStage() == CropStage.MATURE) {
            crop.harvest();

            // ✅ Use same sound as chicken
            Effectmusic.ChickenPickup.play();

            // pause to make it feel like "eating" and prevent multi-play in same tile
            eatPauseLeft = EAT_PAUSE;
        }
    }

    private void moveWanderOneStep(GameMap map) {
        int[][] dirs = new int[][]{{1,0},{-1,0},{0,1},{0,-1}};

        for (int tries = 0; tries < 8; tries++) {
            int i = MathUtils.random(3);
            int dx = dirs[i][0];
            int dy = dirs[i][1];

            if (dx == -lastWanderDx && dy == -lastWanderDy && MathUtils.randomBoolean(0.75f)) {
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

        lastWanderDx = 0;
        lastWanderDy = 0;
    }

    @Override
public boolean isDangerousToPlayer() {
    return false;
}
@Override
public void shoo(int playerTileX, int playerTileY) {
  
    despawn();
}


}
