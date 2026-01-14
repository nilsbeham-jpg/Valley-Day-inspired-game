package de.tum.cit.aet.valleyday.map;

public class Debris extends TileObject {
    public Debris(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    @Override
    public boolean isDestructible() {
        return true;
    }
}
