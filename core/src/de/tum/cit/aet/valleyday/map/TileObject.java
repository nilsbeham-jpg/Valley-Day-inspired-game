package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class TileObject extends GameObject {
    // this class should bacically be "things on the terrain"

    protected TileObject(int x, int y) {
        super(x, y);
    }

    public boolean isWalkable() {
        return true;
    }

    public abstract TextureRegion getTexture();

    /** Can this object be destroyed / removed by interaction? */
    public boolean isDestructible() {
        return false;
    }
}
