package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Contains all texture constants used in the game.
 * It is good practice to keep all textures and animations in constants to avoid loading them multiple times.
 * These can be referenced anywhere they are needed.
 *///这里是取贴图的 看看取哪个贴图
public class Textures {
    
    public static final TextureRegion FLOWERS = SpriteSheet.BASIC_TILES.at(2, 5);

    public static final TextureRegion CHEST = SpriteSheet.BASIC_TILES.at(5, 5);

    public static final TextureRegion PLAYER_DOWN  = SpriteSheet.CHARACTER.at(1, 1); // the directiontexture  of the player
    public static final TextureRegion PLAYER_LEFT  = SpriteSheet.CHARACTER.at(4, 1);
    public static final TextureRegion PLAYER_RIGHT = SpriteSheet.CHARACTER.at(2, 1);
    public static final TextureRegion PLAYER_UP    = SpriteSheet.CHARACTER.at(3, 1);


    public static final TextureRegion FENCE  = SpriteSheet.BASIC_TILES.at(8,4);
    public static final TextureRegion DEBRIS = SpriteSheet.BASIC_TILES.at(8, 3);
    public static final TextureRegion ROCK = SpriteSheet.BASIC_TILES.at(8, 8);
    public static final TextureRegion EXIT   = SpriteSheet.BASIC_TILES.at(7, 3);

    public static final TextureRegion CROP_SEED = SpriteSheet.CROPS.at(2, 5); 
    public static final TextureRegion CROP_SPROUT = SpriteSheet.CROPS.at(6, 5);
    public static final TextureRegion CROP_MATURE = SpriteSheet.CROPS.at(8, 5);
    public static final TextureRegion CROP_ROTTEN = SpriteSheet.CROPS.at(10, 5);

    public static final TextureRegion CROP_SEED2 = SpriteSheet.CROPS.at(2, 10);
    public static final TextureRegion CROP_SPROUT2 = SpriteSheet.CROPS.at(6, 10);
    public static final TextureRegion CROP_MATURE2 = SpriteSheet.CROPS.at(8, 10);
    public static final TextureRegion CROP_ROTTEN2 = SpriteSheet.CROPS.at(10, 10);

    public static final TextureRegion CROP_SEED3 = SpriteSheet.CROPS.at(2, 22);
    public static final TextureRegion CROP_SPROUT3 = SpriteSheet.CROPS.at(6, 22);
    public static final TextureRegion CROP_MATURE3 = SpriteSheet.CROPS.at(8, 22);
    public static final TextureRegion CROP_ROTTEN3 = SpriteSheet.CROPS.at(10, 22);



    public static final TextureRegion GRASS  = SpriteSheet.BASIC_TILES.at(9,1);
    public static final TextureRegion SHOVEL  = SpriteSheet.BASICS.at(3,6);
    public static final TextureRegion FERTILIZER  = SpriteSheet.HARVEST.at(1,5);
    public static final TextureRegion WATERCAN  = SpriteSheet.HARVEST.at(1,7);
    public static TextureRegion NON_FARMLAND  = SpriteSheet.BASIC_TILES.at(2,1);

    public static final TextureRegion SCARECROW  = SpriteSheet.SCARECROW.at(1,2);
    
}
