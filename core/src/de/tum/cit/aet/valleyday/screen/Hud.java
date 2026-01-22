package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.aet.valleyday.GameState;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.player.Player;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * Simple HUD (labels + icons).
 * It shows info, but it dont change game.
 */
public class Hud {

    private final Skin skin;
    private final Stage hudStage;
    private final Stage endStage;

    private GameMap map;
    private Player player;
    private float remainingTime = 0f;

    private static final float TOTAL_TIME = 320f;

    private final Label timeLeft;

    private final Label shovelText;
    private final Image shovelIcon;

    private final Label fertText;
    private final Label fertCount;
    private final Image fertIcon;

    private final Label waterText;
    private final Label waterCount;
    private final Image waterIcon;

    private final Label exitLabel;

    private final Label hintS;
    private final Label hintQ;
    private final Label hintE;
    private final Label hintA;
    private final Label hintD;
    private final Label hintArrows;

    private final Table endTable;

    private final Color dim = new Color(1f, 1f, 1f, 0.45f);
    private final Color sel = new Color(0.20f, 0.90f, 0.35f, 1f);

    /**
     * Make HUD.
     * @param font hud font
     * @param skin ui skin
     */
    public Hud(BitmapFont font, Skin skin) {
        this.skin = skin;

        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        

        this.hudStage = new Stage(new ScreenViewport());

        Table root = new Table();
        root.setFillParent(true);
        root.top().left().pad(10f);
        hudStage.addActor(root);

        root.add(new Label("Press Esc to Pause!", style)).left().colspan(3).row();
        timeLeft = new Label("Time left: 0", style);
        root.add(timeLeft).left().colspan(3).padBottom(8f).row();

        root.add(new Label("Tools:", style)).left().colspan(3).padTop(4f).row();

        shovelText = new Label("Shovel", style);
        shovelIcon = new Image(toDrawable(getShovelIcon()));
        applyImageVisible(shovelIcon);

        fertText = new Label("Fertilizer", style);
        fertCount = new Label("x0", style);
        fertIcon = new Image(toDrawable(Textures.FERTILIZER));

        waterText = new Label("Watering Can", style);
        waterCount = new Label("x0", style);
        waterIcon = new Image(toDrawable(Textures.WATERCAN));

        // layout: text | count | icon (icon near count)
        root.add(shovelText).left();
        root.add(new Label("", style)).width(60f);
        root.add(shovelIcon).size(22f).left().padLeft(8f).row();

        root.add(fertText).left();
        root.add(fertCount).width(60f).left().padLeft(10f);
        root.add(fertIcon).size(22f).left().padLeft(8f).row();

        root.add(waterText).left();
        root.add(waterCount).width(60f).left().padLeft(10f);
        root.add(waterIcon).size(22f).left().padLeft(8f).row();

        exitLabel = new Label("", style);
        root.add(exitLabel).left().colspan(3).padTop(10f).row();

        
        hintS = new Label("S  - attack wildlife", style);
        hintQ = new Label("Q  - switch tool", style);
        hintE = new Label("E  - use tool", style);
        hintA = new Label("A  - plant seed", style);
        hintD = new Label("D  - remove obstacle", style);
        hintArrows = new Label("Arrow Keys - move", style);

        root.add(hintS).left().colspan(3).padTop(20f).row();
        root.add(hintS).left().colspan(3).row();
        root.add(hintQ).left().colspan(3).row();
        root.add(hintE).left().colspan(3).row();
        root.add(hintA).left().colspan(3).row();
        root.add(hintD).left().colspan(3).row();
        root.add(hintArrows).left().colspan(3).row();

        this.endStage = new Stage(new ScreenViewport());
        this.endTable = new Table();
        this.endTable.setFillParent(true);
        endStage.addActor(endTable);
    }

    private TextureRegionDrawable toDrawable(TextureRegion r) {
        return r == null ? null : new TextureRegionDrawable(r);
    }

    private void applyImageVisible(Image img) {
        img.setVisible(img.getDrawable() != null);
    }

    private TextureRegion getShovelIcon() {
        try {
            return (TextureRegion) Textures.class.getField("SHOVEL").get(null);
        } catch (Exception e) {
            return null;
        }
    }

    /** Set player ref. */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /** Set map ref. */
    public void setMap(GameMap map) {
        this.map = map;
    }

    /** Set time left (sec). */
    public void setRemainingTime(float time) {
        this.remainingTime = time;
    }

    /** Draw HUD (every frame). */
    public void render() {
        updateUi();
        hudStage.act();
        hudStage.draw();
    }

    private void updateUi() {
        timeLeft.setText("Time left: " + (int) Math.ceil(remainingTime));

        if (player == null) {
            return;
        }

        boolean hasShovel = player.hasShovel();
        shovelText.setText(hasShovel ? "Shovel  \u2714" : "Shovel");
        shovelText.setColor(hasShovel ? Color.WHITE : skin.getColor("gray"));
        shovelIcon.setColor(hasShovel ? Color.WHITE : dim);

        int f = player.getFertilizerCount();
        int w = player.getWateringCanCount();

        fertCount.setText("x" + f);
        waterCount.setText("x" + w);

        boolean fertAvail = f > 0;
        boolean waterAvail = w > 0;

        setAvail(fertText, fertCount, fertIcon, fertAvail);
        setAvail(waterText, waterCount, waterIcon, waterAvail);

        Player.SelectedTool st = player.getSelectedTool();
        setSelected(st == Player.SelectedTool.FERTILIZER, fertText, fertCount, fertIcon);
        setSelected(st == Player.SelectedTool.WATERING_CAN, waterText, waterCount, waterIcon);

        if (map != null) {
            if (map.isExitUnlocked()) {
                exitLabel.setColor(Color.GREEN);
                exitLabel.setText("Exit unlocked");
            } else {
                exitLabel.setColor(Color.RED);
                exitLabel.setText("Exit locked (" + map.getHarvestedCount() + "/" + map.getQuota() + ")");
            }
        }
    }

    private void setAvail(Label t, Label c, Image i, boolean ok) {
        t.setColor(ok ? Color.WHITE : skin.getColor("gray"));
        c.setColor(ok ? Color.WHITE : skin.getColor("gray"));
        i.setColor(ok ? Color.WHITE : dim);
    }

    private void setSelected(boolean on, Label t, Label c, Image i) {
        if (on) {
            t.setColor(sel);
            c.setColor(sel);
            i.setColor(sel);
        }
    }

    /** Resize when window change. */
    public void resize(int width, int height) {
        hudStage.getViewport().update(width, height, true);
        endStage.getViewport().update(width, height, true);
    }

    /** Draw end text (win/lose). */
    public void renderEndMessage(GameState state) {
        endStage.getViewport().apply();
        endTable.clear();

        String titleText = (state == GameState.VICTORY) ? "YOU WIN!" : "GAME OVER";
        Label title = new Label(titleText, skin, "title");
        title.setAlignment(Align.center);
        endTable.add(title).padBottom(15).row();

        if (state == GameState.VICTORY) {
            Label stars = new Label(computeStars(remainingTime, TOTAL_TIME), skin, "title");
            stars.setFontScale(1.5f);
            stars.setColor(1f, 0.85f, 0.2f, 1f);
            endTable.add(stars).padBottom(15).row();
        }

        Label subtitle = new Label("Press ENTER to return to menu", skin);
        subtitle.setAlignment(Align.center);
        endTable.add(subtitle);

        endStage.act();
        endStage.draw();
    }

    private String computeStars(float r, float t) {
        float k = r / t;
        return k >= 2f / 3f ? "***" : k >= 1f / 3f ? "**-" : k > 0f ? "*--" : "";
    }

    /** Free stuffs. */
    public void dispose() {
        hudStage.dispose();
        endStage.dispose();
    }
}
