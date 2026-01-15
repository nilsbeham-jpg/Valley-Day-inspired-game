package de.tum.cit.aet.valleyday.map.terrain;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Textures;

public class RockDebris extends Debris {

    public RockDebris(int x, int y) {
        super(x, y);
    }

    @Override
    public TextureRegion getTexture() {
        return Textures.ROCK; // add a rock texture
    }
}
