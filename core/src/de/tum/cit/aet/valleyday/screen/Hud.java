package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.aet.valleyday.GameState;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.player.Player;

import static com.badlogic.gdx.scenes.scene2d.ui.Table.Debug.table;


/**
 * Heads-Up Display (HUD) for the gameplay screen.
 *
 * This class is responsible for rendering all on-screen UI elements that
 * should stay fixed relative to the screen, independent of the world camera.
 * This includes timers, tool status, exit state, and end-of-game messages.
 *
 * The HUD does not modify game logic; it only visualizes the current state.
 */
public class Hud {
    
    /** The SpriteBatch used to draw the HUD. This is the same as the one used in the GameScreen. */
    private final SpriteBatch spriteBatch;
    /** The font used to draw text on the screen. */
    private final BitmapFont font;
    /** The camera used to render the HUD. */
    private final OrthographicCamera camera;
    private GameMap map;

    private float remainingTime= 0f;
    private Player player;
    private Stage endStage;
    private Table endTable;
    private final Skin skin;
    private Label difficultyLabel;
    private static final float TOTAL_TIME = 320f;


    /**
     * Assigns the player reference used by the HUD.
     *
     * @param player current player instance
     */
    public void setPlayer(Player player) {
        this.player = player;
    }


    /**
     * Updates the remaining time displayed by the HUD.
     *
     * @param time remaining time in seconds
     */
    public void setRemainingTime(float time) {
        this.remainingTime = time;
    }



    /**
     * Creates a new HUD instance.
     *
     * Initializes the HUD camera, end-of-game stage,
     * and basic layout structures.
     *
     * @param spriteBatch shared sprite batch
     * @param font        font used for text rendering
     * @param skin        UI skin for labels
     */
    public Hud(SpriteBatch spriteBatch, BitmapFont font, Skin skin) {
        this.spriteBatch = spriteBatch;
        this.font = font;
        this.camera = new OrthographicCamera();
        endStage = new Stage(new ScreenViewport());
        endTable = new Table();
        endTable.setFillParent(true);
        endStage.addActor(endTable);
        this.skin = skin;


    }

    /**
     * Renders the in-game HUD.
     *
     * Displays controls hints, remaining time,
     * tool states, and exit unlock status.
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
            font.setColor(Color.GRAY);
            font.draw(spriteBatch, "Shovel", 10, y);
            y -= 20;
            font.setColor(Color.WHITE);
        }
        if (player.isFertilizerActive()) {
            font.setColor(Color.BROWN);
            font.draw(spriteBatch, "Fertilizer active!", 10, y);
            y -= 20;
            font.setColor(Color.WHITE);
        }

        if (player.isWateringCanActive()) {
            font.setColor(Color.BLUE);
            font.draw(spriteBatch, "Watering Can active!", 10, y);
            y -= 20;
            font.setColor(Color.WHITE);
        }
        if (map.isExitUnlocked()) {
            font.setColor(Color.GREEN);
            font.draw(spriteBatch, "Exit unlocked", 10, y);
            font.setColor(Color.WHITE);
        } else {
            font.setColor(Color.RED);
            font.draw(spriteBatch, "Exit locked (" + map.getHarvestedCount()+ "/" + map.getQuota() + ")", 10, y);
            font.setColor(Color.WHITE);
        }




        spriteBatch.end();

    }


    /**
     * Updates the HUD camera when the window size changes.
     *
     * @param width  new screen width
     * @param height new screen height
     */
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    /**
     * Renders the end-of-game overlay.
     *
     * Displays victory or game-over message,
     * star rating (on victory), and return hint.
     *
     * @param state final game state
     */
    public void renderEndMessage(GameState state) {
        endStage.getViewport().apply();

        endTable.clear();

        String titleText = (state == GameState.VICTORY)
                ? "YOU WIN!"
                : "GAME OVER";

        Label title = new Label(titleText, skin, "title");
        title.setAlignment(Align.center);

        Label subtitle = new Label(
                "Press ENTER to return to menu",
                skin
        );
        subtitle.setAlignment(Align.center);

        endTable.add(title).padBottom(15).row();

        if (state == GameState.VICTORY) {
            String stars = computeStars(remainingTime, TOTAL_TIME);

            Label starLabel = new Label(
                    stars,
                    skin,
                    "title"   // reuse big font
            );
            starLabel.setFontScale(1.5f);

            // GOLD color
            starLabel.setColor(1f, 0.85f, 0.2f, 1f);
            starLabel.setAlignment(Align.center);


            endTable.add(starLabel).padBottom(15).row();
        }

        endTable.add(subtitle);


        endStage.act();
        endStage.draw();
    }

    /**
    * Assigns the map reference used by the HUD.
            *
            * @param map current game map
     */
    public void setMap(GameMap map) {
        this.map = map;
    }



    /**
     * Computes a star rating based on remaining time.
     *
     * @param remainingTime time left at level end
     * @param totalTime     total level time
     * @return star string representation
     */
    private String computeStars(float remainingTime, float totalTime) {
        float ratio = remainingTime / totalTime;

        if (ratio >= 2f / 3f) {
            return "***";
        } else if (ratio >= 1f / 3f) {
            return "**-";
        } else if (ratio > 0f) {
            return "*--";
        } else {
            return "";
        }
    }




}
