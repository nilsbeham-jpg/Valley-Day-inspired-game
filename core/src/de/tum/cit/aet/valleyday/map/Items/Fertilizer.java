package de.tum.cit.aet.valleyday.map.Items;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * Fertilizer is an item for crops.
 * When player picks it, crops grow faster for a short time.
 */
public class Fertilizer extends Item {

    /**
     * Create fertilizer at a tile position.
     */
    public Fertilizer(int x, int y) {
        super(x, y);
    }

    /**
     * @return texture for fertilizer
     */
    @Override
    public TextureRegion getTexture() {
        return Textures.FERTILIZER;
    }

    /**
     * Called when player picks this item.
     * Crops advance and player gets fertilizer effect.
     */
    @Override
    public void onPickup(GameMap map) {
        map.advanceAllCrops();
        map.getPlayer().activateFertilizer(5f);
    }
}
