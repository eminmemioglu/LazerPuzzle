package com.mygame;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * 2-Player Cooperative Laser Puzzle Screen with:
 * - Fixed unidirectional laser emitter
 * - Triangular optical prism (splits laser into Red & Cyan beams)
 * - 2 Movable & rotatable mirrors
 * - 2 Color-matched target receivers (Red and Cyan)
 * - 2 Players (P1 Blue, P2 Green)
 */
public class GameScreen extends ScreenAdapter {
    public static final float WORLD_WIDTH = 1280f;
    public static final float WORLD_HEIGHT = 720f;
    private static final float GRID_SIZE = 64f;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;

    private final Player p1;
    private final Player p2;
    private final Prism prism;
    private final Array<Mirror> mirrors;
    private final Array<TargetReceiver> targets;
    private final TargetReceiver targetRed;
    private final TargetReceiver targetCyan;
    private final LaserSystem laserSystem;

    public GameScreen() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();

        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

        shapeRenderer = new ShapeRenderer();

        // 1. Two players
        p1 = Player.createPlayer1(200f, 260f);
        p2 = Player.createPlayer2(200f, 460f);

        // 2. Triangular optical prism (splits laser into 2 distinct colors)
        prism = new Prism(400f, 360f, 0f);

        // 3. Two movable and rotatable mirrors
        mirrors = new Array<>();
        mirrors.add(new Mirror(1, 680f, 520f, 135f, 90f));
        mirrors.add(new Mirror(2, 680f, 200f, 45f, 90f));

        // 4. Fixed unidirectional laser system (centered at left wall, shooting East)
        laserSystem = new LaserSystem();

        // 5. Two color-matched target receivers at the other end of the map
        targets = new Array<>();
        targetRed = new TargetReceiver(1140f, 560f, LaserSystem.COLOR_RED, "HEDEF 1 (KIRMIZI)");
        targetCyan = new TargetReceiver(1140f, 180f, LaserSystem.COLOR_CYAN, "HEDEF 2 (MAVİ)");
        targets.add(targetRed);
        targets.add(targetCyan);
    }

    @Override
    public void render(float delta) {
        // Clear background with deep dark slate
        ScreenUtils.clear(0.08f, 0.10f, 0.14f, 1f);

        // 1. Update target animations
        for (TargetReceiver tr : targets) {
            tr.update(delta);
        }

        // 2. Update player inputs & mirror/prism grabbing and rotation
        p1.update(delta, mirrors, prism, WORLD_WIDTH, WORLD_HEIGHT);
        p2.update(delta, mirrors, prism, WORLD_WIDTH, WORLD_HEIGHT);

        // 3. Update optical ray tracing with prism dispersion & mirror reflections
        laserSystem.update(mirrors, prism, p1, p2, targets);

        // Camera sync
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        // 4. Render 2D map grid and borders
        drawMapGrid();

        // 5. Render color-matched target receivers
        for (TargetReceiver tr : targets) {
            tr.render(shapeRenderer);
        }

        // 6. Render fixed laser emitter and multi-colored beam segments
        laserSystem.render(shapeRenderer);

        // 7. Render triangular optical prism
        prism.render(shapeRenderer);

        // 8. Render mirrors
        for (Mirror mirror : mirrors) {
            mirror.render(shapeRenderer);
        }

        // 9. Render players
        p1.render(shapeRenderer);
        p2.render(shapeRenderer);

        // 10. Render top control bar & status indicators
        renderHUD();
    }

    private void renderHUD() {
        boolean allPowered = targetRed.isPowered() && targetCyan.isPowered();

        // Top control panel
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.11f, 0.14f, 0.20f, 0.92f));
        shapeRenderer.rect(20f, WORLD_HEIGHT - 65f, WORLD_WIDTH - 40f, 52f);

        // P1 Badge (Blue)
        shapeRenderer.setColor(Color.valueOf("38bdf8"));
        shapeRenderer.rect(36f, WORLD_HEIGHT - 54f, 12f, 30f);

        // P2 Badge (Green)
        shapeRenderer.setColor(Color.valueOf("4ade80"));
        shapeRenderer.rect(460f, WORLD_HEIGHT - 54f, 12f, 30f);

        // Target 1 Status Pill (Red)
        if (targetRed.isPowered()) {
            shapeRenderer.setColor(LaserSystem.COLOR_RED);
        } else {
            shapeRenderer.setColor(new Color(0.4f, 0.1f, 0.15f, 1f));
        }
        shapeRenderer.circle(WORLD_WIDTH - 150f, WORLD_HEIGHT - 39f, 11f);

        // Target 2 Status Pill (Cyan)
        if (targetCyan.isPowered()) {
            shapeRenderer.setColor(LaserSystem.COLOR_CYAN);
        } else {
            shapeRenderer.setColor(new Color(0.1f, 0.25f, 0.35f, 1f));
        }
        shapeRenderer.circle(WORLD_WIDTH - 70f, WORLD_HEIGHT - 39f, 11f);

        // Victory celebration banner when both are active
        if (allPowered) {
            shapeRenderer.setColor(new Color(0.13f, 0.77f, 0.36f, 0.25f));
            shapeRenderer.rect(WORLD_WIDTH / 2f - 180f, WORLD_HEIGHT - 62f, 360f, 46f);
        }
        shapeRenderer.end();

        // Outlines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(allPowered ? Color.valueOf("22c55e") : new Color(0.28f, 0.35f, 0.48f, 1f));
        shapeRenderer.rect(20f, WORLD_HEIGHT - 65f, WORLD_WIDTH - 40f, 52f);

        // Target outlines
        shapeRenderer.setColor(targetRed.isPowered() ? Color.WHITE : LaserSystem.COLOR_RED);
        shapeRenderer.circle(WORLD_WIDTH - 150f, WORLD_HEIGHT - 39f, 13f);

        shapeRenderer.setColor(targetCyan.isPowered() ? Color.WHITE : LaserSystem.COLOR_CYAN);
        shapeRenderer.circle(WORLD_WIDTH - 70f, WORLD_HEIGHT - 39f, 13f);

        if (allPowered) {
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(WORLD_WIDTH / 2f - 180f, WORLD_HEIGHT - 62f, 360f, 46f);
        }
        shapeRenderer.end();
    }

    private void drawMapGrid() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Subtle coordinate grid
        shapeRenderer.setColor(new Color(0.14f, 0.17f, 0.23f, 1f));
        for (float x = 0; x <= WORLD_WIDTH; x += GRID_SIZE) {
            shapeRenderer.line(x, 0, x, WORLD_HEIGHT);
        }
        for (float y = 0; y <= WORLD_HEIGHT; y += GRID_SIZE) {
            shapeRenderer.line(0, y, WORLD_WIDTH, y);
        }

        // Map boundary border
        shapeRenderer.setColor(new Color(0.30f, 0.38f, 0.50f, 1f));
        shapeRenderer.rect(2, 2, WORLD_WIDTH - 4, WORLD_HEIGHT - 4);

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
