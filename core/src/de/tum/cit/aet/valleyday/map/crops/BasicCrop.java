package de.tum.cit.aet.valleyday.map.crops;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import static de.tum.cit.aet.valleyday.texture.Textures.*;

public class BasicCrop extends CropType {

    @Override
    public float seedToSprout() { return 5f; }

    @Override
    public float sproutToMature() { return 5f; }

    @Override
    public float matureToRotten() { return 10f; }

    @Override
    public int harvestValue() { return 1; }
    @Override public TextureRegion textureSeed()   { return CROP_SEED; }
    @Override public TextureRegion textureSprout() { return CROP_SPROUT; }
    @Override public TextureRegion textureMature() { return CROP_MATURE; }
    @Override public TextureRegion textureRotten() { return CROP_ROTTEN; }

}
