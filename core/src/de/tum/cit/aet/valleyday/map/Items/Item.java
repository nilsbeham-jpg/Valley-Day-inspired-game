package de.tum.cit.aet.valleyday.map.Items;

import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.TileObject;

public abstract class Item extends TileObject {

    protected Item(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean isWalkable() {
        return true; // player can walk over items
    }

    @Override
    public boolean isDestructible() {
        return true;
    }
    public boolean activatesOnReveal(){
        return false;
    }
    
    public abstract void onPickup(GameMap map);

    /** Called when revealed from debris */
    public void onReveal(GameMap map, int x, int y) {
        // default: do nothing
    }
}
