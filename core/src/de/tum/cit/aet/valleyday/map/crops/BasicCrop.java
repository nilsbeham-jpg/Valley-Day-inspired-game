package de.tum.cit.aet.valleyday.map.crops;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import static de.tum.cit.aet.valleyday.texture.Textures.*;

/**
 * BasicCrop is a simple crop type.
 * It grows fast and gives low harvest value.
 */
public class BasicCrop extends CropType {

    /**
     * Time from seed to sprout.
     */
    @Override
    public float seedToSprout() {
        return 5f;
    }

    /**
     * Time from sprout to mature.
     */
    @Override
    public float sproutToMature() {
        return 5f;
    }

    /**
     * Time before the crop becomes rotten.
     */
    @Override
    public float matureToRotten() {
        return 60f;
    }

    /**
     * @return harvest value of this crop
     */
    @Override
    public int harvestValue() {
        return 1;
    }

    /**
     * @return texture for seed stage
     */
    @Override
    public TextureRegion textureSeed() {
        return CROP_SEED;
    }

    /**
     * @return texture for sprout stage
     */
    @Override
    public TextureRegion textureSprout() {
        return CROP_SPROUT;
    }

    /**
     * @return texture for mature stage
     */
    @Override
    public TextureRegion textureMature() {
        return CROP_MATURE;
    }

    /**
     * @return texture for rotten stage
     */
    @Override
    public TextureRegion textureRotten() {
        return CROP_ROTTEN;
    }
}
