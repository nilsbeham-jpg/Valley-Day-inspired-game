package de.tum.cit.aet.valleyday.map.terrain;

import de.tum.cit.aet.valleyday.map.TileObject;

public class Soil extends TileObject {

    public Soil(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public boolean isDestructible() {
        return false;
    }
}
