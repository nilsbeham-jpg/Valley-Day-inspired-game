package de.tum.cit.aet.valleyday.map.Wildlife;

/**
 * Marker interface for tile objects that block wildlife movement
 * but may still be walkable by the player.
 */
public interface WildlifeBlocker {

    /**
     * @return true if wildlife may not enter this tile
     */
    boolean blocksWildlife();
}
