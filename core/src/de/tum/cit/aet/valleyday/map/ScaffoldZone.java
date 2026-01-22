package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.TileObject;
import de.tum.cit.aet.valleyday.map.Wildlife.WildlifeBlocker;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.Wildlife.WildlifeBase;

public class ScaffoldZone extends TileObject implements WildlifeBlocker {

    public ScaffoldZone(int x, int y, GameMap map) {
        super(x, y);

        WildlifeBase w = map.getWildlifeAt(x, y);
        if (w != null) {
            w.despawn();
        }
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public TextureRegion getTexture() {
        return null;
    }

    @Override
    public boolean isDestructible() {
        return false;
    }

    @Override
    public boolean blocksWildlife() {
        return true;
    }
}
