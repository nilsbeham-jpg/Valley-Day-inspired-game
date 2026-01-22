package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Central registry for all animations used in the game.
 *
 * This class defines animation constants for the player character and
 * wildlife entities. All animations are created once and reused to avoid
 * unnecessary texture loading and memory overhead.
 *
 * The class is intentionally static-only and holds no instance state.
 */
public class Animations {
    
    /**
     * The animation for the character walking down.
     */ //代表行动的动画 
    public static final Animation<TextureRegion> CHARACTER_WALK_DOWN = new Animation<>(0.2f,
            SpriteSheet.CHARACTER.at(1, 1),
            SpriteSheet.CHARACTER.at(1, 2),
            SpriteSheet.CHARACTER.at(1, 3),
            SpriteSheet.CHARACTER.at(1, 4)
    );


    public static final Animation<TextureRegion> CHARACTER_WALK_UP = new Animation<>(0.2f,
            SpriteSheet.CHARACTER.at(3, 1),
            SpriteSheet.CHARACTER.at(3, 2),
            SpriteSheet.CHARACTER.at(3, 3),
            SpriteSheet.CHARACTER.at(3, 4)
    );

    public static final Animation<TextureRegion> CHARACTER_WALK_LEFT = new Animation<>(0.2f,
            SpriteSheet.CHARACTER.at(4, 1),
            SpriteSheet.CHARACTER.at(4, 2),
            SpriteSheet.CHARACTER.at(4, 3),
            SpriteSheet.CHARACTER.at(4, 4)
    );

    public static final Animation<TextureRegion> CHARACTER_WALK_RIGHT = new Animation<>(0.2f,
            SpriteSheet.CHARACTER.at(2, 1),
            SpriteSheet.CHARACTER.at(2, 2),
            SpriteSheet.CHARACTER.at(2, 3),
            SpriteSheet.CHARACTER.at(2, 4)
    );
    
//CHICKEN WALK
     public static final Animation<TextureRegion> CHICKEN_WALK = new Animation<>(
            0.15f, // 
            SpriteSheet.FARM_THINGS.at(1, 1),
            SpriteSheet.FARM_THINGS.at(1, 2),
            SpriteSheet.FARM_THINGS.at(1, 3),
            SpriteSheet.FARM_THINGS.at(1, 4)
    );
    static {
        CHICKEN_WALK.setPlayMode(Animation.PlayMode.LOOP);
    }


    /**
     * Walking animation for snail wildlife.
     *
     * Uses a custom texture file with non-square frames to give the
     * snail a slower and more heavy movement feeling.
     */
        public static final com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> SNAIL_WALK;

        static {
            Texture snailTexture = new Texture(Gdx.files.internal("texture/snail.png"));

            TextureRegion[] snailFrames = new TextureRegion[] {
                    new TextureRegion(snailTexture, 0,   0, 41, 32),
                    new TextureRegion(snailTexture, 41,  0, 41, 32),
                    new TextureRegion(snailTexture, 82,  0, 41, 32)
            };

            SNAIL_WALK = new com.badlogic.gdx.graphics.g2d.Animation<>(
                    0.35f,          // slow animation → snail feeling
                    snailFrames
            );
        }

}

