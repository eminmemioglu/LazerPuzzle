package com.mygame;

/** Physics-driven timing: rising poses, a held fall pose, then landing poses. */
final class JumpAnimation {
    private static final float RISE_FRAME_SECONDS = 0.065f;
    private static final float LAND_FRAME_SECONDS = 0.09f;
    private boolean airborne;
    private float riseTime;
    private float landingTime = -1f;
    private int frameIndex = -1;
    private final int fallFrame;
    private final int frameCount;

    JumpAnimation() { this(6, 9); }

    JumpAnimation(int fallFrame, int frameCount) {
        if (fallFrame < 1 || frameCount <= fallFrame + 1) {
            throw new IllegalArgumentException("Jump needs rising, falling and landing frames");
        }
        this.fallFrame = fallFrame;
        this.frameCount = frameCount;
    }

    void update(float delta, boolean grounded, float verticalVelocity) {
        delta = Math.max(0f, Math.min(delta, 0.1f));
        if (!grounded) {
            riseTime = airborne ? riseTime + delta : 0f;
            airborne = true;
            landingTime = -1f;
            frameIndex = verticalVelocity > 0f ? Math.min(fallFrame - 1, (int) (riseTime / RISE_FRAME_SECONDS)) : fallFrame;
        } else {
            if (airborne) {
                landingTime = 0f;
                airborne = false;
            } else if (landingTime >= 0f) {
                landingTime += delta;
            }
            if (landingTime >= (frameCount - fallFrame - 1) * LAND_FRAME_SECONDS) landingTime = -1f;
            frameIndex = landingTime < 0f ? -1 : fallFrame + 1 + (int) (landingTime / LAND_FRAME_SECONDS);
        }
    }

    int getFrameIndex() { return frameIndex; }
}
