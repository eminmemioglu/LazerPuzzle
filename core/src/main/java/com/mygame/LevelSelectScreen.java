package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Pixel-art level cards with previews of each level's actual starting layout. */
public class LevelSelectScreen extends ScreenAdapter {
    private static final String[] ACCENTS = {"75dcf3", "99e5a5", "edc17b", "c2a8ef"};
    private final MainGame game;
    private final Stage stage;
    private final Skin skin;
    private boolean leaving;

    public LevelSelectScreen(MainGame game) {
        this.game = game;
        stage = new Stage(new FitViewport(GameScreen.WORLD_WIDTH, GameScreen.WORLD_HEIGHT));
        skin = MenuSkin.create();
        Texture background = loadTexture("background");
        Image backdrop = new Image(background);
        backdrop.setBounds(0, 0, 1280, 720); backdrop.setTouchable(Touchable.disabled);
        stage.addActor(backdrop);
        plate(0, 0, 1280, 720, new Color(0.015f, 0.035f, 0.07f, 0.70f));
        plate(0, 602, 1280, 118, new Color(0.02f, 0.04f, 0.075f, 0.82f));
        plate(0, 0, 1280, 68, new Color(0.02f, 0.04f, 0.075f, 0.88f));
        label("Lichtstrahlen", 32, 640, 600, 34, 1.2f, "eaf8ec", Align.left);
        label("BÖLÜMLER", 908, 640, 340, 30, 0.85f, "88a9b8", Align.right);
        label("BÖLÜM SEÇ", 0, 526, 1280, 68, 3f, "eaf8ec", Align.center);
        label("BİRLİKTE ÇÖZ. IŞIĞI HEDEFE ULAŞTIR.", 0, 487, 1280, 30,
            0.85f, "a4cad2", Align.center);

        // Reuse the title screen's generated plate; only its center stretches.
        NinePatchDrawable frame = new NinePatchDrawable(new NinePatch(
            new TextureRegion(loadTexture("play-button"), 70, 0, 84, 48), 10, 10, 16, 16));
        for (LevelDefinition level : LevelDefinition.values()) {
            Color accent = Color.valueOf(ACCENTS[level.number - 1]);
            Button.ButtonStyle style = new Button.ButtonStyle();
            style.up = frame.tint(Color.valueOf("aac3ce"));
            style.over = frame.tint(accent);
            style.down = frame.tint(Color.valueOf("62868e"));
            Button card = new Button(style);
            card.setName("level-" + level.number);
            card.setBounds(108 + (level.number - 1) * 272, 192, 248, 278);
            card.pad(22, 16, 22, 16);
            card.addListener(navigate(() -> game.startGame(level)));

            Table heading = new Table(); heading.setTouchable(Touchable.disabled);
            Label number = new Label(Integer.toString(level.number), skin);
            number.setColor(accent); number.setFontScale(2.7f);
            heading.add(number).width(52).height(56).left();
            Label title = new Label("BÖLÜM", skin); title.setFontScale(1f);
            title.setColor(Color.valueOf("d9e8e9"));
            heading.add(title).expandX().right();
            card.add(heading).growX().padBottom(8); card.row();
            card.add(new LayoutPreview(level, accent)).size(216, 122).padBottom(14); card.row();
            Label enter = new Label("BÖLÜME GİR", skin);
            enter.setFontScale(0.85f); enter.setColor(accent); enter.setTouchable(Touchable.disabled);
            card.add(enter).height(22);
            stage.addActor(card);
        }

        TextButton.TextButtonStyle backStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        backStyle.up = frame;
        backStyle.over = frame.tint(Color.valueOf("b2f3ff"));
        backStyle.down = frame.tint(Color.valueOf("7196a3"));
        backStyle.focused = backStyle.over;
        TextButton back = new TextButton("GERİ", backStyle);
        back.setName("back"); back.getLabel().setFontScale(1.4f);
        back.setBounds(108, 94, 220, 70);
        back.addListener(navigate(game::showMenu)); stage.addActor(back);
        label("BİR BÖLÜM SEÇEREK BAŞLA", 380, 112, 792, 28, 0.85f, "a4cad2", Align.right);
        label("ESC: ANA MENÜ", 0, 24, 1280, 24, 0.75f, "88a9b8", Align.center);
    }

    private ChangeListener navigate(Runnable action) {
        return new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (!leaving) {
                    leaving = true;
                    // Dispatch input fully before the old screen and its textures are disposed.
                    Gdx.app.postRunnable(action);
                }
            }
        };
    }

    private Texture loadTexture(String name) {
        Texture texture = new Texture(Gdx.files.classpath("ui/lichtstrahlen/" + name + ".png"));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        skin.add("levels-" + name, texture); return texture;
    }

    private void plate(float x, float y, float width, float height, Color color) {
        Image image = new Image(skin.newDrawable("white", color));
        image.setBounds(x, y, width, height); image.setTouchable(Touchable.disabled); stage.addActor(image);
    }

    private void label(String text, float x, float y, float width, float height,
                       float scale, String color, int alignment) {
        Label label = new Label(text, skin); label.setFontScale(scale); label.setColor(Color.valueOf(color));
        label.setAlignment(alignment); label.setBounds(x, y, width, height);
        label.setTouchable(Touchable.disabled); stage.addActor(label);
    }

    /** Schematic coordinates come from LevelDefinition; no generated screenshots or fabricated layouts. */
    private class LayoutPreview extends Actor {
        private final LevelDefinition level;
        private final Color accent;
        private final Array<Platform> platforms;
        private final Texture pixel = skin.get("white", Texture.class);
        LayoutPreview(LevelDefinition level, Color accent) {
            this.level = level; this.accent = accent; platforms = level.createPlatforms();
            setTouchable(Touchable.disabled);
        }
        @Override public void draw(Batch batch, float parentAlpha) {
            // Button is a non-transforming Table: drawChildren temporarily puts children in stage coordinates.
            rect(batch, 0, 0, getWidth(), getHeight(), Color.valueOf("091723"), parentAlpha);
            rect(batch, 0, getHeight() - 2, getWidth(), 2, accent, parentAlpha * 0.45f);
            for (int i = 1; i < 8; i++) rect(batch, i * getWidth() / 8f, 4, 1, getHeight() - 8,
                Color.valueOf("172e3a"), parentAlpha);
            for (int i = 1; i < 4; i++) rect(batch, 0, i * getHeight() / 4f, getWidth(), 1,
                Color.valueOf("172e3a"), parentAlpha);
            float sx = getWidth() / GameScreen.WORLD_WIDTH;
            float sy = getHeight() / GameScreen.WORLD_HEIGHT;
            for (Platform p : platforms) {
                rect(batch, p.x * sx, p.y * sy, p.width * sx,
                    Math.max(2, p.height * sy), Color.valueOf("4d6d7b"), parentAlpha);
                rect(batch, p.x * sx, (p.y + p.height) * sy - 1,
                    p.width * sx, 1, accent, parentAlpha * 0.8f);
            }
            // Fixed emitter and its initial incoming beam, before it reaches the prism.
            rect(batch, 34 * sx, 342 * sy, 6, 6, Color.valueOf("eaf8ec"), parentAlpha);
            rect(batch, 64 * sx, 360 * sy, (level.prismX - 64) * sx, 1,
                Color.valueOf("d5e6e8"), parentAlpha * 0.75f);
            diamond(batch, level.prismX * sx, 360 * sy, 5, Color.valueOf("75dcf3"), parentAlpha);
            mirror(batch, level.mirror1X * sx, level.mirror1Y * sy, level.mirror1Angle, parentAlpha);
            mirror(batch, level.mirror2X * sx, level.mirror2Y * sy, level.mirror2Angle, parentAlpha);
            receiver(batch, level.redX * sx, level.redY * sy, Color.valueOf("ef8a91"), parentAlpha);
            receiver(batch, level.cyanX * sx, level.cyanY * sy, Color.valueOf("75dcf3"), parentAlpha);
            // Two player markers at the starting positions.
            rect(batch, 140 * sx - 2, 64 * sy + 1, 4, 6, Color.valueOf("75dcf3"), parentAlpha);
            rect(batch, 220 * sx - 2, 64 * sy + 1, 4, 6, Color.valueOf("99e5a5"), parentAlpha);
            batch.setColor(Color.WHITE);
        }
        private void rect(Batch batch, float x, float y, float w, float h, Color color, float alpha) {
            batch.setColor(color.r, color.g, color.b, color.a * alpha);
            batch.draw(pixel, getX() + x, getY() + y, w, h);
        }
        private void diamond(Batch batch, float x, float y, int radius, Color color, float alpha) {
            for (int row = -radius; row <= radius; row++) {
                int half = radius - Math.abs(row);
                rect(batch, x - half, y + row, half * 2 + 1, 1, color, alpha);
            }
        }
        private void mirror(Batch batch, float x, float y, float angle, float alpha) {
            for (int i = -5; i <= 5; i++) rect(batch, x + MathUtils.cosDeg(angle) * i,
                y + MathUtils.sinDeg(angle) * i, 2, 2, Color.valueOf("cce9eb"), alpha);
        }
        private void receiver(Batch batch, float x, float y, Color color, float alpha) {
            rect(batch, x - 4, y - 4, 8, 8, color, alpha);
            rect(batch, x - 2, y - 2, 4, 4, Color.valueOf("091723"), alpha);
            rect(batch, x - 1, y - 1, 2, 2, color, alpha);
        }
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) {
        if (!leaving && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            leaving = true; game.showMenu(); return;
        }
        ScreenUtils.clear(0.02f, 0.035f, 0.06f, 1);
        stage.getViewport().apply(); stage.act(Math.min(delta, 1f / 30f)); stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
