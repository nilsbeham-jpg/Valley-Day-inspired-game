package de.tum.cit.aet.valleyday.map.Items;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Textures;

public class Shovel extends Item {

    public Shovel(int x, int y) {
        super(x, y);
    }

    @Override
    public TextureRegion getTexture() {
        return Textures.SHOVEL;
    }

    @Override
    public void onPickup(GameMap map) {
        map.getPlayer().enableShovel();
    }
}
