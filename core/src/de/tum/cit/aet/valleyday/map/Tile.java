package de.tum.cit.aet.valleyday.map;

public class Tile {

    public TileType type;        // what you see
    public TileType hiddenType;  // what is hidden (null if nothing)

    public Tile(TileType type) {
        this.type = type;
        this.hiddenType = null;
    }
}
