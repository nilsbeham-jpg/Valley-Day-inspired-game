package de.tum.cit.aet.valleyday.map.crops;

public class CropTile {
    private CropStage stage=CropStage.EMPTY; //Initialize the stage is empty
    private float stageTime=0f; //Initialize the stageTime
    private float matureTime=0f; //Initialize the maturetime
    
    private static final float SEED_TO_SPROUT= 10f; // Initialize the seed to sprout time
    private static final float SPROUT_TO_MATURE= 10f; // Initialize the sprout to mature time
    private static final float MATURE_TO_ROTTEN= 10f; // Initialize the mature to rooten time

public CropStage getStage(){
    return stage;
}
public boolean isEmpty(){
    return stage==CropStage.EMPTY; //know if the Tile is empty
}

public boolean isMature(){
    return stage == CropStage.MATURE; // know if the Tile is Mature
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



    public void plant(){
    if(stage!=CropStage.EMPTY){
        return;
    }
    stage=CropStage.SEED;
    stageTime=0f;
    matureTime=0f;
}

public void harvest(){
    if(stage!=CropStage.MATURE){
        return;
    }
    stage=CropStage.EMPTY;
    stageTime=0f;
    matureTime=0f;
}


    public void resetRotTimer(float seconds) {
        if (stage == CropStage.MATURE) {
            matureTime = Math.max(0f, MATURE_TO_ROTTEN - seconds);
        }
    }


public void tick(float frameTime){ //do nothing if it is empty
    if(stage==CropStage.EMPTY){
        return;
    }


    if(stage==CropStage.SEED){ //seed period
        stageTime+=frameTime;
    
    if(stageTime>=SEED_TO_SPROUT){ //  caculate the time from seed to sprout period
        stage=CropStage.SPROUT;
        stageTime=0f;
    }
    return;
    }

    if(stage==CropStage.SPROUT){ //sprout period
        stageTime+=frameTime;
        if(stageTime>=SPROUT_TO_MATURE){ //caculate the time from sprout to mature period
            stage=CropStage.MATURE;
            matureTime=0f;
            stageTime=0f;
        }
        return;
    }

    if(stage==CropStage.MATURE){ //mature period
        matureTime+=frameTime;
        if (matureTime >= MATURE_TO_ROTTEN) { //caculate the time from mature to rotten
                stage = CropStage.ROTTEN;
    }
    
    }




}
}
