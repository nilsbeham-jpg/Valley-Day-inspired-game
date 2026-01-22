package de.tum.cit.aet.valleyday.map.crops;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import static de.tum.cit.aet.valleyday.texture.Textures.*;

/**
 * SlowCrop grows slower than basic crop.
 * It gives medium harvest value.
 */
public class SlowCrop extends CropType {

    @Override
    public float seedToSprout() {
        return 15f;
    }

    @Override
    public float sproutToMature() {
        return 15f;
    }

    @Override
    public float matureToRotten() {
        return 60f;
    }

    @Override
    public int harvestValue() {
        return 3;
    }

    @Override
    public TextureRegion textureSeed() {
        return CROP_SEED2;
    }

    @Override
    public TextureRegion textureSprout() {
        return CROP_SPROUT2;
    }

    @Override
    public TextureRegion textureMature() {
        return CROP_MATURE2;
    }

    @Override
    public TextureRegion textureRotten() {
        return CROP_ROTTEN2;
    }
}
