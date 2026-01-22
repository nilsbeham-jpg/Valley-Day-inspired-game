package de.tum.cit.aet.valleyday.map.Items;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.Wildlife.WildlifeBase;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * ScaffoldItem is a special item (like scarecrow).
 * It works when revealed, not when picked up.
 *
 * It removes wildlife and blocks new wildlife nearby.
 */
public class ScaffoldItem extends Item {

    /**
     * Create scaffold item.
     */
    public ScaffoldItem(int x, int y) {
        super(x, y);
    }

    /**
     * @return texture for scaffold
     */
    @Override
    public TextureRegion getTexture() {
        return Textures.SCARECROW;
    }

    /**
     * Activate when revealed from debris.
     * Clears wildlife in a small area.
     */
    @Override
    public void onReveal(GameMap map, int x, int y) {
        for (int dx = -1; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                int tx = x + dx;
                int ty = y + dy;

                if (tx < 0 || ty < 0 || tx >= map.getMapWidth() || ty >= map.getMapHeight()) {
                    continue;
                }

                WildlifeBase w = map.getWildlifeAt(tx, ty);
                if (w != null) {
                    w.despawn();
                }

                map.blockWildlifeAt(tx, ty);
            }
        }
    }

    /**
     * This item activates on reveal.
     */
    @Override
    public boolean activatesOnReveal() {
        return true;
    }

    /**
     * Scaffold cannot be picked up.
     */
    @Override
    public void onPickup(GameMap map) {
        // not pickable
    }

    /**
     * @return false always
     */
    @Override
    public boolean isPickable() {
        return false;
    }
}
