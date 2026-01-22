package de.tum.cit.aet.valleyday.map;

import de.tum.cit.aet.valleyday.map.Items.Item;
import de.tum.cit.aet.valleyday.map.crops.CropTile;
import de.tum.cit.aet.valleyday.map.terrain.SoilType;

public class Tile {

    private TileObject object;
    private TileObject hiddenObject;
    private CropTile crop;


    private SoilType soilType;

    // Default constructor: grass / farmland
    public Tile(TileObject object) {
        this.object = object;
        this.crop = null;
        this.hiddenObject = null;
        this.soilType = SoilType.FARMLAND;
    }

    /* ---------- soil / ground logic ---------- */

    public SoilType getSoilType() {
        return soilType;
    }

    public void setSoilType(SoilType soilType) {
        this.soilType = soilType;
    }

    public boolean isPlantable() {
        return soilType == SoilType.FARMLAND && crop == null;
    }

    /* ---------- object logic ---------- */

    public boolean isBlocked() {
        return object != null && !object.isWalkable();
    }

    public boolean hasHiddenObject() {
        return hiddenObject != null;
    }

    public void setHiddenObject(TileObject hidden) {
        this.hiddenObject = hidden;
    }

    public TileObject interact() {
        if (object == null) return null;

        if (!object.isDestructible()) return null;

        if (hiddenObject != null) {
            TileObject revealed = hiddenObject;
            hiddenObject = null;
            object = null; // IMPORTANT: do NOT place revealed automatically
            return revealed;
        } else {
            object = null;
            return null;
        }
    }


    public TileObject getObject() {
        return object;
    }

    public void clearObject() {
        object = null;
    }

    /* ---------- crop logic ---------- */

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

    public boolean canPlantCrop() {
        return soilType == SoilType.FARMLAND   // grass only
                && object == null              // nothing blocking
                && crop != null                // crop slot exists
                && crop.isEmpty();             // not already planted
    }

    public boolean hasCrop() {
        return crop != null;
    }

    public void setObject(TileObject object) {
        this.object = object;
    }




}
