package de.tum.cit.aet.valleyday.map.Items;

import de.tum.cit.aet.valleyday.map.GameMap;

public class Shovel extends Item {

    public Shovel(int x, int y) {
        super(x, y);
    }

    @Override
    public void onPickup(GameMap map) {
        map.getPlayer().enableShovel();
    }
}
