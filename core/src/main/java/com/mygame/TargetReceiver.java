package com.mygame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents a color-sensitive laser receiver placed in the arena.
 * Only activates when struck by a laser beam matching its target color.
 */
public class TargetReceiver {
    private final Vector2 position;
    private final float radius = 26f;
    private final Color requiredColor;
    private final String label;

    private boolean isPowered = false;
    private float pulseTimer = 0f;

    public TargetReceiver(float x, float y, Color requiredColor, String label) {
        this.position = new Vector2(x, y);
        this.requiredColor = requiredColor;
        this.label = label;
    }

    public void update(float delta) {
        if (isPowered) {
            pulseTimer += delta * 6f;
        } else {
            pulseTimer = 0f;
        }
    }

    /**
     * Checks if a laser beam segment strikes this receiver and whether its color matches.
     */
    public boolean checkHit(Vector2 start, Vector2 end, Color beamColor, Vector2 outHitPoint) {
        boolean hit = Intersector.intersectSegmentCircle(start, end, position, radius * radius);
        if (hit && outHitPoint != null) {
            Vector2 dir = new Vector2(start).sub(position).nor();
            outHitPoint.set(position.x + dir.x * radius, position.y + dir.y * radius);
        }

        if (hit) {
            // Check if beam color matches required color
            float dr = Math.abs(beamColor.r - requiredColor.r);
            float dg = Math.abs(beamColor.g - requiredColor.g);
            float db = Math.abs(beamColor.b - requiredColor.b);
            boolean colorMatches = (dr + dg + db) < 0.4f;

            if (colorMatches) {
                isPowered = true;
            }
        }

        return hit;
    }

    public void render(ShapeRenderer shapeRenderer) {
        float pulse = (float) Math.sin(pulseTimer) * 4f;

        // Outer glow when powered
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (isPowered) {
            shapeRenderer.setColor(requiredColor.r, requiredColor.g, requiredColor.b, 0.35f);
            shapeRenderer.circle(position.x, position.y, radius + 14f + pulse);
        }

        // Base circular receiver pad (Darkened matching color)
        shapeRenderer.setColor(requiredColor.r * 0.25f, requiredColor.g * 0.25f, requiredColor.b * 0.25f, 1f);
        shapeRenderer.circle(position.x, position.y, radius + 4f);

        // Sensor core
        if (isPowered) {
            shapeRenderer.setColor(requiredColor);
        } else {
            shapeRenderer.setColor(requiredColor.r * 0.55f, requiredColor.g * 0.55f, requiredColor.b * 0.55f, 1f);
        }
        shapeRenderer.circle(position.x, position.y, radius);

        // Center lens
        shapeRenderer.setColor(isPowered ? Color.WHITE : Color.valueOf("e2e8f0"));
        shapeRenderer.circle(position.x, position.y, 8f);
        shapeRenderer.end();

        // Concentric sensor outline rings
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(isPowered ? Color.WHITE : requiredColor);
        shapeRenderer.circle(position.x, position.y, radius + 4f);
        shapeRenderer.circle(position.x, position.y, radius * 0.6f);

        // Crosshair reticle lines
        float arm = radius + 8f;
        shapeRenderer.setColor(requiredColor.r, requiredColor.g, requiredColor.b, isPowered ? 1f : 0.6f);
        shapeRenderer.line(position.x - arm, position.y, position.x + arm, position.y);
        shapeRenderer.line(position.x, position.y - arm, position.x, position.y + arm);
        shapeRenderer.end();
    }

    public Vector2 getPosition() { return position; }
    public float getRadius() { return radius; }
    public boolean isPowered() { return isPowered; }
    public void setPowered(boolean powered) { this.isPowered = powered; }
    public Color getRequiredColor() { return requiredColor; }
    public String getLabel() { return label; }
}
