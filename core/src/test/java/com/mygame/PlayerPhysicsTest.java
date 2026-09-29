package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import java.lang.reflect.Proxy;

/** Deterministic collision checks; no window or additional test dependency required. */
public final class PlayerPhysicsTest {
    private static final float DT = 1f / 60f;

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static float feet(Player player) {
        return player.getCenterY() - player.getSize() / 2f;
    }

    private static void move(Player player, Array<Platform> platforms, float direction, boolean jump, float dt) {
        player.updateMovement(dt, direction, jump, platforms, 1280f, 720f);
    }

    private static void settle(Player player, Array<Platform> platforms) {
        for (int i = 0; i < 180; i++) move(player, platforms, 0f, false, DT);
    }

    public static void main(String[] args) {
        Array<Platform> floor = new Array<>();
        floor.add(new Platform(0f, 0f, 1280f, 64f));
        Player player = Player.createPlayer1(140f, 500f);
        move(player, floor, 0f, false, DT);
        check(player.getCenterY() < 500f, "Gravity must pull an airborne player down");
        settle(player, floor);
        check(feet(player) == 64f && player.isGrounded(), "Player must land on the floor");

        move(player, floor, 0f, true, DT);
        float jumpingY = feet(player);
        check(jumpingY > 64f && !player.isGrounded(), "Jump must leave the ground");
        Player control = Player.createPlayer1(140f, 81f);
        move(control, floor, 0f, true, DT);
        for (int i = 0; i < 20; i++) {
            move(player, floor, 0f, true, DT);
            move(control, floor, 0f, false, DT);
        }
        check(Math.abs(feet(player) - feet(control)) < 0.001f, "Jump presses in the air must not reset velocity");
        settle(player, floor);
        move(player, floor, 0f, true, DT);
        check(feet(player) > 64f, "Jump must work again after landing");

        Array<Platform> ledges = new Array<>(floor);
        ledges.add(new Platform(300f, 180f, 160f, 20f));
        player = Player.createPlayer2(380f, 550f);
        settle(player, ledges);
        check(feet(player) == 200f, "Player must land on the raised platform");
        for (int i = 0; i < 30; i++) move(player, ledges, 1f, false, DT);
        check(feet(player) < 200f && !player.isGrounded(), "Walking off an edge must start a fall");
        settle(player, ledges);
        check(feet(player) == 64f, "Player must land on the floor after walking off an edge");

        player = Player.createPlayer1(380f, 81f);
        move(player, ledges, 0f, true, DT);
        float peak = feet(player);
        for (int i = 0; i < 60; i++) {
            move(player, ledges, 0f, false, DT);
            peak = Math.max(peak, feet(player));
        }
        check(peak + player.getSize() <= 180.001f, "Head must not pass through a platform");

        Array<Platform> wall = new Array<>(floor);
        wall.add(new Platform(300f, 64f, 100f, 200f));
        player = Player.createPlayer1(250f, 81f);
        for (int i = 0; i < 60; i++) move(player, wall, 1f, false, DT);
        check(player.getCenterX() + player.getSize() / 2f <= 300f, "Platform side must block movement");

        player = Player.createPlayer1(380f, 650f);
        for (int i = 0; i < 30; i++) move(player, ledges, 0f, false, 0.1f);
        check(feet(player) == 200f, "Slow frames must not tunnel through thin platforms");

        float minPeak = Float.MAX_VALUE, maxPeak = 0f;
        for (int fps : new int[]{30, 60, 144}) {
            player = Player.createPlayer1(140f, 81f);
            move(player, floor, 0f, true, 1f / fps);
            peak = feet(player);
            for (int i = 0; i < fps * 2; i++) {
                move(player, floor, 0f, false, 1f / fps);
                peak = Math.max(peak, feet(player));
            }
            check(feet(player) == 64f, "Landing must work at " + fps + " FPS");
            minPeak = Math.min(minPeak, peak);
            maxPeak = Math.max(maxPeak, peak);
        }
        check(maxPeak - minPeak < 3f, "Jump height should remain consistent across frame rates");
        check(minPeak > 200f, "Jump must reach the first level's platform gaps");
        Array<Platform> level = LevelDefinition.LEVEL_1.createPlatforms();
        player = Player.createPlayer1(180f, 81f);
        jumpTo(player, level, 400f, 168f);
        while (player.getCenterX() < 470f) move(player, level, 1f, false, DT);
        jumpTo(player, level, 560f, 272f, 8);
        jumpTo(player, level, 400f, 340f);
        while (player.getCenterX() < 460f) move(player, level, 1f, false, DT);
        jumpTo(player, level, 670f, 452f);
        checkCarrying(floor);
        PlayerContactTest.main(args);
        System.out.println("Player physics passed: gravity, landing, jump, air-jump prevention, edges, ceiling, sides, slow frames, frame rates.");
    }

    private static void checkCarrying(Array<Platform> floor) {
        Input original = Gdx.input;
        boolean[] jumping = {false};
        Gdx.input = (Input) Proxy.newProxyInstance(Input.class.getClassLoader(), new Class<?>[]{Input.class},
            (proxy, method, args) -> {
                int key = args != null && args.length > 0 && args[0] instanceof Integer ? (int) args[0] : -1;
                if (method.getName().equals("isKeyPressed")) return key == Input.Keys.SPACE || key == Input.Keys.A;
                if (method.getName().equals("isKeyJustPressed")) return jumping[0] && key == Input.Keys.W;
                throw new UnsupportedOperationException(method.getName());
            });
        try {
            Player player = Player.createPlayer1(21f, 81f);
            Prism prism = new Prism(65f, 100f, 0f);
            Array<Mirror> mirrors = new Array<>();
            for (int i = 0; i < 30; i++) player.update(DT, mirrors, prism, floor, 1280f, 720f);
            check(prism.getX() == 65f && prism.getY() == 100f,
                "Held objects must not drift while the player is blocked by the floor or map edge");
            float beforePlayerY = player.getCenterY();
            jumping[0] = true;
            player.update(DT, mirrors, prism, floor, 1280f, 720f);
            jumping[0] = false;
            check(Math.abs(prism.getY() - 100f - (player.getCenterY() - beforePlayerY)) < 0.001f,
                "Held objects must follow the character during a jump");
            for (int i = 0; i < 90; i++) player.update(DT, mirrors, prism, floor, 1280f, 720f);
            check(Math.abs(prism.getY() - 100f) < 0.01f && player.isGrounded(),
                "Held objects must follow the fall and stop at landing");
        } finally {
            Gdx.input = original;
        }
    }

    private static void jumpTo(Player player, Array<Platform> platforms, float targetX, float targetY) {
        jumpTo(player, platforms, targetX, targetY, 0);
    }

    private static void jumpTo(Player player, Array<Platform> platforms, float targetX, float targetY,
                               int verticalFrames) {
        for (int i = 0; i < 90; i++) {
            float distance = targetX - player.getCenterX();
            float direction = i >= verticalFrames && Math.abs(distance) > 3f ? Math.signum(distance) : 0f;
            move(player, platforms, direction, i == 0, DT);
        }
        check(Math.abs(feet(player) - targetY) < 0.01f,
            "First level platform at " + targetY + " must be reachable; landed at " + feet(player));
    }
}
