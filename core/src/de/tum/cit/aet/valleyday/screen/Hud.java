package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import de.tum.cit.aet.valleyday.GameState;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.player.Player;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * HUD shows simple game info on screen (top-left).
 * It uses Scene2D Table so it looks tidy.
 */
public class Hud {

    private final SpriteBatch spriteBatch;
    private final BitmapFont font; // kept for compatibility
    private final Skin skin;

    private GameMap map;
    private Player player;
    private float remainingTime = 0f;

    // ---------- HUD UI (top-left) ----------
    private final Stage hudStage;
    private final Table hudTable;

    private final Label hintLabel;
    private final Label timeLabel;
    private final Label toolsTitleLabel;
    private final Label exitLabel;

    // tools (icons + text, always visible)
    private final Image shovelIcon;
    private final Image fertilizerIcon;
    private final Image wateringIcon;

    private final Label shovelLabel;
    private final Label fertilizerLabel;
    private final Label wateringLabel;

    // ---------- END UI (center) ----------
    private final Stage endStage;
    private final Table endTable;

    private static final float TOTAL_TIME = 320f;

    /**
     * Create a new HUD.
     *
     * @param spriteBatch shared batch
     * @param font        font (not required, but keep it)
     * @param skin        UI skin
     */
    public Hud(SpriteBatch spriteBatch, BitmapFont font, Skin skin) {
        this.spriteBatch = spriteBatch;
        this.font = font;
        this.skin = skin;

        // Normal HUD stage
        this.hudStage = new Stage(new ScreenViewport(), spriteBatch);
        this.hudTable = new Table();
        this.hudTable.setFillParent(true);
        this.hudStage.addActor(hudTable);

        // Labels (use styles from skin)
        hintLabel = new Label("Press Esc to Pause!", skin, "default");
        timeLabel = new Label("Time left: 0", skin, "default");
        toolsTitleLabel = new Label("Tools:", skin, "bold");
        exitLabel = new Label("", skin, "default");

        // IMPORTANT:
        // Use "default" here, not "dim".
        // Because Scene2D multiplies label color with style fontColor.
        shovelLabel = new Label("Shovel", skin, "default");
        fertilizerLabel = new Label("Fertilizer", skin, "default");
        wateringLabel = new Label("Watering Can", skin, "default");

        // Icons
        shovelIcon = new Image(new TextureRegionDrawable(Textures.SHOVEL));
        fertilizerIcon = new Image(new TextureRegionDrawable(Textures.FERTILIZER));
        wateringIcon = new Image(new TextureRegionDrawable(Textures.WATERCAN));

        // Bigger icons
        float iconSize = 32f;

        // Layout: top-left
        hudTable.top().left();
        hudTable.pad(10);

        // Slightly smaller text so it matches icons better
        hintLabel.setFontScale(0.9f);
        timeLabel.setFontScale(0.95f);
        toolsTitleLabel.setFontScale(0.95f);
        exitLabel.setFontScale(0.95f);

        shovelLabel.setFontScale(0.9f);
        fertilizerLabel.setFontScale(0.9f);
        wateringLabel.setFontScale(0.9f);

        hudTable.add(hintLabel).left().row();
        hudTable.add(timeLabel).left().padTop(4).row();

        // Tools block (aligned rows)
        Table toolsBlock = new Table();
        toolsBlock.left();

        toolsBlock.add(toolsTitleLabel).left().row();

        toolsBlock.add(shovelIcon).size(iconSize).left();
        toolsBlock.add(shovelLabel).left().padLeft(8).row();

        toolsBlock.add(fertilizerIcon).size(iconSize).left();
        toolsBlock.add(fertilizerLabel).left().padLeft(8).row();

        toolsBlock.add(wateringIcon).size(iconSize).left();
        toolsBlock.add(wateringLabel).left().padLeft(8).row();

        hudTable.add(toolsBlock).left().padTop(10).row();

        // Exit line
        hudTable.add(exitLabel).left().padTop(8).row();

        // End screen stage
        this.endStage = new Stage(new ScreenViewport(), spriteBatch);
        this.endTable = new Table();
        this.endTable.setFillParent(true);
        this.endStage.addActor(endTable);
    }

    /**
     * Set player for HUD.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Set map for HUD.
     */
    public void setMap(GameMap map) {
        this.map = map;
    }

    /**
     * Update remaining time.
     */
    public void setRemainingTime(float time) {
        this.remainingTime = time;
    }

    /**
     * Render normal HUD.
     */
    public void render() {
        updateHudState();

        hudStage.getViewport().apply();
        hudStage.act();
        hudStage.draw();
    }

    /**
     * Update HUD texts and colors.
     */
    private void updateHudState() {
        int secondsLeft = (int) Math.ceil(remainingTime);
        timeLabel.setText("Time left: " + secondsLeft);

        if (player != null) {
            // Shovel
            if (player.hasShovel()) {
                shovelLabel.setText("Shovel(AKTIV), Your faster!!");
                shovelLabel.setColor(Color.WHITE);
            } else {
                shovelLabel.setText("Shovel");
                shovelLabel.setColor(skin.getColor("gray"));
            }

            // Fertilizer
            if (player.isFertilizerActive()) {
                fertilizerLabel.setText("Fertilizer(AKTIV), Your plants have been fertilized once. ");
                fertilizerLabel.setColor(Color.WHITE);
            } else {
                fertilizerLabel.setText("Fertilizer");
                fertilizerLabel.setColor(skin.getColor("gray"));
            }

            // Watering Can
            if (player.isWateringCanActive()) {
                wateringLabel.setText("Watering Can(AKTIV), You used the Watering Can once.");
                wateringLabel.setColor(Color.WHITE);
            } else {
                wateringLabel.setText("Watering Can");
                wateringLabel.setColor(skin.getColor("gray"));
            }
        }

        if (map != null) {
            if (map.isExitUnlocked()) {
                exitLabel.setColor(Color.GREEN);
                exitLabel.setText("Exit unlocked");
            } else {
                exitLabel.setColor(Color.RED);
                exitLabel.setText("Exit locked (" + map.getHarvestedCount() + "/" + map.getQuota() + ")");
            }
        } else {
            exitLabel.setColor(Color.WHITE);
            exitLabel.setText("");
        }
    }

    /**
     * Resize HUD.
     */
    public void resize(int width, int height) {
        hudStage.getViewport().update(width, height, true);
        endStage.getViewport().update(width, height, true);
    }

    /**
     * Render end message overlay.
     */
    public void renderEndMessage(GameState state) {
        endStage.getViewport().apply();

        endTable.clear();

        String titleText = (state == GameState.VICTORY) ? "YOU WIN!" : "GAME OVER";

        Label title = new Label(titleText, skin, "title");
        title.setAlignment(Align.center);

        Label subtitle = new Label("Press ENTER to return to menu", skin, "default");
        subtitle.setAlignment(Align.center);

        endTable.add(title).padBottom(15).row();

        if (state == GameState.VICTORY) {
            String stars = computeStars(remainingTime, TOTAL_TIME);

            Label starLabel = new Label(stars, skin, "title");
            starLabel.setFontScale(1.5f);
            starLabel.setColor(1f, 0.85f, 0.2f, 1f);
            starLabel.setAlignment(Align.center);

            endTable.add(starLabel).padBottom(15).row();
        }

        endTable.add(subtitle);

        endStage.act();
        endStage.draw();
    }

    /**
     * Compute stars from remaining time.
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

    /**
     * Free resources.
     */
    public void dispose() {
        hudStage.dispose();
        endStage.dispose();
    }
}
