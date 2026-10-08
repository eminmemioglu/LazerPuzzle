package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Lichtstrahlen's pixel-art title screen and session controls. */
public class MenuScreen extends ScreenAdapter {
    private final MainGame game;
    private final Stage stage;
    private final Skin skin;
    private final Button musicButton;
    private final Label musicHint;
    private boolean leaving;
    private Dialog settingsDialog;

    public MenuScreen(MainGame game) {
        this.game = game;
        stage = new Stage(new FitViewport(GameScreen.WORLD_WIDTH, GameScreen.WORLD_HEIGHT));
        skin = MenuSkin.create();
        addImage("background", 0, 0, 1280, 720);
        addPlate(0, 0, 1280, 720, new Color(0.015f, 0.035f, 0.07f, 0.45f));
        addPlate(0, 582, 1280, 138, new Color(0.02f, 0.04f, 0.075f, 0.82f));
        addPlate(0, 0, 1280, 94, new Color(0.02f, 0.04f, 0.075f, 0.88f));
        addPlate(232, 226, 816, 330, new Color(0.025f, 0.055f, 0.09f, 0.92f));
        addPlate(232, 548, 104, 4, Color.valueOf("75dcf3"));
        addPlate(944, 548, 104, 4, Color.valueOf("99e5a5"));
        addPlate(232, 226, 104, 4, Color.valueOf("75dcf3"));
        addPlate(944, 226, 104, 4, Color.valueOf("99e5a5"));
        addLabel("IŞIĞI BİRLİKTE YÖNLENDİR", 32, 620, 740, 32, 1.0f, "88a9b8", Align.left);
        addLabel("Lichtstrahlen", 232, 414, 816, 90, 4.4f, "eaf8ec", Align.center);
        addLabel("İKİ ROBOT. BİR BULMACA.", 232, 372, 816, 30, 1.1f, "a4cad2", Align.center);
        addPlate(564, 349, 68, 3, Color.valueOf("75dcf3"));
        addPlate(648, 349, 68, 3, Color.valueOf("99e5a5"));
        RobotPreview blue = new RobotPreview("blue-robot");
        blue.setBounds(524, 250, 84, 84); stage.addActor(blue);
        RobotPreview green = new RobotPreview("green-robot");
        green.setBounds(672, 250, 84, 84); stage.addActor(green);

        Drawable frame = iconFrame();
        Button.ButtonStyle icons = new Button.ButtonStyle();
        icons.up = frame;
        icons.over = ((NinePatchDrawable) frame).tint(Color.valueOf("b2f3ff"));
        icons.down = ((NinePatchDrawable) frame).tint(Color.valueOf("6f9eac"));
        icons.checked = ((NinePatchDrawable) frame).tint(Color.valueOf("af7480"));
        icons.checkedOver = icons.over;
        musicButton = new Button(icons);
        musicButton.setName("music-toggle");
        musicButton.add(new MusicIcon(loadTexture("music"))).size(40);
        musicButton.setBounds(1068, 612, 68, 68);
        musicButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.toggleMusic(); updateMusicButton();
            }
        });
        stage.addActor(musicButton);
        Button settings = new Button(icons);
        settings.setName("settings");
        settings.add(new Image(loadTexture("settings"))).size(40);
        settings.setBounds(1152, 612, 68, 68);
        settings.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                settings.setChecked(false); showSettings();
            }
        });
        stage.addActor(settings);
        TextTooltip.TextTooltipStyle tooltip = new TextTooltip.TextTooltipStyle();
        tooltip.label = skin.get(Label.LabelStyle.class);
        tooltip.background = skin.newDrawable("white", Color.valueOf("102331"));
        musicButton.addListener(new TextTooltip("MÜZİĞİ AÇ / KAPAT", tooltip));
        settings.addListener(new TextTooltip("AYARLAR", tooltip));
        musicHint = addLabel("", 990, 578, 230, 25, 0.65f, "a4cad2", Align.right);
        updateMusicButton();

        TextButton.TextButtonStyle playStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        // The generated sheet contains two plates; use the complete right plate and stretch only its center.
        NinePatchDrawable playArt = new NinePatchDrawable(new NinePatch(
            new TextureRegion(loadTexture("play-button"), 70, 0, 84, 48), 10, 10, 16, 16));
        playStyle.up = playArt;
        playStyle.over = playArt.tint(Color.valueOf("b2f3ff"));
        playStyle.down = playArt.tint(Color.valueOf("7196a3"));
        playStyle.focused = playStyle.over;
        TextButton play = new TextButton("OYNA", playStyle);
        play.setName("play"); play.getLabel().setFontScale(2f);
        play.setBounds(460, 108, 360, 108);
        play.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (!leaving) { leaving = true; Gdx.app.postRunnable(game::showLevelSelection); }
            }
        });
        stage.addActor(play);
        addLabel("2 OYUNCULU LAZER BULMACASI", 0, 42, 1280, 24, 0.9f, "b1c7cd", Align.center);
        addLabel("OYNA İLE BÖLÜM SEÇ", 0, 16, 1280, 22, 0.65f, "70949e", Align.center);
    }

    private Texture loadTexture(String name) {
        Texture texture = new Texture(Gdx.files.classpath("ui/lichtstrahlen/" + name + ".png"));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        skin.add("menu-" + name, texture);
        return texture;
    }

    private void addImage(String name, float x, float y, float w, float h) {
        Image image = new Image(loadTexture(name)); image.setBounds(x, y, w, h);
        image.setTouchable(Touchable.disabled); stage.addActor(image);
    }

    private void addPlate(float x, float y, float w, float h, Color color) {
        Image image = new Image(skin.newDrawable("white", color)); image.setBounds(x, y, w, h);
        image.setTouchable(Touchable.disabled); stage.addActor(image);
    }

    private Label addLabel(String text, float x, float y, float w, float h, float scale, String color, int align) {
        Label label = new Label(text, skin); label.setFontScale(scale); label.setColor(Color.valueOf(color));
        label.setAlignment(align); label.setBounds(x, y, w, h); label.setTouchable(Touchable.disabled);
        stage.addActor(label); return label;
    }

    private Drawable iconFrame() {
        Pixmap pixels = new Pixmap(20, 20, Pixmap.Format.RGBA8888);
        pixels.setColor(Color.valueOf("0c1c28")); pixels.fillRectangle(2, 2, 16, 16);
        pixels.setColor(Color.valueOf("466b7a")); pixels.drawRectangle(2, 2, 16, 16);
        pixels.setColor(Color.valueOf("8fbcc8")); pixels.drawLine(4, 2, 15, 2);
        Texture texture = new Texture(pixels); pixels.dispose();
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        skin.add("icon-frame-texture", texture);
        return new NinePatchDrawable(new NinePatch(texture, 5, 5, 5, 5));
    }

    private String musicLabel() { return game.isMusicEnabled() ? "MÜZİK: AÇIK" : "MÜZİK: KAPALI"; }

    private void updateMusicButton() {
        musicButton.setProgrammaticChangeEvents(false);
        musicButton.setChecked(!game.isMusicEnabled());
        musicButton.setProgrammaticChangeEvents(true);
        musicHint.setText(musicLabel());
    }

    private void showSettings() {
        if (settingsDialog != null && settingsDialog.getStage() != null) return;
        Dialog dialog = new Dialog("AYARLAR", skin);
        settingsDialog = dialog; dialog.setName("settings-dialog"); dialog.setMovable(false);
        dialog.pad(60, 32, 28, 32); dialog.getTitleLabel().setFontScale(1.4f);
        TextButton settingsMusic = new TextButton(musicLabel(), skin);
        settingsMusic.setName("settings-music-toggle"); settingsMusic.getLabel().setFontScale(1.2f);
        settingsMusic.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.toggleMusic(); updateMusicButton(); settingsMusic.setText(musicLabel());
            }
        });
        dialog.getContentTable().add(settingsMusic).width(340).height(64).padBottom(20);
        dialog.getContentTable().row();
        dialog.getContentTable().add(new Label("MENÜ MÜZİĞİ", skin)).padBottom(24);
        TextButton close = new TextButton("KAPAT", skin); close.setName("close-settings");
        close.getLabel().setFontScale(1.2f); dialog.button(close);
        dialog.getButtonTable().getCell(close).width(200).height(56);
        dialog.key(Input.Keys.ESCAPE, null); dialog.show(stage);
    }

    private class MusicIcon extends Image {
        MusicIcon(Texture texture) { super(texture); setTouchable(Touchable.disabled); }
        @Override public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
            if (!game.isMusicEnabled()) {
                // Pixel steps make the muted state readable without relying on color alone.
                Texture white = skin.get("white", Texture.class);
                batch.setColor(Color.valueOf("f38b91"));
                for (int i = 0; i < 7; i++) batch.draw(white,
                    getX() + 4 + i * 4, getY() + 4 + i * 4, 5, 5);
            }
            batch.setColor(Color.WHITE);
        }
    }

    private class RobotPreview extends Actor {
        private final TextureRegion[] frames;
        private final float[] ends;
        private final float duration;
        private float time;
        RobotPreview(String name) {
            Texture texture = new Texture(Gdx.files.classpath("characters/" + name + "/breathing/south.png"));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            skin.add("preview-" + name, texture);
            JsonValue data = new JsonReader().parse(Gdx.files.classpath("characters/" + name + "/breathing/south.json")).get("frames");
            frames = new TextureRegion[data.size]; ends = new float[data.size]; float total = 0;
            for (int i = 0; i < data.size; i++) {
                JsonValue f = data.get(i);
                frames[i] = new TextureRegion(texture, f.getInt("x"), f.getInt("y"), f.getInt("width"), f.getInt("height"));
                total += f.getInt("durationMs") / 1000f; ends[i] = total;
            }
            duration = total; setTouchable(Touchable.disabled);
        }
        @Override public void act(float delta) { super.act(delta); time = (time + delta) % duration; }
        @Override public void draw(Batch batch, float parentAlpha) {
            int index = 0; while (index < frames.length - 1 && time >= ends[index]) index++;
            TextureRegion f = frames[index]; float scale = getHeight() / frames[0].getRegionHeight();
            batch.setColor(1, 1, 1, parentAlpha);
            batch.draw(f, getX() + (getWidth() - f.getRegionWidth() * scale) / 2,
                getY(), f.getRegionWidth() * scale, f.getRegionHeight() * scale);
            batch.setColor(Color.WHITE);
        }
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) {
        ScreenUtils.clear(0.02f, 0.035f, 0.06f, 1); stage.getViewport().apply();
        stage.act(Math.min(delta, 1f / 30f)); stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() {
        TooltipManager.getInstance().hideAll();
        if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null);
    }
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
