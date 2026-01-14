package de.tum.cit.aet.valleyday.map.player;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.crops.CropStage;
import de.tum.cit.aet.valleyday.map.crops.CropTile;
import de.tum.cit.aet.valleyday.texture.Animations;

public class WildlifeVisitor {

    private int x;
    private int y;

    private boolean alive = true;

    // Animation timing
    private float animTime = 0f;

    // Movement timing
    private float moveTimer = 0f;

    // Two speeds: wander vs chase
    private static final float WANDER_INTERVAL = 0.7f;
    private static final float CHASE_INTERVAL = 0.5f;

    // “视野”：看到成熟作物才追（你想全图追就改成 999）
    private static final int VISION_RANGE = 10;

    //记住上一次移动方向，避免来回抖动
    private int lastDx = 0;
    private int lastDy = 0;

    private enum State { WANDER, CHASE }
    private State state = State.WANDER;

    // 追踪目标
    private int targetX = -1;
    private int targetY = -1;

    public WildlifeVisitor(int x, int y) {
        this.x = x;
        this.y = y;
        this.moveTimer = MathUtils.random(0f, WANDER_INTERVAL); // 不同鸡错开移动
    }

    public boolean isAlive() {
        return alive;
    }

    public void despawn() {
        alive = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public TextureRegion getCurrentAppearance() {
        return Animations.CHICKEN_WALK.getKeyFrame(animTime, true);
    }

    public void tick(float dt, GameMap map) {
        if (!alive) {
            return;
        }

        animTime += dt;

        // 每次 tick 都检查是否撞到玩家（即使没移动也能判定）
        checkPlayerCollision(map);

        moveTimer -= dt;
        if (moveTimer > 0f) {
            return;
        }

        // 1) 决定是否进入追逐状态
        int[] seen = map.findNearestMatureCrop(x, y, VISION_RANGE);
        if (seen != null) {
            state = State.CHASE;
            targetX = seen[0];
            targetY = seen[1];
        } else {
            state = State.WANDER;
            targetX = -1;
            targetY = -1;
        }

        // 2) 根据状态设置速度
        moveTimer = (state == State.CHASE) ? CHASE_INTERVAL : WANDER_INTERVAL;

        // 3) 执行一步移动
        if (state == State.CHASE) {
            moveChaseOneStep(map);
        } else {
            moveWanderOneStep(map);
        }

        // 4) 到达后偷成熟作物
        CropTile crop = map.getCropAt(x, y);
        if (crop != null && crop.getStage() == CropStage.MATURE) {
            crop.harvest();
        }

        // 5) 移动后再判一次玩家碰撞（更及时）
        checkPlayerCollision(map);
    }

    private void moveChaseOneStep(GameMap map) {
        if (targetX < 0) {
            return;
        }

        int[] next = map.nextStepBfs(x, y, targetX, targetY);
        if (next == null) {
            // 找不到路就退回 wander（避免卡住一直抖）
            moveWanderOneStep(map);
            state = State.WANDER;
            return;
        }

        int nx = next[0];
        int ny = next[1];

        lastDx = nx - x;
        lastDy = ny - y;

        x = nx;
        y = ny;
    }

    private void moveWanderOneStep(GameMap map) {
        int[][] dirs = new int[][]{{1,0},{-1,0},{0,1},{0,-1}};

        // 先尝试不反向的方向（避免左右抖/上下抖）
        for (int tries = 0; tries < 8; tries++) {
            int i = MathUtils.random(3);
            int dx = dirs[i][0];
            int dy = dirs[i][1];

            // 50% 概率避免立刻走回头路
            if (dx == -lastDx && dy == -lastDy && MathUtils.randomBoolean(0.7f)) {
                continue;
            }

            int nx = x + dx;
            int ny = y + dy;

            if (map.isBlocked(nx, ny)) {
                continue;
            }

            lastDx = dx;
            lastDy = dy;

            x = nx;
            y = ny;
            return;
        }

        // 实在走不了就不动
        lastDx = 0;
        lastDy = 0;
    }

    private void checkPlayerCollision(GameMap map) {
        int px = map.worldToTile(map.getPlayer().getX());
        int py = map.worldToTile(map.getPlayer().getY());
        if (px == x && py == y) {
            map.loseByWildlife();
        }
    }
}
