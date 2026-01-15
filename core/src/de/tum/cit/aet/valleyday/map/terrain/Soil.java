package de.tum.cit.aet.valleyday.map.terrain;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.TileObject;

import static de.tum.cit.aet.valleyday.texture.Textures.GRASS;
import static de.tum.cit.aet.valleyday.texture.Textures.NON_FARMLAND;

public class Soil extends TileObject {

    private SoilType type;

    public Soil(int x, int y, SoilType type) {
        super(x, y);
        this.type = type;
    }

    public void setType(SoilType type) {
        this.type = type;
    }

    public SoilType getType() {
        return type;
    }

    public boolean isPlantable() {
        return type == SoilType.FARMLAND;
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public boolean isDestructible() {
        return false;
    }

    @Override
    public TextureRegion getTexture() {
        return switch (type) {
            case FARMLAND -> GRASS;
            case NON_FARMLAND -> NON_FARMLAND;
        };
    }
}
