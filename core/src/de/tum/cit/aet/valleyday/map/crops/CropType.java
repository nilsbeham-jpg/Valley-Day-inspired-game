package de.tum.cit.aet.valleyday.map.crops;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class CropType {

    public abstract float seedToSprout();
    public abstract float sproutToMature();
    public abstract float matureToRotten();

    public abstract int harvestValue(); //
    public abstract TextureRegion textureSeed();
    public abstract TextureRegion textureSprout();
    public abstract TextureRegion textureMature();
    public abstract TextureRegion textureRotten();

}
