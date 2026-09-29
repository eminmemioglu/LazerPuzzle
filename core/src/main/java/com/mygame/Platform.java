package com.mygame;

/** Solid rectangle used for character collisions and platform rendering. */
public final class Platform {
    public final float x, y, width, height;

    public Platform(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float top() { return y + height; }
    public float right() { return x + width; }
}
