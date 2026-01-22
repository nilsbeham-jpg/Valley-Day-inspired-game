package de.tum.cit.aet.valleyday;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.tum.cit.aet.valleyday.audio.MusicTrack;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.screen.GameScreen;
import de.tum.cit.aet.valleyday.screen.MenuScreen;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

/**
 * Central entry point and lifecycle manager of the game.
 *
 * This class owns all global resources such as the {@link SpriteBatch},
 * UI {@link Skin}, the current {@link GameMap}, and handles transitions
 * between different screens (menu, gameplay, etc.).
 *
 * It also stores global configuration such as the selected difficulty
 * and the currently chosen map file.
 */
public class ValleyDayGame extends Game {

    /**
     * Sprite Batch for rendering game elements.
     * This eats a lot of memory, so we only want one of these.
     */
    private SpriteBatch spriteBatch; //贴图绘制核心对象

    /** The game's UI skin. This is used to style the game's UI elements. */
    private Skin skin; //UI样式


    private Difficulty difficulty = Difficulty.NORMAL;//Difficulty system;
    /**
     * The file chooser for loading map files from the user's computer.
     * This will give you access to a {@link com.badlogic.gdx.files.FileHandle} object,
     * which you can use to read the contents of the map file as a String, and then parse it into a {@link GameMap}.
     */
    private final NativeFileChooser fileChooser; //用于从用户电脑选择文件（比如加载 map 文件）
    
    /**
     * The map. This is where all the game objects are stored.
     * This is owned by {@link ValleyDayGame} and not by {@link GameScreen}
     * because the map should not be destroyed if we temporarily switch to another screen.
     */
    private GameMap map;

    private GameScreen currentGameScreen;
    private String selectedMapPath = "maps/map-1.properties"; // default



    /**
     * Constructor for ValleyDayGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environment.
     */
    public ValleyDayGame(NativeFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     * During the class constructor, libGDX is not fully initialized yet.
     * Therefore this method serves as a second constructor for the game,
     * and we can use libGDX resources here.
     */
    @Override
    public void create() {
        this.spriteBatch = new SpriteBatch(); // Create SpriteBatch for rendering
        this.skin = new Skin(Gdx.files.internal("skin/craftacular/craftacular-ui.json")); // Load UI skin
        this.map = new GameMap(this, selectedMapPath); // Create a new game map (you should change this to load the map from a file instead)

        goToMenu(); // Navigate to the menu screen
    }//

    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {
        this.setScreen(new MenuScreen(this, false));
    }


    /**
     * Switches to the game screen.
     */
    public void goToGame() {
        map = new GameMap(this, selectedMapPath);             // NEW world
        this.currentGameScreen = new GameScreen(this); // NEW screen
        this.setScreen(currentGameScreen);
    }


    /**
     * Resumes the currently paused game, if one exists.
     */
    public void resumeGame() {
        if (currentGameScreen != null) {
            this.setScreen(currentGameScreen); // SAME screen
        }
    }


    /** Returns the skin for UI elements. */
    public Skin getSkin() {
        return skin;
    }

    /** Returns the main SpriteBatch for rendering. */
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
    
    /** Returns the current map, if there is one. */
    public GameMap getMap() {
        return map;
    }



    /**
     * Sets the active difficulty.
     *
     * @param difficulty new difficulty level
     */
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * @return the currently selected difficulty
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }



    /**
     * Overrides screen switching to control disposal behavior.
     *
     * Screens are disposed automatically when replaced, except for
     * the active {@link GameScreen}, which may be resumed later.
     *
     * @param screen new screen to display
     */
    @Override
    public void setScreen(Screen screen) {
        Screen previousScreen = super.screen;
        super.setScreen(screen);
        // Only dispose if it is NOT the game screen we want to resume
        if (previousScreen != null && previousScreen != currentGameScreen) {
            previousScreen.dispose();
        }
    }

    /** Cleans up resources when the game is disposed. */
    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        skin.dispose(); // Dispose the skin
    }


    /**
     * Sets the path of the map file to be loaded next.
     *
     * @param path absolute or relative file path
     */
    public void setSelectedMapPath(String path) {
        this.selectedMapPath = path;
    }

    /**
     * @return the currently selected map path
     */
    public String getSelectedMapPath() {
        return selectedMapPath;
    }

    /**
     * @return the native file chooser instance
     */
    public NativeFileChooser getFileChooser() {
        return fileChooser;
    }
    //public void startGameWithMap(String mapPath) {
     //   setScreen(new GameScreen(this, mapPath));
    //}


}
