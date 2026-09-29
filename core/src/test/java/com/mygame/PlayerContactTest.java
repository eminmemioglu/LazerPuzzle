package com.mygame;

import com.badlogic.gdx.utils.Array;

/** Two-body regressions: pushing, stacking, constrained spaces and update-order symmetry. */
public final class PlayerContactTest {
    private static final float DT = 1f / 60f;

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void near(float actual, float expected, String message) {
        check(Math.abs(actual - expected) < 0.02f, message + ": " + actual + " != " + expected);
    }

    private static void step(Player a, Player b, float ax, float bx, boolean aj, boolean bj,
                             Array<Platform> platforms, float dt) {
        Player.movePair(dt, a, b, ax, bx, aj, bj, platforms, 1280f, 720f);
        float overlapX = 34f - Math.abs(a.getCenterX() - b.getCenterX());
        float overlapY = 34f - Math.abs(a.getCenterY() - b.getCenterY());
        check(overlapX < 0.01f || overlapY < 0.01f,
            "Players overlap: " + overlapX + " x " + overlapY);
    }

    public static void main(String[] args) {
        Array<Platform> floor = new Array<>();
        floor.add(new Platform(0f, 0f, 1280f, 64f));
        Player a = Player.createPlayer1(200f, 81f), b = Player.createPlayer2(234f, 81f);
        for (int i = 0; i < 60; i++) step(a, b, 1f, 0f, false, false, floor, DT);
        near(a.getCenterX(), 360f, "Pusher should share movement with the pushed player");
        near(b.getCenterX(), 394f, "Stationary player should be pushed");
        Player reverseA = Player.createPlayer1(200f, 81f), reverseB = Player.createPlayer2(234f, 81f);
        for (int i = 0; i < 60; i++) step(reverseB, reverseA, 0f, 1f, false, false, floor, DT);
        near(reverseA.getCenterX(), a.getCenterX(), "Reversing update order must preserve results");
        near(reverseB.getCenterX(), b.getCenterX(), "Reversing update order must preserve pushes");
        for (int i = 0; i < 60; i++) step(a, b, 0f, -1f, false, false, floor, DT);
        near(a.getCenterX(), 200f, "Second player must also be able to push");
        for (int i = 0; i < 60; i++) step(a, b, 1f, -1f, false, false, floor, DT);
        near(a.getCenterX(), 200f, "Opposing equal pushes must cancel");

        Array<Platform> wall = new Array<>(floor);
        wall.add(new Platform(300f, 64f, 100f, 200f));
        a = Player.createPlayer1(249f, 81f); b = Player.createPlayer2(283f, 81f);
        for (int i = 0; i < 90; i++) step(a, b, 1f, 0f, false, false, wall, DT);
        near(a.getCenterX(), 249f, "Wall must stop the pusher");
        near(b.getCenterX(), 283f, "Pushed player must not enter wall");
        a = Player.createPlayer1(1225f, 81f); b = Player.createPlayer2(1259f, 81f);
        for (int i = 0; i < 30; i++) step(a, b, 1f, 0f, false, false, floor, DT);
        near(b.getCenterX(), 1259f, "Map edge must stop pushing");

        a = Player.createPlayer1(200f, 81f); b = Player.createPlayer2(200f, 350f);
        for (int i = 0; i < 120; i++) step(a, b, 0f, 0f, false, false, floor, DT);
        near(b.getCenterY(), 115f, "Falling player must land on the other's head");
        check(b.isGrounded(), "Standing on a teammate must allow jumping");
        for (int i = 0; i < 60; i++) step(a, b, 1f, 0f, false, false, floor, DT);
        near(b.getCenterX(), a.getCenterX(), "Rider must travel with the supporting player");
        near(b.getCenterY(), 115f, "Rider must remain stable while moving");
        step(a, b, 0f, 0f, false, true, floor, DT);
        check(b.getCenterY() > 115f && !b.isGrounded(), "Rider must jump off the other player");
        for (int i = 0; i < 90; i++) step(a, b, 0f, 0f, false, false, floor, DT);
        near(b.getCenterY(), 115f, "Rider must land again after jumping");
        for (int i = 0; i < 60; i++) step(a, b, 0f, 1f, false, false, floor, DT);
        near(b.getCenterY(), 81f, "Walking off the teammate must fall to the floor");

        a = Player.createPlayer1(200f, 81f); b = Player.createPlayer2(200f, 115f);
        step(a, b, 0f, 0f, true, false, floor, DT);
        check(a.getCenterY() > 81f && b.getCenterY() > 115f, "Bottom player can jump while carrying a rider");
        for (int i = 0; i < 90; i++) step(a, b, 0f, 0f, false, false, floor, DT);
        near(a.getCenterY(), 81f, "Stack must return to the ground");
        near(b.getCenterY(), 115f, "Stack must remain separated on landing");

        Array<Platform> ceiling = new Array<>(floor);
        ceiling.add(new Platform(100f, 150f, 400f, 20f));
        a = Player.createPlayer1(200f, 81f); b = Player.createPlayer2(200f, 115f);
        for (int i = 0; i < 90; i++) {
            step(a, b, 0f, 0f, i == 0, false, ceiling, DT);
            check(b.getCenterY() + 17f <= 150.01f, "Rider must not enter a ceiling when lifted");
        }
        for (float dt : new float[]{1f / 30f, 1f / 144f, 0.1f}) {
            a = Player.createPlayer1(200f, 81f); b = Player.createPlayer2(200f, 650f);
            for (int i = 0; i < Math.ceil(3f / dt); i++) step(a, b, 0f, 0f, false, false, floor, dt);
            near(b.getCenterY(), 115f, "Stacking must work at different frame rates");
        }
        a = Player.createPlayer1(200f, 81f); b = Player.createPlayer2(280f, 81f);
        for (int i = 0; i < 100; i++) {
            step(a, b, a.getCenterX() < b.getCenterX() ? 1f : 0f, 0f, i == 0, false, floor, DT);
        }
        near(a.getCenterY(), 115f, "Player must be able to jump onto a teammate from the side");

        Array<Platform> ledge = new Array<>(floor);
        ledge.add(new Platform(100f, 180f, 200f, 20f));
        a = Player.createPlayer1(240f, 217f); b = Player.createPlayer2(274f, 217f);
        for (int i = 0; i < 180; i++) step(a, b, 1f, 0f, false, false, ledge, DT);
        near(b.getCenterY(), 81f, "Pushed player must fall off a platform edge");
        System.out.println("Player contacts passed: pushing both ways, opposite inputs, order, walls, stacking, riding, jumping, ceilings, slow frames.");
    }
}
