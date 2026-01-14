package de.tum.cit.aet.valleyday.map;

public abstract class TileObject extends GameObject {

    protected TileObject(int x, int y) {
        super(x, y);
    }

    public boolean isWalkable() {
        return true;
    }

    public boolean isDestructible() {
        return false;
    }
}
