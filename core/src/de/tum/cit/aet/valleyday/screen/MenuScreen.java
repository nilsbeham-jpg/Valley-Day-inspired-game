package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.aet.valleyday.Difficulty;
import de.tum.cit.aet.valleyday.ValleyDayGame;

import de.tum.cit.aet.valleyday.audio.MusicTrack;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;


import java.io.File;
import java.io.FilenameFilter;



/**
 * Main menu screen of the game.
 *
 * This screen is responsible for presenting the start menu to the player.
 * It allows starting a new game, resuming an existing one, loading a custom
 * map from disk, selecting the difficulty, and exiting the application.
 *
 * The MenuScreen itself does not contain game logic; it only configures
 * global game state via {@link ValleyDayGame} and triggers screen transitions.
 */

public class MenuScreen implements Screen {

    private final Stage stage; // root container of everything UI-related.
    private final boolean canResume;
    private Difficulty selectedDifficulty;
    private Texture menuBgTex;
    private Image menuBgImg;
    private Table table;



    /**
     * Creates the menu screen and sets up all UI elements.
     *
     * This includes buttons for starting/resuming the game,
     * loading a map, difficulty selection, and exiting.
     *
     * @param game      main game instance
     * @param canResume whether an existing game can be resumed
     */
    public MenuScreen(ValleyDayGame game, boolean canResume) {
        this.canResume = canResume;
        this.selectedDifficulty = game.getDifficulty();
        var camera = new OrthographicCamera();
        camera.zoom = 1.5f; // Set camera zoom for a closer view 创建相机 Zoom代表视角远近

        Viewport viewport = new ScreenViewport(camera); // Create a viewport with the camera 负责把“世界坐标”映射到“屏幕像素”
        stage = new Stage(viewport, game.getSpriteBatch()); // Create a stage for UI elements

        table = new Table(); // Create a table for layout table is a layout manager.
        table.setFillParent(true); // Make the table fill the stage
        stage.addActor(table); // Add the table to the stage

        // Add a label as a title
        table.add(new Label("Valley Day", game.getSkin(), "title")).padBottom(80).row();
      

        // Create and add a button to go to the game screen
        TextButton goToGameButton = new TextButton("Start new Game", game.getSkin());
        table.add(goToGameButton).width(300).row();
        goToGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToGame(); // Change to the game screen when button is pressed
            }
        });
        // Resume Game (only if allowed)
        if (canResume) {
            TextButton resumeButton = new TextButton("Resume Game", game.getSkin());
            table.add(resumeButton).width(300).padBottom(20).row();

            resumeButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.resumeGame(); // SAME world
                }
            });
        }
        TextButton loadMapButton = new TextButton("Load Map", game.getSkin());
        table.add(loadMapButton).width(300).padBottom(20).row();
        loadMapButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                NativeFileChooserConfiguration config =
                        new NativeFileChooserConfiguration();

                config.title = "Select map file";
                config.directory = Gdx.files.absolute(System.getProperty("user.home"));
                config.nameFilter = (dir, name) -> name.endsWith(".properties");

                game.getFileChooser().chooseFile(config,
                        new NativeFileChooserCallback() {

                            @Override
                            public void onFileChosen(FileHandle file) {
                                String path = file.file().getAbsolutePath();

                                // 🔑 THIS is the key integration point
                                game.setSelectedMapPath(path);
                                game.goToGame();
                            }

                            @Override
                            public void onCancellation() {
                                System.out.println("Map loading cancelled");
                            }

                            @Override
                            public void onError(Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                );
            }
        });

        TextButton exitButton = new TextButton("Exit Game", game.getSkin());
        table.add(exitButton).width(300).padBottom(20).row();

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        Label difficultyLabel = new Label("Difficulty", game.getSkin());
        table.add(difficultyLabel).padBottom(10).row();

        Table diffTable = new Table();
        table.add(diffTable).padBottom(30).row();
        TextButton easyBtn   = new TextButton("Easy", game.getSkin());
        TextButton normalBtn = new TextButton("Normal", game.getSkin());
        TextButton hardBtn   = new TextButton("Hard", game.getSkin());

        ButtonGroup<TextButton> difficultyGroup = new ButtonGroup<>(easyBtn, normalBtn, hardBtn);
        difficultyGroup.setMinCheckCount(1);
        difficultyGroup.setMaxCheckCount(1);
        difficultyGroup.setUncheckLast(true);


        diffTable.add(easyBtn).width(120).pad(5);
        diffTable.add(normalBtn).width(120).pad(5);
        diffTable.add(hardBtn).width(120).pad(5);

        easyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectDifficulty(game, Difficulty.EASY, easyBtn, normalBtn, hardBtn);
            }
        });

        normalBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectDifficulty(game, Difficulty.NORMAL, easyBtn, normalBtn, hardBtn);
            }
        });

        hardBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectDifficulty(game, Difficulty.HARD, easyBtn, normalBtn, hardBtn);
            }
        });

        selectDifficulty(game, selectedDifficulty, easyBtn, normalBtn, hardBtn);

        



    




    }

    /**
     * Renders the menu screen.
     *
     * Clears the screen and updates/draws the stage.
     *
     * @param deltaTime time since last frame in seconds
     */
    @Override
    public void render(float deltaTime) {
        float frameTime = Math.min(deltaTime, 0.250f); // Cap frame time to 250ms to prevent spiral of death        ScreenUtils.clear(Color.BLACK);
        ScreenUtils.clear(Color.BLACK);
        stage.act(frameTime); // Update the stage
        stage.draw(); // Draw the stage
    }
    
    /**
     * Resize the stage when the screen is resized.
     * @param width The new width of the screen.
     * @param height The new height of the screen.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
    }

    @Override
    public void dispose() {
        // Dispose of the stage when screen is disposed 
        stage.dispose();
        if (menuBgTex != null) {
        menuBgTex.dispose();
    }

    }

    @Override
    
    public void show() {
    Gdx.input.setInputProcessor(stage);
    MusicTrack.playExclusive(MusicTrack.MENU);

    menuBgTex = new Texture("texture/Menubg.png");
    menuBgImg = new Image(menuBgTex);

    menuBgImg.setFillParent(true);
    menuBgImg.setScaling(Scaling.fill);
    menuBgImg.setAlign(com.badlogic.gdx.utils.Align.center);

    stage.addActor(menuBgImg);
    menuBgImg.toBack();
}


    // The following methods are part of the Screen interface but are not used in this screen.
    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    /**
     * Applies the selected difficulty to the game.
     *
     * @param game       main game instance
     * @param difficulty chosen difficulty
     * @param easy       easy button
     * @param normal     normal button
     * @param hard       hard button
     */
    private void selectDifficulty(ValleyDayGame game, Difficulty difficulty, TextButton easy, TextButton normal, TextButton hard) {

        this.selectedDifficulty = difficulty;
        game.setDifficulty(difficulty);

    }

}
