package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Directional idle and jump poses, aligned to the player's feet. */
public final class PlayerSprite implements Disposable {
    private final TextureRegion front;
    private final TextureRegion left;
    private final TextureRegion right;
    private final Texture jumpTexture;
    private final TextureRegion[] jumpFrames;
    private final Texture eastJumpTexture;
    private final TextureRegion[] eastJumpFrames;
    private final TextureRegion[] westJumpFrames;
    private final JumpAnimation jumpAnimation = new JumpAnimation();

    public PlayerSprite() {
        front = load("south");
        left = load("west");
        right = load("east");
        jumpTexture = new Texture(Gdx.files.classpath("characters/blue-robot/jump/south.png"));
        jumpFrames = loadJump(jumpTexture, "south");
        eastJumpTexture = new Texture(Gdx.files.classpath("characters/blue-robot/jump/east.png"));
        eastJumpFrames = loadJump(eastJumpTexture, "east");
        westJumpFrames = new TextureRegion[eastJumpFrames.length];
        for (int i = 0; i < eastJumpFrames.length; i++) {
            // Copy the region so mirroring left never changes the right-facing frames.
            westJumpFrames[i] = new TextureRegion(eastJumpFrames[i]);
            westJumpFrames[i].flip(true, false);
        }
    }

    private TextureRegion[] loadJump(Texture texture, String direction) {
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        JsonValue frames = new JsonReader().parse(Gdx.files.classpath(
            "characters/blue-robot/jump/" + direction + ".json")).get("frames");
        if (frames.size != 9) throw new IllegalArgumentException("Jump requires nine frames: " + direction);
        TextureRegion[] regions = new TextureRegion[frames.size];
        for (int i = 0; i < frames.size; i++) {
            JsonValue frame = frames.get(i);
            regions[i] = new TextureRegion(texture, frame.getInt("x"), frame.getInt("y"),
                frame.getInt("width"), frame.getInt("height"));
        }
        return regions;
    }

    public void update(float delta, Player player) {
        jumpAnimation.update(delta, player.isGrounded(), player.getVerticalVelocity());
    }

    private TextureRegion load(String direction) {
        // Classpath resources also work when running the packaged distribution elsewhere.
        Pixmap pixels = new Pixmap(Gdx.files.classpath(
            "characters/blue-robot/Idle/rotations/" + direction + ".png"));
        try {
            int minX = pixels.getWidth(), minY = pixels.getHeight(), maxX = -1, maxY = -1;
            for (int y = 0; y < pixels.getHeight(); y++) {
                for (int x = 0; x < pixels.getWidth(); x++) {
                    if ((pixels.getPixel(x, y) & 0xff) == 0) continue;
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
            if (maxX < 0) throw new IllegalArgumentException("Empty character sprite: " + direction);
            Texture texture = new Texture(pixels);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            return new TextureRegion(texture, minX, minY, maxX - minX + 1, maxY - minY + 1);
        } finally {
            pixels.dispose();
        }
    }

    /** Caller owns the batch begin/end and camera projection. */
    public void draw(SpriteBatch batch, Player player) {
        TextureRegion pose;
        switch (player.getFacing()) {
            case LEFT: pose = left; break;
            case RIGHT: pose = right; break;
            default: pose = front;
        }
        float scale = player.getSize() / pose.getRegionHeight();
        int jumpFrame = jumpAnimation.getFrameIndex();
        if (jumpFrame >= 0) {
            TextureRegion[] directionalFrames;
            switch (player.getFacing()) {
                case LEFT: directionalFrames = westJumpFrames; break;
                case RIGHT: directionalFrames = eastJumpFrames; break;
                default: directionalFrames = jumpFrames;
            }
            pose = directionalFrames[jumpFrame];
            // Constant scale preserves squash/tuck poses; the GIF's baked-in travel is removed.
            scale = player.getSize() / directionalFrames[0].getRegionHeight();
        }
        float height = pose.getRegionHeight() * scale;
        float width = pose.getRegionWidth() * scale;
        batch.draw(pose, player.getCenterX() - width / 2f,
            player.getCenterY() - player.getSize() / 2f, width, height);
    }

    @Override
    public void dispose() {
        front.getTexture().dispose();
        left.getTexture().dispose();
        right.getTexture().dispose();
        jumpTexture.dispose();
        eastJumpTexture.dispose();
    }
}
