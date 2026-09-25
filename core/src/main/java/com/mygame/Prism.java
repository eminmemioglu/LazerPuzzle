package com.mygame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents a movable and rotatable triangular optical prism.
 * Splits incoming white laser light into two distinct colored rays (Red & Cyan).
 */
public class Prism {
    private float x;
    private float y;
    private float angleDeg;
    private final float radius = 34f; // Circumradius of the equilateral triangle

    private final Vector2 v1 = new Vector2();
    private final Vector2 v2 = new Vector2();
    private final Vector2 v3 = new Vector2();

    private boolean isHovered = false;
    private boolean isGrabbed = false;
    private Color highlightColor = Color.WHITE;

    private final Vector2 tempHit = new Vector2();

    public Prism(float startX, float startY, float angleDeg) {
        this.x = startX;
        this.y = startY;
        this.angleDeg = angleDeg;
        updateVertices();
    }

    public void updateVertices() {
        float r1 = (angleDeg) * MathUtils.degreesToRadians;
        float r2 = (angleDeg + 120f) * MathUtils.degreesToRadians;
        float r3 = (angleDeg + 240f) * MathUtils.degreesToRadians;

        v1.set(x + radius * MathUtils.cos(r1), y + radius * MathUtils.sin(r1));
        v2.set(x + radius * MathUtils.cos(r2), y + radius * MathUtils.sin(r2));
        v3.set(x + radius * MathUtils.cos(r3), y + radius * MathUtils.sin(r3));
    }

    public void setPosition(float newX, float newY, float mapWidth, float mapHeight) {
        float pad = radius + 15f;
        this.x = MathUtils.clamp(newX, pad, mapWidth - pad);
        this.y = MathUtils.clamp(newY, pad, mapHeight - pad);
        updateVertices();
    }

    /**
     * Smooth fine-tuned rotation method.
     */
    public void rotateBy(float deltaAngle) {
        this.angleDeg = (this.angleDeg + deltaAngle + 360f) % 360f;
        updateVertices();
    }

    /**
     * Checks if a ray segment intersects any of the 3 triangular prism faces.
     * Stores the closest hit point in outIntersection.
     */
    public boolean intersects(Vector2 start, Vector2 end, Vector2 outIntersection) {
        boolean hit = false;
        float closestDist = Float.MAX_VALUE;

        // Check edge 1
        if (Intersector.intersectSegments(start, end, v1, v2, tempHit)) {
            float d = start.dst(tempHit);
            if (d < closestDist) {
                closestDist = d;
                outIntersection.set(tempHit);
                hit = true;
            }
        }
        // Check edge 2
        if (Intersector.intersectSegments(start, end, v2, v3, tempHit)) {
            float d = start.dst(tempHit);
            if (d < closestDist) {
                closestDist = d;
                outIntersection.set(tempHit);
                hit = true;
            }
        }
        // Check edge 3
        if (Intersector.intersectSegments(start, end, v3, v1, tempHit)) {
            float d = start.dst(tempHit);
            if (d < closestDist) {
                closestDist = d;
                outIntersection.set(tempHit);
                hit = true;
            }
        }

        return hit;
    }

    public void render(ShapeRenderer shapeRenderer) {
        // Player interaction halo
        if (isGrabbed || isHovered) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(highlightColor.r, highlightColor.g, highlightColor.b, isGrabbed ? 0.9f : 0.4f);
            shapeRenderer.circle(x, y, radius + 10f);
            shapeRenderer.end();
        }

        // Translucent prism interior
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.35f, 0.55f, 0.75f, 0.28f));
        shapeRenderer.triangle(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);

        // Core refractive gem
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(x, y, 4f);
        shapeRenderer.end();

        // Glowing crystal borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // Outer cyan glow
        shapeRenderer.setColor(new Color(0.4f, 0.9f, 1f, 0.85f));
        shapeRenderer.triangle(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);

        // Inner prism facet lines
        shapeRenderer.setColor(new Color(0.7f, 0.85f, 1f, 0.6f));
        shapeRenderer.line(x, y, v1.x, v1.y);
        shapeRenderer.line(x, y, v2.x, v2.y);
        shapeRenderer.line(x, y, v3.x, v3.y);

        // Small corner nodes
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(v1.x, v1.y, 2.5f);
        shapeRenderer.circle(v2.x, v2.y, 2.5f);
        shapeRenderer.circle(v3.x, v3.y, 2.5f);

        // Apex orientation pointer showing prism aiming angle
        float rad = angleDeg * MathUtils.degreesToRadians;
        shapeRenderer.setColor(Color.valueOf("fcd34d")); // Gold pointer
        shapeRenderer.line(x, y, x + (radius + 8f) * MathUtils.cos(rad), y + (radius + 8f) * MathUtils.sin(rad));

        shapeRenderer.end();

        isHovered = false;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public Vector2 getCenter() { return new Vector2(x, y); }
    public float getRadius() { return radius; }
    public float getAngleDeg() { return angleDeg; }

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
