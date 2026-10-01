package com.mygame;

/** Timing for the supplied nine-frame jump: takeoff 0–5, fall 6, landing 7–8. */
final class JumpAnimation {
    private static final float RISE_FRAME_SECONDS = 0.065f;
    private static final float LAND_FRAME_SECONDS = 0.09f;
    private boolean airborne;
    private float riseTime;
    private float landingTime = -1f;
    private int frameIndex = -1;

    void update(float delta, boolean grounded, float verticalVelocity) {
        delta = Math.max(0f, Math.min(delta, 0.1f));
        if (!grounded) {
            riseTime = airborne ? riseTime + delta : 0f;
            airborne = true;
            landingTime = -1f;
            frameIndex = verticalVelocity > 0f ? Math.min(5, (int) (riseTime / RISE_FRAME_SECONDS)) : 6;
        } else {
            if (airborne) {
                landingTime = 0f;
                airborne = false;
            } else if (landingTime >= 0f) {
                landingTime += delta;
            }
            if (landingTime >= 2f * LAND_FRAME_SECONDS) landingTime = -1f;
            frameIndex = landingTime < 0f ? -1 : 7 + (int) (landingTime / LAND_FRAME_SECONDS);
        }
    }

    int getFrameIndex() { return frameIndex; }
}
