package de.tum.cit.aet.valleyday.map.terrain;

import de.tum.cit.aet.valleyday.map.TileObject;

public class Fence extends TileObject {
    public Fence(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean isWalkable() {
        return false;
    }
}
