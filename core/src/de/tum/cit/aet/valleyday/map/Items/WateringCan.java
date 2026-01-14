package de.tum.cit.aet.valleyday.map.Items;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Textures;

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
        map.getPlayer().activateWateringCan(5f); // 5 seconds
    }

}

