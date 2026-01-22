package de.tum.cit.aet.valleyday.map.Items;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * WateringCan helps crops.
 * It refresh crops and gives a short buff.
 */
public class WateringCan extends Item {

    /**
     * Create watering can.
     */
    public WateringCan(int x, int y) {
        super(x, y);
    }

    /**
     * @return watering can texture
     */
    @Override
    public TextureRegion getTexture() {
        return Textures.WATERCAN;
    }

    /**
     * Apply watering effect when picked up.
     */
    @Override
    public void onPickup(GameMap map) {
        map.applyWateringCan();
        map.getPlayer().activateWateringCan(5f);
    }
}
