package de.tum.cit.aet.valleyday.map.crops;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class CropTile {
    private CropStage stage=CropStage.EMPTY; //Initialize the stage is empty
    private float stageTime=0f; //Initialize the stageTime
    private float matureTime=0f; //Initialize the maturetime
    


    private CropType type;

    public CropTile(CropType type) {
        this.type = type;
    }
    public CropTile() {
        this.type = null;   // no crop planted yet
        this.stage = CropStage.EMPTY;
    }



    public CropStage getStage(){
    return stage;
}
public boolean isEmpty(){
    return stage==CropStage.EMPTY; //know if the Tile is empty
}

public boolean isMature(){
    return stage == CropStage.MATURE; // know if the Tile is Mature
}
public boolean isRotten() {
    return stage == CropStage.ROTTEN;
}

public void clearToEmpty() {
    stage = CropStage.EMPTY;
    stageTime = 0f;
    matureTime = 0f;
}



 public void advanceStage() {
        if (stage == CropStage.SEED) {
            stage = CropStage.SPROUT;
        } else if (stage == CropStage.SPROUT) {
            stage = CropStage.MATURE;
        }
    }


    public void restoreIfRotted() {
        if (stage == CropStage.ROTTEN) {
            stage = CropStage.MATURE;
            stageTime = 0f;
        }
    }



    public void plant(CropType type) {
        if (stage != CropStage.EMPTY) return;

        this.type = type;
        stage = CropStage.SEED;
        stageTime = 0f;
        matureTime = 0f;
    }



    public void harvest() {
        if (stage != CropStage.MATURE) return;

        stage = CropStage.EMPTY;
        stageTime = 0f;
        matureTime = 0f;
        type = null;
    }



    public void resetRotTimer(float seconds) {
        if (stage == CropStage.MATURE && type != null) {
            matureTime = Math.max(0f, type.matureToRotten() - seconds);
        }
    }



    public void tick(float frameTime) {
        if (stage == CropStage.EMPTY) return;

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


    public int getHarvestValue() {
        if (type == null) {
            return 0;
        }
        return type.harvestValue();
    }


    public TextureRegion getTexture() {
        if (type == null) return null;

        return switch (stage) {
            case SEED    -> type.textureSeed();
            case SPROUT  -> type.textureSprout();
            case MATURE  -> type.textureMature();
            case ROTTEN  -> type.textureRotten();
            default      -> null;
        };
    }


}
