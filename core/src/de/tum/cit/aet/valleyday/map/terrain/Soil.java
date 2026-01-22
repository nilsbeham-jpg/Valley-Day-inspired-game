package de.tum.cit.aet.valleyday.map.terrain;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.TileObject;

import static de.tum.cit.aet.valleyday.texture.Textures.GRASS;
import static de.tum.cit.aet.valleyday.texture.Textures.NON_FARMLAND;

/**
 * Soil represents the ground type of a tile.
 * It decides if crops can be planted or not.
 */
public class Soil extends TileObject {

    private SoilType type;

    /**
     * Create soil with a type.
     */
    public Soil(int x, int y, SoilType type) {
        super(x, y);
        this.type = type;
    }

    /**
     * Change soil type.
     */
    public void setType(SoilType type) {
        this.type = type;
    }

    /**
     * @return current soil type
     */
    public SoilType getType() {
        return type;
    }

    /**
     * @return true if crops can be planted here
     */
    public boolean isPlantable() {
        return type == SoilType.FARMLAND;
    }

    /**
     * Player can walk on soil.
     */
    @Override
    public boolean isWalkable() {
        return true;
    }

    /**
     * Soil cannot be destroyed.
     */
    @Override
    public boolean isDestructible() {
        return false;
    }

    /**
     * @return texture based on soil type
     */
    @Override
    public TextureRegion getTexture() {
        return switch (type) {
            case FARMLAND -> GRASS;
            case NON_FARMLAND -> NON_FARMLAND;
        };
    }
}

