package de.tum.cit.aet.valleyday.map.Items;

import de.tum.cit.aet.valleyday.map.GameMap;

public class WateringCan extends Item {

    public WateringCan(int x, int y) {
        super(x, y);
    }

    @Override
    public void onPickup(GameMap map) {
        map.restoreAllCrops();
    }
}
