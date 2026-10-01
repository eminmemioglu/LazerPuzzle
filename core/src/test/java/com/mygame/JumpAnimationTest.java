package com.mygame;

final class JumpAnimationTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    static void run() {
        JumpAnimation animation = new JumpAnimation();
        animation.update(1f / 60f, true, 0f);
        check(animation.getFrameIndex() == -1, "Standing must use idle pose");
        animation.update(1f / 60f, false, 700f);
        check(animation.getFrameIndex() == 0, "Takeoff must start at frame zero");
        for (int i = 0; i < 30; i++) animation.update(1f / 60f, false, 100f);
        check(animation.getFrameIndex() == 5, "Rise must hold the tucked pose without looping");
        for (int i = 0; i < 120; i++) animation.update(1f / 60f, false, -300f);
        check(animation.getFrameIndex() == 6, "Long falls must hold the fall pose");
        animation.update(1f / 60f, true, 0f);
        check(animation.getFrameIndex() == 7, "Landing must wait for actual ground contact");
        animation.update(0.1f, true, 0f);
        check(animation.getFrameIndex() == 8, "Landing recovery must play");
        animation.update(0.1f, true, 0f);
        check(animation.getFrameIndex() == -1, "Landing must return to idle");
        animation.update(1f / 60f, false, -20f);
        check(animation.getFrameIndex() == 6, "Walking off a ledge must skip takeoff poses");
        animation.update(1f / 60f, true, 0f);
        animation.update(1f / 60f, false, 720f);
        check(animation.getFrameIndex() == 0, "Jumping during landing must restart immediately");
        System.out.println("Jump animation passed: takeoff, fall, contact, recovery, ledges and repeat jumps.");
    }
}
