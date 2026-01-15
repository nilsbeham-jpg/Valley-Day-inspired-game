package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import de.tum.cit.aet.valleyday.GameState;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.audio.MusicTrack;
import de.tum.cit.aet.valleyday.map.*;
import de.tum.cit.aet.valleyday.map.crops.CropStage;
import de.tum.cit.aet.valleyday.map.crops.CropTile;
import de.tum.cit.aet.valleyday.map.player.WildlifeVisitor;
import de.tum.cit.aet.valleyday.map.structures.Exit;
import de.tum.cit.aet.valleyday.map.terrain.Debris;
import de.tum.cit.aet.valleyday.map.terrain.Fence;
import de.tum.cit.aet.valleyday.map.terrain.SoilType;
import de.tum.cit.aet.valleyday.texture.Drawable;
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

    private GameState gameState = GameState.PLAYING;

    private float remainingTime = 320f; // remeber to change total time in HUD if you change this

// seconds

    private static final Color Play_Color = new Color (0.2f,0.5f,0.2f,1f);
    private static final Color WIN_Color = new Color (0.2f,0.6f,0.2f,1f);
    private static final Color LOSE_Color = new Color (0.6f,0.1f,0.1f,1f);
    private static final Color PATH_GREEN =
            new Color(0.55f, 0.55f, 0.50f, 1f);










    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(ValleyDayGame game) {
        this.game = game;
        this.spriteBatch = game.getSpriteBatch();
        this.map = game.getMap();
        this.hud = new Hud(spriteBatch, game.getSkin().getFont("font"), game.getSkin());
        hud.setPlayer(map.getPlayer());
        hud.setMap(map);
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
            game.setScreen(new MenuScreen(game, true));
            return;
        }

        if (gameState != GameState.PLAYING &&
                Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new MenuScreen(game, false));
            return;
        }
        if (gameState != GameState.PLAYING){
            switch (gameState) {
                case VICTORY -> MusicTrack.playExclusive(MusicTrack.VICTORY);
                case GAME_OVER -> MusicTrack.playExclusive(MusicTrack.GAME_OVER);
                case PLAYING -> MusicTrack.playExclusive(MusicTrack.BACKGROUND);
            }
        }


        // Clear the previous frame from the screen, or else the picture smears
        //ScreenUtils.clear(new Color(0.2f, 0.5f, 0.2f, 1f)); // dark green
        //清屏：避免“拖影/涂抹”
        switch(gameState){
            case PLAYING->ScreenUtils.clear(Play_Color);
            case VICTORY->ScreenUtils.clear(WIN_Color);
            case GAME_OVER->ScreenUtils.clear(LOSE_Color);
        }
    



        // Cap frame time to 250ms to prevent spiral of death
        float frameTime = Math.min(deltaTime, 0.250f); //如果某一帧卡顿（比如 deltaTime=2秒），物理会需要做很多步来追赶，会导致更卡，进入“死亡螺旋”
        
        

        
        if (gameState == GameState.PLAYING) {
        map.tick(frameTime);

    
    if (map.isLostByWildlife()) { //meet wildlife= gameover
        remainingTime = 0f;
        gameState = GameState.GAME_OVER;
        return; 
    }

 
    remainingTime -= frameTime; //remainingTime empty=gameover
    if (remainingTime <= 0f) {
        remainingTime = 0f;
        gameState = GameState.GAME_OVER;
    }
}



        if (map.hasPlayerReachedExit() && gameState == GameState.PLAYING) {
            gameState = GameState.VICTORY;
        }

        if (gameState == GameState.PLAYING) {
            updateCamera();
            renderMap();
            hud.setRemainingTime(remainingTime);
            hud.render();
        }
        else {
            hud.renderEndMessage(gameState);
        }


        hud.setPlayer(map.getPlayer());



    }



    /**
     * Updates the camera to match the current state of the game.
     * Currently, this just centers the camera at the origin.
     */
    private static final float DEAD_ZONE_FACTOR = 0.8f;

    private void updateCamera() {
        float playerX = map.getPlayer().getX() * TILE_SIZE_PX * SCALE;
        float playerY = map.getPlayer().getY() * TILE_SIZE_PX * SCALE;

        float halfW = mapCamera.viewportWidth * 0.5f;
        float halfH = mapCamera.viewportHeight * 0.5f;

        float deadW = halfW * DEAD_ZONE_FACTOR;
        float deadH = halfH * DEAD_ZONE_FACTOR;

        float camX = mapCamera.position.x;
        float camY = mapCamera.position.y;

        if (playerX < camX - deadW) camX = playerX + deadW;
        if (playerX > camX + deadW) camX = playerX - deadW;
        if (playerY < camY - deadH) camY = playerY + deadH;
        if (playerY > camY + deadH) camY = playerY - deadH;

        mapCamera.position.set(camX, camY, 0);
        mapCamera.update();
    }

    private static final int GRASS_PADDING = 10; // tiles


    private void renderMap() {
        // This configures the spriteBatch to use the camera's perspective when rendering
        spriteBatch.setProjectionMatrix(mapCamera.combined);
        
        // Start drawing
        spriteBatch.begin();

        
        Tile[][] tiles = map.getTiles();


        // DRAW GRASS BACKGROUND WITH PADDING
        for (int x = -GRASS_PADDING; x < map.getMapWidth() + GRASS_PADDING; x++) {
            for (int y = -GRASS_PADDING; y < map.getMapHeight() + GRASS_PADDING; y++) {

                float drawX = x * TILE_SIZE_PX * SCALE;
                float drawY = y * TILE_SIZE_PX * SCALE;

                spriteBatch.draw(
                        Textures.GRASS,
                        drawX,
                        drawY,
                        TILE_SIZE_PX * SCALE,
                        TILE_SIZE_PX * SCALE
                );
            }
        }

        // DRAW NON-FARMLAND AS DARKENED GRASS
        spriteBatch.setColor(PATH_GREEN);

        for (int x = 0; x < map.getMapWidth(); x++) {
            for (int y = 0; y < map.getMapHeight(); y++) {

                Tile tile = tiles[x][y];
                if (tile.getSoilType() != SoilType.NON_FARMLAND) {
                    continue;
                }

                float drawX = x * TILE_SIZE_PX * SCALE;
                float drawY = y * TILE_SIZE_PX * SCALE;

                spriteBatch.draw(
                        Textures.GRASS,
                        drawX,
                        drawY,
                        TILE_SIZE_PX * SCALE,
                        TILE_SIZE_PX * SCALE
                );
            }
        }

// IMPORTANT: reset color!
        spriteBatch.setColor(Color.WHITE);










        for (int x = 0; x < map.getMapWidth(); x++) {
            for (int y = 0; y < map.getMapHeight(); y++) {
                Tile tile = tiles[x][y];
                TileObject obj = tile.getObject();

                if (obj == null) continue;

                TextureRegion texture = obj.getTexture();
                if (texture == null) continue; //invisible object


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

//RRAW Chicken
for (WildlifeVisitor w : map.getWildlife()) {
    TextureRegion tex = w.getCurrentAppearance();

    float px = w.getRenderX() * TILE_SIZE_PX * SCALE;
    float py = w.getRenderY() * TILE_SIZE_PX * SCALE;

    spriteBatch.draw(tex, px, py, TILE_SIZE_PX * SCALE, TILE_SIZE_PX * SCALE);
}






        // Render everything in the map here, in order from lowest to highest (later things appear on top)
        // You may want to add a method to GameMap to return all the drawables in the correct order
        //画画面的顺序 先花后箱子后玩家

        //DRAW CROPS
        CropTile[][] crops = map.getCrops();
        if (crops != null) {
            for (int x = 0; x < map.getMapWidth(); x++) {
                for (int y = 0; y < map.getMapHeight(); y++) {

                    CropTile crop = crops[x][y];
                    if (crop == null) {
                        continue;
                    }

                    TextureRegion tex = crop.getTexture();
                    if (tex == null) {
                        continue;
                    }

                    float px = x * TILE_SIZE_PX * SCALE;
                    float py = y * TILE_SIZE_PX * SCALE;

                    spriteBatch.draw(
                            tex,
                            px,
                            py,
                            TILE_SIZE_PX * SCALE,
                            TILE_SIZE_PX * SCALE);


    

        }
    }
}




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

        float worldX = drawable.getX() - 0.5f;
        float worldY = drawable.getY() - 0.5f;

        float x = worldX * TILE_SIZE_PX * SCALE;
        float y = worldY * TILE_SIZE_PX * SCALE;

        float width  = TILE_SIZE_PX * SCALE;
        float height = TILE_SIZE_PX * SCALE;

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
        mapCamera.viewportWidth = width;
        mapCamera.viewportHeight = height;
        mapCamera.update();

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
        MusicTrack.playExclusive(MusicTrack.BACKGROUND);
        // Set viewport size in WORLD UNITS (pixels in your case)
        mapCamera.setToOrtho(false,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );

        // Center camera on player ONCE
        float px = map.getPlayer().getX() * TILE_SIZE_PX * SCALE;
        float py = map.getPlayer().getY() * TILE_SIZE_PX * SCALE;

        mapCamera.position.set(px, py, 0);
        mapCamera.update();
    }


    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

}
