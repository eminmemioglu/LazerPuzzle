package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
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
    private static final float GRAVITY = 1800f;
    private static final float JUMP_SPEED = 720f;
    private static final float MAX_FALL_SPEED = 1100f;
    private float velocityY;
    private boolean grounded;
    private final Color color;

    // Key configuration
    private final int keyJump;
    private final int keyLeft;
    private final int keyRight;
    private final int keyGrab;
    private final int keyRotLeft;
    private final int keyRotRight;

    private Mirror grabbedMirror = null;
    private Prism grabbedPrism = null;
    private final float grabRange = 70f;

    public Player(int id, float startX, float startY, Color color,
                  int jump, int left, int right,
                  int grab, int rotLeft, int rotRight) {
        this.id = id;
        this.x = startX - size / 2f;
        this.y = startY - size / 2f;
        this.color = color;

        this.keyJump = jump;
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
            Input.Keys.W, Input.Keys.A, Input.Keys.D,
            Input.Keys.SPACE, Input.Keys.Q, Input.Keys.E
        );
    }

    public static Player createPlayer2(float startX, float startY) {
        return new Player(
            2, startX, startY,
            Color.valueOf("4ade80"), // Bright Emerald Green
            Input.Keys.UP, Input.Keys.LEFT, Input.Keys.RIGHT,
            Input.Keys.ENTER, Input.Keys.K, Input.Keys.L
        );
    }

    public void update(float delta, Array<Mirror> mirrors, Prism prism, Array<Platform> platforms,
                       float mapWidth, float mapHeight) {
        delta = MathUtils.clamp(delta, 0f, 0.1f);
        float moveX = 0f;
        if (Gdx.input.isKeyPressed(keyLeft)) moveX -= 1f;
        if (Gdx.input.isKeyPressed(keyRight)) moveX += 1f;

        float previousX = x;
        float previousY = y;
        updateMovement(delta, moveX, Gdx.input.isKeyJustPressed(keyJump), platforms, mapWidth, mapHeight);
        // Carry objects by the resolved displacement, including falls and collisions.
        float deltaX = x - previousX;
        float deltaY = y - previousY;

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

    /** Input-independent movement so collisions can be checked without a graphics window. */
    void updateMovement(float delta, float moveX, boolean jumpPressed, Array<Platform> platforms,
                        float mapWidth, float mapHeight) {
        float remaining = MathUtils.clamp(delta, 0f, 0.1f);
        if (remaining == 0f) return;

        grounded = false;
        for (Platform platform : platforms) {
            if (x < platform.right() && x + size > platform.x
                && Math.abs(y - platform.top()) < 0.01f && velocityY <= 0f) {
                grounded = true;
                break;
            }
        }
        if (jumpPressed && grounded) {
            velocityY = JUMP_SPEED;
            grounded = false;
        }

        // Small steps keep side/corner collisions stable, even during a slow frame.
        while (remaining > 0f) {
            float step = Math.min(remaining, 1f / 120f);
            remaining -= step;
            float oldX = x;
            x = MathUtils.clamp(x + MathUtils.clamp(moveX, -1f, 1f) * speed * step,
                4f, mapWidth - size - 4f);
            for (Platform platform : platforms) {
                if (y >= platform.top() || y + size <= platform.y) continue;
                if (oldX + size <= platform.x && x + size > platform.x) {
                    x = platform.x - size;
                } else if (oldX >= platform.right() && x < platform.right()) {
                    x = platform.right();
                }
            }

            float oldY = y;
            velocityY = Math.max(velocityY - GRAVITY * step, -MAX_FALL_SPEED);
            y += velocityY * step;
            grounded = false;
            for (Platform platform : platforms) {
                if (x >= platform.right() || x + size <= platform.x) continue;
                if (oldY >= platform.top() && y <= platform.top()) {
                    y = platform.top();
                    velocityY = 0f;
                    grounded = true;
                } else if (oldY + size <= platform.y && y + size >= platform.y) {
                    y = platform.y - size;
                    velocityY = 0f;
                }
            }
            if (y > mapHeight - size - 4f) {
                y = mapHeight - size - 4f;
                velocityY = Math.min(velocityY, 0f);
            }
        }
    }

    public boolean isGrounded() { return grounded; }

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
