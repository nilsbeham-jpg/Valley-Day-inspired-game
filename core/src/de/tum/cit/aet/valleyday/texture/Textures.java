package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Contains all texture constants used in the game.
 * It is good practice to keep all textures and animations in constants to avoid loading them multiple times.
 * These can be referenced anywhere they are needed.
 *///这里是取贴图的 看看取哪个贴图
public class Textures {
    
    public static final TextureRegion FLOWERS = SpriteSheet.BASIC_TILES.at(2, 5);

    public static final TextureRegion CHEST = SpriteSheet.BASIC_TILES.at(5, 5);

    public static final TextureRegion PLAYER_DOWN  = SpriteSheet.CHARACTER.at(1, 1); //人物的方向
    public static final TextureRegion PLAYER_LEFT  = SpriteSheet.CHARACTER.at(4, 1);
    public static final TextureRegion PLAYER_RIGHT = SpriteSheet.CHARACTER.at(2, 1);
    public static final TextureRegion PLAYER_UP    = SpriteSheet.CHARACTER.at(3, 1);


    public static final TextureRegion FENCE  = SpriteSheet.BASIC_TILES.at(8,4);
    public static final TextureRegion DEBRIS = SpriteSheet.BASIC_TILES.at(8, 8);
    public static final TextureRegion EXIT   = SpriteSheet.BASIC_TILES.at(7, 3);

    public static final TextureRegion GRASS = SpriteSheet.BASIC_TILES.at(9, 1);

}
