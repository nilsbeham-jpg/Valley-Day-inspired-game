package de.tum.cit.aet.valleyday.map.crops;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * CropTile stores the crop on one map tile.
 * It controls growth stages and timing.
 */
public class CropTile {

    private CropStage stage = CropStage.EMPTY;
    private float stageTime = 0f;
    private float matureTime = 0f;

    private CropType type;

    /**
     * Create a crop tile with a crop type.
     */
    public CropTile(CropType type) {
        this.type = type;
    }

    /**
     * Create an empty crop tile.
     */
    public CropTile() {
        this.type = null;
        this.stage = CropStage.EMPTY;
    }

    /**
     * @return current crop stage
     */
    public CropStage getStage() {
        return stage;
    }

    /**
     * @return true if no crop is planted
     */
    public boolean isEmpty() {
        return stage == CropStage.EMPTY;
    }

    /**
     * @return true if crop is mature
     */
    public boolean isMature() {
        return stage == CropStage.MATURE;
    }

    /**
     * @return true if crop is rotten
     */
    public boolean isRotten() {
        return stage == CropStage.ROTTEN;
    }

    /**
     * Clear this tile to empty state.
     */
    public void clearToEmpty() {
        stage = CropStage.EMPTY;
        stageTime = 0f;
        matureTime = 0f;
    }

    /**
     * Force crop to next growth stage.
     */
    public void advanceStage() {
        if (stage == CropStage.SEED) {
            stage = CropStage.SPROUT;
        } else if (stage == CropStage.SPROUT) {
            stage = CropStage.MATURE;
        }
    }

    /**
     * Restore crop if it is rotten.
     */
    public void restoreIfRotted() {
        if (stage == CropStage.ROTTEN) {
            stage = CropStage.MATURE;
            stageTime = 0f;
        }
    }

    /**
     * Plant a new crop on this tile.
     */
    public void plant(CropType type) {
        if (stage != CropStage.EMPTY) {
            return;
        }

        this.type = type;
        stage = CropStage.SEED;
        stageTime = 0f;
        matureTime = 0f;
    }

    /**
     * Harvest the crop if it is mature.
     */
    public void harvest() {
        if (stage != CropStage.MATURE) {
            return;
        }

        stage = CropStage.EMPTY;
        stageTime = 0f;
        matureTime = 0f;
        type = null;
    }

    /**
     * Reduce time until crop becomes rotten.
     */
    public void resetRotTimer(float seconds) {
        if (stage == CropStage.MATURE && type != null) {
            matureTime = Math.max(0f, type.matureToRotten() - seconds);
        }
    }

    /**
     * Update crop growth with time.
     */
    public void tick(float frameTime) {
        if (stage == CropStage.EMPTY) {
            return;
        }

        if (stage == CropStage.SEED) {
            stageTime += frameTime;
            if (stageTime >= type.seedToSprout()) {
                stage = CropStage.SPROUT;
                stageTime = 0f;
            }
            return;
        }

        if (stage == CropStage.SPROUT) {
            stageTime += frameTime;
            if (stageTime >= type.sproutToMature()) {
                stage = CropStage.MATURE;
                matureTime = 0f;
                stageTime = 0f;
            }
            return;
        }

        if (stage == CropStage.MATURE) {
            matureTime += frameTime;
            if (matureTime >= type.matureToRotten()) {
                stage = CropStage.ROTTEN;
            }
        }
    }

    /**
     * @return harvest value of this crop
     */
    public int getHarvestValue() {
        if (type == null) {
            return 0;
        }
        return type.harvestValue();
    }

    /**
     * @return texture for current crop stage
     */
    public TextureRegion getTexture() {
        if (type == null) {
            return null;
        }

        return switch (stage) {
            case SEED   -> type.textureSeed();
            case SPROUT -> type.textureSprout();
            case MATURE -> type.textureMature();
            case ROTTEN -> type.textureRotten();
            default     -> null;
        };
    }
}
