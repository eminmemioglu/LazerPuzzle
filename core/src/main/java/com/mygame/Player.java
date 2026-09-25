package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Represents a playable character in the cooperative puzzle.
 * Handles movement, mirror/prism grabbing, and smooth fine-tuned rotation.
 */
public class Player {
    private final int id;
    private float x;
    private float y;
    private final float size = 34f;
    private final float speed = 320f;
    private final Color color;

    // Key configuration
    private final int keyUp;
    private final int keyDown;
    private final int keyLeft;
    private final int keyRight;
    private final int keyGrab;
    private final int keyRotLeft;
    private final int keyRotRight;

    private Mirror grabbedMirror = null;
    private Prism grabbedPrism = null;
    private final float grabRange = 70f;

    public Player(int id, float startX, float startY, Color color,
                  int up, int down, int left, int right,
                  int grab, int rotLeft, int rotRight) {
        this.id = id;
        this.x = startX - size / 2f;
        this.y = startY - size / 2f;
        this.color = color;

        this.keyUp = up;
        this.keyDown = down;
        this.keyLeft = left;
        this.keyRight = right;
        this.keyGrab = grab;
        this.keyRotLeft = rotLeft;
        this.keyRotRight = rotRight;
    }

    public static Player createPlayer1(float startX, float startY) {
        return new Player(
            1, startX, startY,
            Color.valueOf("38bdf8"), // Bright Sky Blue
            Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D,
            Input.Keys.SPACE, Input.Keys.Q, Input.Keys.E
        );
    }

    public static Player createPlayer2(float startX, float startY) {
        return new Player(
            2, startX, startY,
            Color.valueOf("4ade80"), // Bright Emerald Green
            Input.Keys.UP, Input.Keys.DOWN, Input.Keys.LEFT, Input.Keys.RIGHT,
            Input.Keys.ENTER, Input.Keys.K, Input.Keys.L
        );
    }

    public void update(float delta, Array<Mirror> mirrors, Prism prism, float mapWidth, float mapHeight) {
        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(keyUp)) moveY += 1f;
        if (Gdx.input.isKeyPressed(keyDown)) moveY -= 1f;
        if (Gdx.input.isKeyPressed(keyLeft)) moveX -= 1f;
        if (Gdx.input.isKeyPressed(keyRight)) moveX += 1f;

        float deltaX = 0f;
        float deltaY = 0f;

        if (moveX != 0f || moveY != 0f) {
            float len = (float) Math.sqrt(moveX * moveX + moveY * moveY);
            deltaX = (moveX / len) * speed * delta;
            deltaY = (moveY / len) * speed * delta;

            x += deltaX;
            y += deltaY;
        }

        // Clamp inside screen
        x = MathUtils.clamp(x, 4f, mapWidth - size - 4f);
        y = MathUtils.clamp(y, 4f, mapHeight - size - 4f);

        float cx = getCenterX();
        float cy = getCenterY();

        // Check nearest interactable item (Mirror or Prism)
        Mirror nearestMirror = getNearestMirror(mirrors);
        float mirrorDist = (nearestMirror != null) ? nearestMirror.getCenter().dst(cx, cy) : Float.MAX_VALUE;
        float prismDist = (prism != null) ? prism.getCenter().dst(cx, cy) : Float.MAX_VALUE;

        boolean prismIsCloser = (prismDist < mirrorDist);

        // Hover feedback
        if (prismIsCloser && prismDist <= grabRange && prism != null) {
            prism.setHovered(true, color);
        } else if (!prismIsCloser && mirrorDist <= grabRange && nearestMirror != null) {
            nearestMirror.setHovered(true, color);
        }

        // Handle Grab/Hold
        if (Gdx.input.isKeyPressed(keyGrab)) {
            if (grabbedMirror == null && grabbedPrism == null) {
                if (prismIsCloser && prismDist <= grabRange && prism != null) {
                    grabbedPrism = prism;
                    grabbedPrism.setGrabbed(true, color);
                } else if (!prismIsCloser && mirrorDist <= grabRange && nearestMirror != null) {
                    grabbedMirror = nearestMirror;
                    grabbedMirror.setGrabbed(true, color);
                }
            }
        } else {
            if (grabbedMirror != null) {
                grabbedMirror.setGrabbed(false, color);
                grabbedMirror = null;
            }
            if (grabbedPrism != null) {
                grabbedPrism.setGrabbed(false, color);
                grabbedPrism = null;
            }
        }

        // Drag held item
        if (grabbedMirror != null) {
            grabbedMirror.setPosition(
                grabbedMirror.getX() + deltaX,
                grabbedMirror.getY() + deltaY,
                mapWidth,
                mapHeight
            );
        } else if (grabbedPrism != null) {
            grabbedPrism.setPosition(
                grabbedPrism.getX() + deltaX,
                grabbedPrism.getY() + deltaY,
                mapWidth,
                mapHeight
            );
        }

        // Smooth fine-tuned rotation (Tapping gives small ~3° step, holding gives smooth 55°/sec continuous rotation)
        float rotSpeed = 55f;
        float rotAmount = 0f;

        if (Gdx.input.isKeyJustPressed(keyRotLeft)) {
            rotAmount += 3.5f;
        } else if (Gdx.input.isKeyPressed(keyRotLeft)) {
            rotAmount += rotSpeed * delta;
        }

        if (Gdx.input.isKeyJustPressed(keyRotRight)) {
            rotAmount -= 3.5f;
        } else if (Gdx.input.isKeyPressed(keyRotRight)) {
            rotAmount -= rotSpeed * delta;
        }

        if (rotAmount != 0f) {
            if (grabbedMirror != null || (!prismIsCloser && mirrorDist <= grabRange && nearestMirror != null)) {
                Mirror m = (grabbedMirror != null) ? grabbedMirror : nearestMirror;
                m.rotateBy(rotAmount);
            } else if (grabbedPrism != null || (prismIsCloser && prismDist <= grabRange && prism != null)) {
                Prism p = (grabbedPrism != null) ? grabbedPrism : prism;
                p.rotateBy(rotAmount);
            }
        }
    }

    private Mirror getNearestMirror(Array<Mirror> mirrors) {
        Mirror closest = null;
        float minDist = Float.MAX_VALUE;
        float cx = getCenterX();
        float cy = getCenterY();

        for (Mirror m : mirrors) {
            float dist = m.getCenter().dst(cx, cy);
            if (dist < minDist) {
                minDist = dist;
                closest = m;
            }
        }
        return closest;
    }

    public void render(ShapeRenderer shapeRenderer) {
        float cx = getCenterX();
        float cy = getCenterY();

        // Tether line if carrying mirror or prism
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (grabbedMirror != null) {
            shapeRenderer.setColor(color.r, color.g, color.b, 0.7f);
            shapeRenderer.line(cx, cy, grabbedMirror.getX(), grabbedMirror.getY());
        } else if (grabbedPrism != null) {
            shapeRenderer.setColor(color.r, color.g, color.b, 0.7f);
            shapeRenderer.line(cx, cy, grabbedPrism.getX(), grabbedPrism.getY());
        }
        shapeRenderer.end();

        // Player filled square
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, size, size);

        // Center badge dot
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(cx, cy, 5f);
        shapeRenderer.end();

        // Border outline
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x, y, size, size);

        // Distinct ID line markings
        if (id == 1) {
            shapeRenderer.line(cx, y + 6f, cx, y + size - 6f);
        } else {
            shapeRenderer.line(cx - 4f, y + 6f, cx - 4f, y + size - 6f);
            shapeRenderer.line(cx + 4f, y + 6f, cx + 4f, y + size - 6f);
        }
        shapeRenderer.end();
    }

    public float getCenterX() { return x + size / 2f; }
    public float getCenterY() { return y + size / 2f; }
    public float getSize() { return size; }
    public int getId() { return id; }
    public Color getColor() { return color; }
}
