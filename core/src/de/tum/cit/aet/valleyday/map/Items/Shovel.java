package de.tum.cit.aet.valleyday.map.Items;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * Shovel unlocks special actions.
 * Player needs it to remove hard debris like tree
 */
public class Shovel extends Item {

    /**
     * Create shovel item.
     */
    public Shovel(int x, int y) {
        super(x, y);
    }

    /**
     * @return shovel texture
     */
    @Override
    public TextureRegion getTexture() {
        return Textures.SHOVEL;
    }

    /**
     * Enable shovel ability on player.
     */
    @Override
    public void onPickup(GameMap map) {
        map.getPlayer().enableShovel();
    }
}
