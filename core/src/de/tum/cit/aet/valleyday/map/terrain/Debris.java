package de.tum.cit.aet.valleyday.map.terrain;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.TileObject;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * Debris is an obstacle on the map.
 * Player cannot walk through it, but can destroy it.
 */
public class Debris extends TileObject {

    /**
     * Create debris at a tile position.
     */
    public Debris(int x, int y) {
        super(x, y);
    }

    /**
     * Debris blocks movement.
     */
    @Override
    public boolean isWalkable() {
        return false;
    }

    /**
     * Debris can be destroyed by player.
     */
    @Override
    public boolean isDestructible() {
        return true;
    }

    /**
     * @return debris texture
     */
    @Override
    public TextureRegion getTexture() {
        return Textures.DEBRIS;
    }
}
