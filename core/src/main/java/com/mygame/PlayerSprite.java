package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/** PixelLab's static directional poses, aligned to the player's feet and collision height. */
public final class PlayerSprite implements Disposable {
    private final TextureRegion front;
    private final TextureRegion left;
    private final TextureRegion right;

    public PlayerSprite() {
        front = load("south");
        left = load("west");
        right = load("east");
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
        float height = player.getSize();
        float width = height * pose.getRegionWidth() / pose.getRegionHeight();
        batch.draw(pose, player.getCenterX() - width / 2f,
            player.getCenterY() - height / 2f, width, height);
    }

    @Override
    public void dispose() {
        front.getTexture().dispose();
        left.getTexture().dispose();
        right.getTexture().dispose();
    }
}
