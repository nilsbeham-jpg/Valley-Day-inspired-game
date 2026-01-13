package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.map.Flowers;
import de.tum.cit.aet.valleyday.map.Tile;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {
    
    /**
     * The size of a grid cell in pixels.
     * This allows us to think of coordinates in terms of square grid tiles
     * (e.g. x=1, y=1 is the bottom left corner of the map)
     * rather than absolute pixel coordinates.
     */
    public static final int TILE_SIZE_PX = 16; // your world logic thinks in tiles, your screen thinks in pixels 1 tile = 16 pixels
    
    /**
     * The scale of the game.
     * This is used to make everything in the game look bigger or smaller.
     */
    public static final int SCALE = 4; //SCALE = 4：把所有贴图放大 4 倍显示（16×16 变成 64×64），画面更大更清晰

    private final ValleyDayGame game;
    private final SpriteBatch spriteBatch;
    private final GameMap map;
    private final Hud hud;
    private final OrthographicCamera mapCamera;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(ValleyDayGame game) {
        this.game = game;
        this.spriteBatch = game.getSpriteBatch();
        this.map = game.getMap();
        this.hud = new Hud(spriteBatch, game.getSkin().getFont("font"));
        // Create and configure the camera for the game view
        this.mapCamera = new OrthographicCamera(); //创建正交相机
        this.mapCamera.setToOrtho(false);
    }
    
    /**
     * The render method is called every frame to render the game.
     * @param deltaTime The time in seconds since the last render.
     *///deltaTime：上一帧到这一帧过去了多少秒（秒数）
    @Override
    public void render(float deltaTime) {
        // Check for escape key press to go back to the menu //按ESC到菜单界面
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu();
        }
        
        // Clear the previous frame from the screen, or else the picture smears
        ScreenUtils.clear(Color.BLACK); //清屏：避免“拖影/涂抹”
        
        // Cap frame time to 250ms to prevent spiral of death
        float frameTime = Math.min(deltaTime, 0.250f); //如果某一帧卡顿（比如 deltaTime=2秒），物理会需要做很多步来追赶，会导致更卡，进入“死亡螺旋”
        
        // Update the map state
        map.tick(frameTime);

        if (map.hasPlayerReachedExit()) {
            game.goToMenu(); // or game.goToWinScreen() later
            return;}
        
        // Update the camera
        updateCamera();
        
        // Render the map on the screen
        renderMap(); //把 flowers/chest/player 画到屏幕上



        
        // Render the HUD on the screen
        hud.render(); //HUD画在最上层
    }
    
    /**
     * Updates the camera to match the current state of the game.
     * Currently, this just centers the camera at the origin.
     */
    private void updateCamera() {
        mapCamera.setToOrtho(false); //Y轴向上
        mapCamera.position.x = 3.5f * TILE_SIZE_PX * SCALE;
        mapCamera.position.y = 3.5f * TILE_SIZE_PX * SCALE;
        mapCamera.update(); // This is necessary to apply the changes
    }
    
    private void renderMap() {
        // This configures the spriteBatch to use the camera's perspective when rendering
        spriteBatch.setProjectionMatrix(mapCamera.combined);
        
        // Start drawing
        spriteBatch.begin();

        Tile[][] tiles = map.getTiles();

        for (int x = 0; x < map.getMapWidth(); x++) {
            for (int y = 0; y < map.getMapHeight(); y++) {
                Tile tile = tiles[x][y];

                TextureRegion texture = switch (tile.type) {
                    case FENCE -> Textures.FENCE;
                    case DEBRIS -> Textures.DEBRIS;
                    case EXIT -> Textures.EXIT;
                    default -> null;
                };

                if (texture != null) {
                    float drawX = x * TILE_SIZE_PX * SCALE;
                    float drawY = y * TILE_SIZE_PX * SCALE;
                    spriteBatch.draw(
                            texture,
                            drawX,
                            drawY,
                            TILE_SIZE_PX * SCALE,
                            TILE_SIZE_PX * SCALE
                    );
                }
            }
        }





        // Render everything in the map here, in order from lowest to highest (later things appear on top)
        // You may want to add a method to GameMap to return all the drawables in the correct order
        //画画面的顺序 先花后箱子后玩家

        draw(spriteBatch, map.getPlayer());
        
        // Finish drawing, i.e. send the drawn items to the graphics card
        spriteBatch.end();
    }
    
    /**
     * Draws this object on the screen.
     * The texture will be scaled by the game scale and the tile size.
     * This should only be called between spriteBatch.begin() and spriteBatch.end(), e.g. in the renderMap() method.
     * @param spriteBatch The SpriteBatch to draw with.
     */
    private static void draw(SpriteBatch spriteBatch, Drawable drawable) {
        TextureRegion texture = drawable.getCurrentAppearance();
        // Drawable coordinates are in tiles, so we need to scale them to pixels
        float x = drawable.getX() * TILE_SIZE_PX * SCALE;
        float y = drawable.getY() * TILE_SIZE_PX * SCALE;
        // Additionally scale everything by the game scale
        float width = texture.getRegionWidth() * SCALE; //让贴图跟地图一致缩放
        float height = texture.getRegionHeight() * SCALE;
        spriteBatch.draw(texture, x, y, width, height);
    }
    
    /**
     * Called when the window is resized.
     * This is where the camera is updated to match the new window size.
     * @param width The new window width.
     * @param height The new window height.
     */
    @Override
    public void resize(int width, int height) {
        mapCamera.setToOrtho(false); //当窗口大小变化，HUD 需要调整 viewport 或布局
        hud.resize(width, height);
    }

    // Unused methods from the Screen interface
    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

}
