package de.tum.cit.aet.valleyday.map.Items;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.TileObject;

/**
 * this Class is father class for all the Items.
 * 
 * Player can usually walk over and pick up items.
 */
public abstract class Item extends TileObject {

    /**
     * Create an item on a tile.
     */
    protected Item(int x, int y) {
        super(x, y);
    }

    /**
     * Items do not block movement.
     */
    @Override
    public boolean isWalkable() {
        return true;
    }

    /**
     * Items can be removed from the map.
     */
    @Override
    public boolean isDestructible() {
        return true;
    }

    /**
     * Some items activate when revealed from debris.
     * Default is false.
     */
    public boolean activatesOnReveal() {
        return false;
    }

    /**
     * Called when player picks up the item.
     */
    public abstract void onPickup(GameMap map);

    /**
     * Called when item is revealed.
     * Default does nothing.
     */
    public void onReveal(GameMap map, int x, int y) {
        // nothing here
    }

    /**
     * @return true if item can be picked up
     */
    public boolean isPickable() {
        return true;
    }
}
