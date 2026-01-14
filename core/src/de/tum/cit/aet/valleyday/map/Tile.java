package de.tum.cit.aet.valleyday.map;

public class Tile {

    private TileObject object;
    private TileObject hiddenObject;

    public Tile(TileObject object) {
        this.object = object;
    }

    public boolean isBlocked() {
        return object != null && !object.isWalkable();
    }

    public boolean hasHiddenObject() {
        return hiddenObject != null;
    }

    public void setHiddenObject(TileObject hidden) {
        this.hiddenObject = hidden;
    }

    public void interact() {
        if (object == null) return;

        if (object.isDestructible()) {
            if (hiddenObject != null) {
                object = hiddenObject;     //  reveal
                hiddenObject = null;
            } else {
                object = null;             //  just clear debris
            }
        }
    }


    public TileObject getObject() {
        return object;
    }
    public TileObject getHiddenObject() {
        return hiddenObject;
    }

}
