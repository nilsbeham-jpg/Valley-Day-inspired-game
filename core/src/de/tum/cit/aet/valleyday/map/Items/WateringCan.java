package de.tum.cit.aet.valleyday.map.Items;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * Watering can item.
 *
 * Pick up will NOT use it instantly.
 * It will be stored in player inventory.
 */
public class WateringCan extends Item {

    public WateringCan(int x, int y) {
        super(x, y);
    }

    @Override
    public TextureRegion getTexture() {
        return Textures.WATERCAN;
    }

    @Override
    public void onPickup(GameMap map) {
        map.getPlayer().addWateringCan(1);
        // no instant effect, player can use it later (E key)
    }
}
