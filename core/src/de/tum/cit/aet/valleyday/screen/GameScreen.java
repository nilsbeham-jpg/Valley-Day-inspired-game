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
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.Tile;
import de.tum.cit.aet.valleyday.map.TileObject;
import de.tum.cit.aet.valleyday.map.Wildlife.WildlifeBase;
import de.tum.cit.aet.valleyday.map.crops.CropTile;
import de.tum.cit.aet.valleyday.map.terrain.SoilType;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * Main gameplay screen.
 *
 * This screen is responsible for rendering the entire game world,
 * advancing the game state each frame, handling camera movement,
 * drawing HUD elements, and transitioning between playing, victory,
 * and game-over states.
 *
 * It does not own game logic itself, but orchestrates rendering and
 * delegates simulation updates to {@link GameMap}.
 */


public class GameScreen implements Screen {

    /**
     * 1 tile = 16 pixels (before SCALE).
     */
    public static final int TILE_SIZE_PX = 16; // your world logic thinks in tiles, your screen thinks in pixels 1 tile = 16 pixels
    
    /**
     * Render scale.
     */
    public static final int SCALE = 4; //SCALE = 4：把所有贴图放大 4 倍显示（16×16 变成 64×64），画面更大更清晰

    private final ValleyDayGame game;
    private final SpriteBatch spriteBatch;
    private final GameMap map;
    private final Hud hud;
    private final OrthographicCamera mapCamera;

    private GameState gameState = GameState.PLAYING;

    private float remainingTime = 320f; // remeber to change total time in HUD if you change this


    //fog of war
    private com.badlogic.gdx.graphics.Texture blackPixel;
    private static final int VISION_RADIUS = 3;      
    private static final float FOG_ALPHA = 1f;    
    private static final float EDGE_ALPHA = 0.45f;   
    // seconds

    private static final Color Play_Color = new Color (0.2f,0.5f,0.2f,1f);
    private static final Color WIN_Color = new Color (0.2f,0.6f,0.2f,1f);
    private static final Color LOSE_Color = new Color (0.6f,0.1f,0.1f,1f);
    private static final Color PATH_GREEN =
            new Color(0.55f, 0.55f, 0.50f, 1f);





    /**
     * Creates the gameplay screen and initializes camera and HUD.
     *
     * @param game main game instance
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
     * Main render loop, called once per frame.
     *
     * Handles input, updates game state, advances the map simulation,
     * and renders either the gameplay or end screens.
     *
     * @param deltaTime time since last frame in seconds
     */
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

        if (gameState != GameState.PLAYING) {
            switch (gameState) {
                case VICTORY -> MusicTrack.playExclusive(MusicTrack.VICTORY);
                case GAME_OVER -> MusicTrack.playExclusive(MusicTrack.GAME_OVER);
                case PLAYING -> MusicTrack.playExclusive(MusicTrack.BACKGROUND);
            }
        }


        // Clear the previous frame from the screen, or else the picture smears
        //ScreenUtils.clear(new Color(0.2f, 0.5f, 0.2f, 1f)); // dark green
        switch(gameState){
            case PLAYING->ScreenUtils.clear(Play_Color);
            case VICTORY->ScreenUtils.clear(WIN_Color);
            case GAME_OVER->ScreenUtils.clear(LOSE_Color);
        }
    

        float frameTime = Math.min(deltaTime, 0.250f);

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
        } else {
            hud.renderEndMessage(gameState);
        }

        hud.setPlayer(map.getPlayer());
    }



    /**
     * Updates the camera to match the current state of the game.
     * Currently, this just centers the camera at the origin.
     */
    private static final float DEAD_ZONE_FACTOR = 0.8f;


    /**
     * Updates the camera position to follow the player smoothly
     * while respecting a dead-zone.
     */

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


    /**
     * Renders the entire map including background, tiles,
     * crops, wildlife, player, and fog-of-war.
     */
    private void renderMap() {
        // This configures the spriteBatch to use the camera's perspective when rendering
        spriteBatch.setProjectionMatrix(mapCamera.combined);
        
        // Start drawing
        spriteBatch.begin();

        Tile[][] tiles = map.getTiles();

        // 1) Grass background with padding
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

        // 2) Non-farmland overlay
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
        spriteBatch.setColor(Color.WHITE);

        // 3) Tile objects (fence, debris, items, etc.)
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

        // 4) Crops (draw before wildlife, so wildlife stays on top)
        CropTile[][] crops = map.getCrops();
        if (crops != null) {
            for (int x = 0; x < map.getMapWidth(); x++) {
                for (int y = 0; y < map.getMapHeight(); y++) {

                    CropTile crop = crops[x][y];
                    if (crop == null) continue;

                    TextureRegion tex = crop.getTexture();
                    if (tex == null) continue;

                    float px = x * TILE_SIZE_PX * SCALE;
                    float py = y * TILE_SIZE_PX * SCALE;

                    spriteBatch.draw(
                            tex,
                            px,
                            py,
                            TILE_SIZE_PX * SCALE,
                            TILE_SIZE_PX * SCALE
                    );
                }
            }
        }

        // 5) Wildlife
    drawWildlife(spriteBatch);

// 6) Player
    draw(spriteBatch, map.getPlayer());

// 7) Vision mask (MUST be before end)
    drawVisionMask();

spriteBatch.end();

    }



    /**
     * Draws fog-of-war overlay based on player vision radius
     * and explored tiles.
     */
    private void drawVisionMask() {
        if (blackPixel == null) {
            return;
        }

        int px = map.worldToTile(map.getPlayer().getX());
        int py = map.worldToTile(map.getPlayer().getY());

        int r = VISION_RADIUS;
        int r2 = r * r;

        boolean[][] explored = map.getExplored();

        for (int x = 0; x < map.getMapWidth(); x++) {
            for (int y = 0; y < map.getMapHeight(); y++) {
                int dx = x - px;
                int dy = y - py;
                int d2 = dx * dx + dy * dy;

                float alpha;

                if (d2 <= r2) {

                    explored[x][y] = true;
                    continue;
                }

                if (explored[x][y]) {
                    alpha = 0.75f;
                } else {
                    alpha = 1.0f;
                }

                float drawX = x * TILE_SIZE_PX * SCALE;
                float drawY = y * TILE_SIZE_PX * SCALE;

                spriteBatch.setColor(0f, 0f, 0f, alpha);
                spriteBatch.draw(
                        blackPixel,
                        drawX,
                        drawY,
                        TILE_SIZE_PX * SCALE,
                        TILE_SIZE_PX * SCALE
                );
            }
        }

        spriteBatch.setColor(1f, 1f, 1f, 1f);
    }




    private float wildlifeDebugTimer = 0f;


    /**
     * Draws all visible wildlife entities.
     */
    private void drawWildlife(SpriteBatch batch) {
        int playerTileX = map.worldToTile(map.getPlayer().getX());
        int playerTileY = map.worldToTile(map.getPlayer().getY());
        int r = VISION_RADIUS;
        int r2 = r * r;

        float baseSize = TILE_SIZE_PX * SCALE;

        for (WildlifeBase w : map.getWildlife()) {
            if (!w.isAlive()) {
                continue;
            }

            // ONLY render wildlife if it's inside CURRENT vision circle
            int wx = w.getX();
            int wy = w.getY();
            int dx = wx - playerTileX;
            int dy = wy - playerTileY;
            if (dx * dx + dy * dy > r2) {
                continue;
            }

            TextureRegion tex = w.getCurrentAppearance();
            if (tex == null) {
                continue;
            }

            float drawW = baseSize;
            float drawH = baseSize;

            // 🐌 snail is smaller
            if (w.getClass().getSimpleName().contains("Snail")) {
                drawW = baseSize * 0.6f;
                drawH = baseSize * 0.6f;
            }

            // center the sprite in the tile (use smooth render position)
            float px = w.getRenderX() * baseSize + (baseSize - drawW) * 0.5f;
            float py = w.getRenderY() * baseSize + (baseSize - drawH) * 0.5f;

            batch.draw(tex, px, py, drawW, drawH);
        }
    }

    /**
     * Draws a drawable entity aligned to tile center.
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

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }


    /**
     * Called when the screen becomes active.
     */
    @Override
    public void show() {
        MusicTrack.playExclusive(MusicTrack.BACKGROUND);

        mapCamera.setToOrtho(false,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );

        float px = map.getPlayer().getX() * TILE_SIZE_PX * SCALE;
        float py = map.getPlayer().getY() * TILE_SIZE_PX * SCALE;

        mapCamera.position.set(px, py, 0);
        mapCamera.update();
        //fog of war
        if (blackPixel == null) {
    com.badlogic.gdx.graphics.Pixmap pm =
            new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
    pm.setColor(0f, 0f, 0f, 1f);
    pm.fill();
    blackPixel = new com.badlogic.gdx.graphics.Texture(pm);
    pm.dispose();
}



    }

    @Override
    public void hide() {
    }


    /**
     * Disposes GPU resources used by this screen.
     */
    @Override
    public void dispose() {
        if (blackPixel != null) {
            blackPixel.dispose();
            blackPixel = null;
        }
    }

}
