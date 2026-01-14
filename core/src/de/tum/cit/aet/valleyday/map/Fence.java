package de.tum.cit.aet.valleyday.map;

public class Fence extends TileObject {
    public Fence(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean isWalkable() {
        return false;
    }
}
