package de.tum.cit.aet.valleyday.map.crops;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * CropType defines how a crop grows and looks.
 */
public abstract class CropType {

    /** @return time from seed to sprout */
    public abstract float seedToSprout();

    /** @return time from sprout to mature */
    public abstract float sproutToMature();

    /** @return time until crop becomes rotten */
    public abstract float matureToRotten();

    /** @return harvest value */
    public abstract int harvestValue();

    /** @return seed texture */
    public abstract TextureRegion textureSeed();

    /** @return sprout texture */
    public abstract TextureRegion textureSprout();

    /** @return mature texture */
    public abstract TextureRegion textureMature();

    /** @return rotten texture */
    public abstract TextureRegion textureRotten();
}
