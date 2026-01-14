package de.tum.cit.aet.valleyday.map.terrain;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.TileObject;
import de.tum.cit.aet.valleyday.texture.Textures;

public class Debris extends TileObject {
    public Debris(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    @Override
    public boolean isDestructible() {
        return true;
    }

    @Override
    public TextureRegion getTexture() {
        return Textures.DEBRIS;
    }
}
