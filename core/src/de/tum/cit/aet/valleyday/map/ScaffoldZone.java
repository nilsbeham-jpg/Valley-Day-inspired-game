package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.TileObject;
import de.tum.cit.aet.valleyday.map.Wildlife.WildlifeBlocker;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.Wildlife.WildlifeBase;

/**
 * ScaffoldZone is an invisible area on the map.
 * It blocks wildlife from entering or spawning.
 *
 * It is created by scaffold item.
 */
public class ScaffoldZone extends TileObject implements WildlifeBlocker {

    /**
     * Create a scaffold zone at a tile position.
     * If wildlife is already here, it will be removed.
     *
     * @param x tile x position
     * @param y tile y position
     * @param map the game map
     */
    public ScaffoldZone(int x, int y, GameMap map) {
        super(x, y);

        WildlifeBase w = map.getWildlifeAt(x, y);
        if (w != null) {
            w.despawn();
        }
    }

    /**
     * Player can walk through this zone.
     */
    @Override
    public boolean isWalkable() {
        return true;
    }

    /**
     * This object has no texture.
     * It is invisible in the game.
     *
     * @return null always
     */
    @Override
    public TextureRegion getTexture() {
        return null;
    }

    /**
     * Scaffold zone cannot be destroyed.
     */
    @Override
    public boolean isDestructible() {
        return false;
    }

    /**
     * This zone blocks wildlife movement and spawning.
     *
     * @return true always
     */
    @Override
    public boolean blocksWildlife() {
        return true;
    }
}
