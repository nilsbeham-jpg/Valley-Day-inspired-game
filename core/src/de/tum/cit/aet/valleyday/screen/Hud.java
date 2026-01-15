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
    private GameMap map;

    private float remainingTime= 0f;
    private Player player;
    private Stage endStage;
    private Table endTable;
    private final Skin skin;
    private Label difficultyLabel;
    private static final float TOTAL_TIME = 320f;




    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setRemainingTime(float time) {
        this.remainingTime = time;
    }


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
     * Resizes the HUD when the screen size changes.
     * This is called when the window is resized.
     * @param width The new width of the screen.
     * @param height The new height of the screen.
     */ //Hud 随窗口变化
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

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

    public void setMap(GameMap map) {
        this.map = map;
    }


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
