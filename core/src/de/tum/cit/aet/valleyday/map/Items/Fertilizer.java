package de.tum.cit.aet.valleyday.map.Items;

import de.tum.cit.aet.valleyday.map.GameMap;

public class Fertilizer extends Item {

    public Fertilizer(int x, int y) {
        super(x, y);
    }

    @Override
    public void onPickup(GameMap map) {
        map.advanceAllCrops();
    }
}
