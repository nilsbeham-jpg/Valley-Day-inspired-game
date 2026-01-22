package de.tum.cit.aet.valleyday.map.terrain;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * RockDebris is a stronger type of debris.
 * It uses a rock texture and needs shovel to remove.
 */
public class RockDebris extends Debris {

    /**
     * Create rock debris at a tile position.
     */
    public RockDebris(int x, int y) {
        super(x, y);
    }

    /**
     * @return rock texture
     */
    @Override
    public TextureRegion getTexture() {
        return Textures.ROCK;
    }
}
