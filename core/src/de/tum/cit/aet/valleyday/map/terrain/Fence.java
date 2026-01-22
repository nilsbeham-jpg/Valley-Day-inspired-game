package de.tum.cit.aet.valleyday.map.terrain;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.TileObject;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * Fence is a solid object.
 * Player cannot walk through it and cannot destroy it.
 */
public class Fence extends TileObject {

    /**
     * Create fence at a tile position.
     */
    public Fence(int x, int y) {
        super(x, y);
    }

    /**
     * Fence blocks movement.
     */
    @Override
    public boolean isWalkable() {
        return false;
    }

    /**
     * @return fence texture
     */
    @Override
    public TextureRegion getTexture() {
        return Textures.FENCE;
    }
}
