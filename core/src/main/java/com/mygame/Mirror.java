package com.mygame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents a movable and rotatable mirror.
 * Can be grabbed and moved by players and rotated smoothly.
 */
public class Mirror {
    private float x;
    private float y;
    private float angleDeg;
    private final float length;
    private final int id;

    private final Vector2 p1 = new Vector2();
    private final Vector2 p2 = new Vector2();
    private final Vector2 normal = new Vector2();

    private boolean isHovered = false;
    private boolean isGrabbed = false;
    private Color highlightColor = Color.WHITE;

    public Mirror(int id, float startX, float startY, float angleDeg, float length) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.angleDeg = angleDeg;
        this.length = length;
        updateGeometry();
    }

    /**
     * Recomputes segment endpoints p1, p2 and normal vector based on current position and angle.
     */
    public void updateGeometry() {
        float rad = angleDeg * MathUtils.degreesToRadians;
        float halfLen = length / 2f;
        float dx = halfLen * MathUtils.cos(rad);
        float dy = halfLen * MathUtils.sin(rad);

        p1.set(x - dx, y - dy);
        p2.set(x + dx, y + dy);

        // Unit normal perpendicular to the line: (-sin, cos)
        normal.set(-MathUtils.sin(rad), MathUtils.cos(rad)).nor();
    }

    public void setPosition(float newX, float newY, float mapWidth, float mapHeight) {
        float pad = length / 2f + 10f;
        this.x = MathUtils.clamp(newX, pad, mapWidth - pad);
        this.y = MathUtils.clamp(newY, pad, mapHeight - pad);
        updateGeometry();
    }

    /**
     * Smooth fine-tuned rotation method.
     */
    public void rotateBy(float deltaAngle) {
        this.angleDeg = (this.angleDeg + deltaAngle + 360f) % 360f;
        updateGeometry();
    }

    public void rotateClockwise() {
        rotateBy(-15f);
    }

    public void rotateCounterClockwise() {
        rotateBy(15f);
    }

    /**
     * Checks if a ray segment intersects this mirror line segment.
     */
    public boolean intersects(Vector2 start, Vector2 end, Vector2 outIntersection) {
        return Intersector.intersectSegments(start, end, p1, p2, outIntersection);
    }

    /**
     * Reflects incoming beam velocity across mirror surface normal:
     * v' = v - 2 * (v . n) * n
     */
    public Vector2 reflect(Vector2 incomingVelocity) {
        float dot = incomingVelocity.dot(normal);
        Vector2 reflected = new Vector2(incomingVelocity);
        reflected.sub(normal.x * 2f * dot, normal.y * 2f * dot);
        return reflected;
    }

    /**
     * Renders mirror with reflective surface, pivot stand, and interaction highlight.
     */
    public void render(ShapeRenderer shapeRenderer) {
        // Highlight aura when a player is in range or carrying it
        if (isGrabbed || isHovered) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(highlightColor.r, highlightColor.g, highlightColor.b, isGrabbed ? 0.9f : 0.4f);
            shapeRenderer.circle(x, y, length / 2f + 8f);
            shapeRenderer.end();
        }

        // Center pivot / stand
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.2f, 0.28f, 0.38f, 1f));
        shapeRenderer.circle(x, y, 6f);
        shapeRenderer.end();

        // Mirror reflective body
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // Outer glow
        shapeRenderer.setColor(new Color(0.4f, 0.85f, 1f, 0.8f));
        shapeRenderer.line(p1.x - normal.x, p1.y - normal.y, p2.x - normal.x, p2.y - normal.y);
        shapeRenderer.line(p1.x + normal.x, p1.y + normal.y, p2.x + normal.x, p2.y + normal.y);

        // Core silver mirror line
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.line(p1.x, p1.y, p2.x, p2.y);

        // End caps
        shapeRenderer.setColor(new Color(0.38f, 0.9f, 1f, 1f));
        shapeRenderer.circle(p1.x, p1.y, 3f);
        shapeRenderer.circle(p2.x, p2.y, 3f);

        // Small indicator notch showing current normal direction
        float notchLen = 12f;
        shapeRenderer.setColor(Color.valueOf("38bdf8"));
        shapeRenderer.line(x, y, x + normal.x * notchLen, y + normal.y * notchLen);

        shapeRenderer.end();

        isHovered = false;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public Vector2 getCenter() { return new Vector2(x, y); }
    public float getAngleDeg() { return angleDeg; }
    public float getLength() { return length; }
    public int getId() { return id; }

    public void setHovered(boolean hovered, Color color) {
        this.isHovered = hovered;
        this.highlightColor = color;
    }

    public void setGrabbed(boolean grabbed, Color color) {
        this.isGrabbed = grabbed;
        this.highlightColor = color;
    }

    public boolean isGrabbed() { return isGrabbed; }
}
