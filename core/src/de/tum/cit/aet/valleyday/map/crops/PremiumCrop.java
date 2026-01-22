package de.tum.cit.aet.valleyday.map.crops;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import static de.tum.cit.aet.valleyday.texture.Textures.*;

/**
 * PremiumCrop grows very slow but gives high value.
 */
public class PremiumCrop extends CropType {

    @Override
    public float seedToSprout() {
        return 25f;
    }

    @Override
    public float sproutToMature() {
        return 25f;
    }

    @Override
    public float matureToRotten() {
        return 60f;
    }

    @Override
    public int harvestValue() {
        return 5;
    }

    @Override
    public TextureRegion textureSeed() {
        return CROP_SEED3;
    }

    @Override
    public TextureRegion textureSprout() {
        return CROP_SPROUT3;
    }

    @Override
    public TextureRegion textureMature() {
        return CROP_MATURE3;
    }

    @Override
    public TextureRegion textureRotten() {
        return CROP_ROTTEN3;
    }
}
