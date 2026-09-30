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
    public enum Facing { FRONT, LEFT, RIGHT }

    private Facing facing = Facing.FRONT;
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
        faceMovement(moveX);
        updateMovement(delta, moveX, Gdx.input.isKeyJustPressed(keyJump), platforms, mapWidth, mapHeight);
        // Carry objects by the resolved displacement, including falls and collisions.
        float deltaX = x - previousX;
        float deltaY = y - previousY;

        updateInteractions(delta, deltaX, deltaY, mirrors, prism, mapWidth, mapHeight);
    }

    /** Resolve both bodies before moving held objects or checking interactions. */
    public static void updatePair(float delta, Player first, Player second, Array<Mirror> mirrors,
                                  Prism prism, Array<Platform> platforms, float mapWidth, float mapHeight) {
        delta = MathUtils.clamp(delta, 0f, 0.1f);
        float firstX = first.x, firstY = first.y;
        float secondX = second.x, secondY = second.y;
        float firstInput = first.horizontalInput(), secondInput = second.horizontalInput();
        first.faceMovement(firstInput);
        second.faceMovement(secondInput);
        movePair(delta, first, second, firstInput, secondInput,
            Gdx.input.isKeyJustPressed(first.keyJump), Gdx.input.isKeyJustPressed(second.keyJump),
            platforms, mapWidth, mapHeight);
        first.updateInteractions(delta, first.x - firstX, first.y - firstY, mirrors, prism, mapWidth, mapHeight);
        second.updateInteractions(delta, second.x - secondX, second.y - secondY, mirrors, prism, mapWidth, mapHeight);
    }

    private float horizontalInput() {
        return (Gdx.input.isKeyPressed(keyRight) ? 1f : 0f) - (Gdx.input.isKeyPressed(keyLeft) ? 1f : 0f);
    }

    private void faceMovement(float input) {
        if (input < 0f) facing = Facing.LEFT;
        else if (input > 0f) facing = Facing.RIGHT;
    }

    public Facing getFacing() { return facing; }

    private void updateInteractions(float delta, float deltaX, float deltaY, Array<Mirror> mirrors,
                                    Prism prism, float mapWidth, float mapHeight) {

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
        prepareJump(jumpPressed, platforms, null);
        while (remaining > 0f) {
            float step = Math.min(remaining, 1f / 120f);
            remaining -= step;
            moveHorizontal(MathUtils.clamp(moveX, -1f, 1f) * speed * step, platforms, mapWidth);
            fall(step, platforms, mapHeight);
        }
    }

    /** Equal-mass contact resolution, independent of which player is updated first. */
    static void movePair(float delta, Player a, Player b, float inputA, float inputB,
                         boolean jumpA, boolean jumpB, Array<Platform> platforms,
                         float mapWidth, float mapHeight) {
        float remaining = MathUtils.clamp(delta, 0f, 0.1f);
        if (remaining == 0f) return;
        a.prepareJump(jumpA, platforms, b);
        b.prepareJump(jumpB, platforms, a);
        while (remaining > 0f) {
            float step = Math.min(remaining, 1f / 120f);
            remaining -= step;
            float ax = a.x, bx = b.x, ay = a.y, by = b.y;
            boolean aRidesB = a.standsOn(b) && a.velocityY <= b.velocityY;
            boolean bRidesA = b.standsOn(a) && b.velocityY <= a.velocityY;

            a.moveHorizontal(MathUtils.clamp(inputA, -1f, 1f) * a.speed * step, platforms, mapWidth);
            b.moveHorizontal(MathUtils.clamp(inputB, -1f, 1f) * b.speed * step, platforms, mapWidth);
            if (aRidesB) a.moveHorizontal(b.x - bx, platforms, mapWidth);
            if (bRidesA) b.moveHorizontal(a.x - ax, platforms, mapWidth);

            if (a.y < b.y + b.size - 0.001f && a.y + a.size > b.y + 0.001f) {
                Player left = ax < bx ? a : b;
                Player right = left == a ? b : a;
                float overlap = left.x + left.size - right.x;
                if (overlap > 0f) {
                    // Both share the displacement; an immovable wall blocks the pusher too.
                    left.moveHorizontal(-overlap / 2f, platforms, mapWidth);
                    right.moveHorizontal(overlap / 2f, platforms, mapWidth);
                    overlap = Math.max(0f, left.x + left.size - right.x);
                    left.moveHorizontal(-overlap, platforms, mapWidth);
                    overlap = Math.max(0f, left.x + left.size - right.x);
                    right.moveHorizontal(overlap, platforms, mapWidth);
                }
            }

            a.fall(step, platforms, mapHeight);
            b.fall(step, platforms, mapHeight);
            if (a.overlapsHorizontally(b)) {
                if (ay >= by + b.size - 0.01f) {
                    landOnPlayer(a, b, platforms, mapHeight);
                } else if (by >= ay + a.size - 0.01f) {
                    landOnPlayer(b, a, platforms, mapHeight);
                }
            }
        }
    }

    private static void landOnPlayer(Player upper, Player lower, Array<Platform> platforms, float mapHeight) {
        float overlap = lower.y + lower.size - upper.y;
        if (overlap < -0.01f) return;
        if (overlap > 0f) upper.moveVertical(overlap, platforms, mapHeight);
        float blocked = lower.y + lower.size - upper.y;
        if (blocked > 0.001f) {
            // A ceiling above a stack stops both bodies instead of crushing them together.
            lower.moveVertical(-blocked, platforms, mapHeight);
            lower.velocityY = Math.min(0f, lower.velocityY);
        }
        upper.velocityY = lower.velocityY;
        upper.grounded = true;
    }

    private boolean overlapsHorizontally(Player other) {
        return x < other.x + other.size && x + size > other.x;
    }

    private boolean standsOn(Player other) {
        return overlapsHorizontally(other) && Math.abs(y - other.y - other.size) < 0.01f;
    }

    private void prepareJump(boolean jumpPressed, Array<Platform> platforms, Player other) {
        grounded = other != null && standsOn(other) && velocityY <= other.velocityY + 0.01f;
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
    }

    private void moveHorizontal(float amount, Array<Platform> platforms, float mapWidth) {
        float oldX = x;
        x = MathUtils.clamp(x + amount, 4f, mapWidth - size - 4f);
        for (Platform platform : platforms) {
            if (y >= platform.top() || y + size <= platform.y) continue;
            if (oldX + size <= platform.x && x + size > platform.x) {
                x = platform.x - size;
            } else if (oldX >= platform.right() && x < platform.right()) {
                x = platform.right();
            }
        }
    }

    private void fall(float step, Array<Platform> platforms, float mapHeight) {
        velocityY = Math.max(velocityY - GRAVITY * step, -MAX_FALL_SPEED);
        grounded = false;
        moveVertical(velocityY * step, platforms, mapHeight);
    }

    private void moveVertical(float amount, Array<Platform> platforms, float mapHeight) {
        float oldY = y;
        y += amount;
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

    public void renderTether(ShapeRenderer shapeRenderer) {
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
    }

    public void render(ShapeRenderer shapeRenderer) {
        renderTether(shapeRenderer);
        float cx = getCenterX();
        float cy = getCenterY();

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
