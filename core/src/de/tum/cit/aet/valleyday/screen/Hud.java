package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.aet.valleyday.GameState;
import de.tum.cit.aet.valleyday.map.player.Player;

/**
 * A Heads-Up Display (HUD) that displays information on the screen.
 * It uses a separate camera so that it is always fixed on the screen.
 */
public class Hud {
    
    /** The SpriteBatch used to draw the HUD. This is the same as the one used in the GameScreen. */
    private final SpriteBatch spriteBatch;
    /** The font used to draw text on the screen. */
    private final BitmapFont font;
    /** The camera used to render the HUD. */
    private final OrthographicCamera camera;

    private float remainingTime= 0f;
    private Player player;

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setRemainingTime(float time) {
        this.remainingTime = time;
    }


    public Hud(SpriteBatch spriteBatch, BitmapFont font) {
        this.spriteBatch = spriteBatch;
        this.font = font;
        this.camera = new OrthographicCamera();
    }
    
    /**
     * Renders the HUD on the screen.
     * This uses a different OrthographicCamera so that the HUD is always fixed on the screen.
     */
    public void render() {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        // Pause hint
        font.draw(
                spriteBatch,
                "Press Esc to Pause!",
                10,
                Gdx.graphics.getHeight() - 10
        );

        // Daylight countdown (rounded up)
        int secondsLeft = (int) Math.ceil(remainingTime);

        font.draw(
                spriteBatch,
                "Time left: " + secondsLeft,
                10,
                Gdx.graphics.getHeight() - 30
        );

        int y = Gdx.graphics.getHeight() - 60;

        font.draw(spriteBatch, "Tools:", 10, y);
        y -= 20;

        if (player.hasShovel()) {
            font.draw(spriteBatch, "- Shovel", 10, y);
            y -= 20;
        }
        if (player.isFertilizerActive()) {
            font.draw(spriteBatch, "Fertilizer active!", 10, y);
            y -= 20;
        }

        if (player.isWateringCanActive()) {
            font.draw(spriteBatch, "Watering Can active!", 10, y);
        }



        spriteBatch.end();

    }


    /**
     * Resizes the HUD when the screen size changes.
     * This is called when the window is resized.
     * @param width The new width of the screen.
     * @param height The new height of the screen.
     */ //Hud 随窗口变化
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    public void renderEndMessage(GameState state) {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        String text = (state == GameState.VICTORY)
                ? "YOU WIN!"
                : "GAME OVER";

        font.draw(
                spriteBatch,
                text,
                Gdx.graphics.getWidth() / 2f - 40,
                Gdx.graphics.getHeight() / 2f
        );

        font.draw(
                spriteBatch,
                "Press ENTER to return to menu",
                Gdx.graphics.getWidth() / 2f - 140,
                Gdx.graphics.getHeight() / 2f - 30
        );

        spriteBatch.end();
    }



}
