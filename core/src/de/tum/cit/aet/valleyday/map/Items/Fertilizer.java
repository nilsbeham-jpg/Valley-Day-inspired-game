package de.tum.cit.aet.valleyday.map.Items;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Textures;

public class Fertilizer extends Item {

    public Fertilizer(int x, int y) {
        super(x, y);
    }

    @Override
    public TextureRegion getTexture() {
        return Textures.FERTILIZER;
    }

    @Override
    public void onPickup(GameMap map) {
        map.advanceAllCrops();
        map.getPlayer().activateFertilizer(5f);// 5 seconds
    }

}
