package com.mygame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents a red laser beam projectile fired by the player.
 * Supports continuous collision detection and reflection off mirrors.
 */
public class Laser {
    private final Vector2 position = new Vector2();
    private final Vector2 prevPosition = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Vector2 hitPoint = new Vector2();

    private final float speed = 900f; // Pixels per second
    private final float beamLength = 30f;
    private boolean active = true;
    private float bounceCooldown = 0f;

    public Laser(float startX, float startY, float dirX, float dirY) {
        this.position.set(startX, startY);
        this.prevPosition.set(startX, startY);

        Vector2 dir = new Vector2(dirX, dirY).nor();
        this.velocity.set(dir.scl(speed));
    }

    /**
     * Updates laser position, checks mirror reflection, and verifies boundary limits.
     */
    public void update(float delta, Mirror mirror, float mapWidth, float mapHeight) {
        if (!active) return;

        if (bounceCooldown > 0f) {
            bounceCooldown -= delta;
        }

        prevPosition.set(position);
        position.add(velocity.x * delta, velocity.y * delta);

        // Continuous collision detection with mirror
        if (bounceCooldown <= 0f && mirror.intersects(prevPosition, position, hitPoint)) {
            position.set(hitPoint);
            Vector2 reflectedVelocity = mirror.reflect(velocity);
            velocity.set(reflectedVelocity);

            // Nudge slightly in new direction so it doesn't re-trigger collision
            position.add(velocity.x * delta * 0.1f, velocity.y * delta * 0.1f);
            bounceCooldown = 0.05f;
        }

        // Deactivate if out of game screen bounds
        if (position.x < -50 || position.x > mapWidth + 50 ||
            position.y < -50 || position.y > mapHeight + 50) {
            active = false;
        }
    }

    /**
     * Renders the laser as a vivid red beam with a bright core.
     */
    public void render(ShapeRenderer shapeRenderer) {
        if (!active) return;

        // Calculate tail position based on current velocity direction
        Vector2 dir = new Vector2(velocity).nor();
        float tailX = position.x - dir.x * beamLength;
        float tailY = position.y - dir.y * beamLength;

        // Outer red glow
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(1f, 0.15f, 0.25f, 0.7f));
        shapeRenderer.line(tailX - 1, tailY, position.x - 1, position.y);
        shapeRenderer.line(tailX + 1, tailY, position.x + 1, position.y);
        shapeRenderer.line(tailX, tailY - 1, position.x, position.y - 1);
        shapeRenderer.line(tailX, tailY + 1, position.x, position.y + 1);

        // Core bright beam (Intense red/pink)
        shapeRenderer.setColor(new Color(1f, 0.35f, 0.45f, 1f));
        shapeRenderer.line(tailX, tailY, position.x, position.y);
        shapeRenderer.end();

        // Laser tip point
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(position.x, position.y, 2.5f);
        shapeRenderer.end();
    }

    public boolean isActive() {
        return active;
    }

    public Vector2 getPosition() {
        return position;
    }
}
