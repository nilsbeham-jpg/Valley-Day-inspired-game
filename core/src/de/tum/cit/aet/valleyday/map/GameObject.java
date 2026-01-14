package de.tum.cit.aet.valleyday.map;

public abstract class GameObject {
// anything that has a position size... should extend Gameobject
    protected int x;
    protected int y;

    protected GameObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getTileX() {
        return x;
    }

    public int getTileY() {
        return y;
    }
}
