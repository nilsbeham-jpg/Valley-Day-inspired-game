package de.tum.cit.aet.valleyday.map;

import de.tum.cit.aet.valleyday.map.crops.CropTile;

public class Tile {

    private TileObject object;
    private TileObject hiddenObject;
    private CropTile crop;

    public Tile(TileObject object) {
        this.object = object;
        this.crop=null;
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
    public CropTile getCrop() {
        return crop;
    }

    public void setCrop(CropTile crop) {
        this.crop = crop;
    }

    public boolean hasCropTile() {
        return crop != null;
    }

    public TileObject getHiddenObject() {
        return hiddenObject;
    }
    public void clearObject() {
        object = null;
    }

}
