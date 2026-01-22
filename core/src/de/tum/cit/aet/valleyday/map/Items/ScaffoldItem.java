package de.tum.cit.aet.valleyday.map.Items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.ScaffoldZone;
import de.tum.cit.aet.valleyday.map.Wildlife.WildlifeBase;
import de.tum.cit.aet.valleyday.texture.Textures;

public class ScaffoldItem extends Item {

    public ScaffoldItem(int x, int y) {
        super(x, y);
    }

    @Override
    public TextureRegion getTexture() {
        return Textures.SCARECROW;
    }

    @Override
    public void onReveal(GameMap map, int x, int y) {

        // create 4x4 exclusion zone
        for (int dx = -1; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {

                int tx = x + dx;
                int ty = y + dy;

                if (tx < 0 || ty < 0 || tx >= map.getMapWidth() || ty >= map.getMapHeight()) {
                    continue;
                }

                // 1) kill wildlife
                WildlifeBase w = map.getWildlifeAt(tx, ty);
                if (w != null) {
                    w.despawn();
                }

                // 2) block future wildlife
                map.blockWildlifeAt(tx, ty);
            }
        }
        
    }

@Override
public boolean activatesOnReveal(){
        return true;
    }
    
    @Override
    public void onPickup(GameMap map) {
        // scaffold is NOT picked up
    }


}
