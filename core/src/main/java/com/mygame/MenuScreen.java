package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Clickable menu prototype. Its generated skin can be replaced by the final artwork. */
public class MenuScreen extends ScreenAdapter {
    private final MainGame game;
    private final Stage stage;
    private final Skin skin;
    private final TextButton musicButton;

    public MenuScreen(MainGame game) {
        this.game = game;
        stage = new Stage(new FitViewport(GameScreen.WORLD_WIDTH, GameScreen.WORLD_HEIGHT));
        skin = createSkin();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(32f);
        stage.addActor(root);

        Table header = new Table();
        musicButton = button(musicLabel(), "music-toggle", () -> {
            game.toggleMusic();
            updateMusicButton();
        });
        TextButton settingsButton = button("AYARLAR", "settings", this::showSettings);
        header.add().expandX();
        header.add(musicButton).width(200f).height(56f).padRight(16f);
        header.add(settingsButton).width(160f).height(56f);
        root.add(header).growX();
        root.row();

        Table content = new Table();
        Label title = new Label("LAZER PUZZLE", skin);
        title.setFontScale(3f);
        Label subtitle = new Label("2 OYUNCULU LAZER BULMACASI", skin);
        subtitle.setColor(Color.valueOf("aab8cc"));
        subtitle.setFontScale(1.2f);
        content.add(title).padBottom(20f);
        content.row();
        content.add(subtitle).padBottom(52f);
        content.row();
        // Defer disposal of this stage until its input event has finished dispatching.
        content.add(button("OYNA", "play", () -> Gdx.app.postRunnable(game::startGame)))
            .width(300f).height(76f);
        root.add(content).grow();
        root.row();

        Label footer = new Label("Oyunda ESC ile menüye dön.", skin);
        footer.setColor(Color.valueOf("aab8cc"));
        root.add(footer).padTop(20f);
    }

    private Skin createSkin() {
        Skin result = new Skin();
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        Texture texture = new Texture(pixel);
        pixel.dispose();
        result.add("white", texture);

        BitmapFont font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        result.add("default-font", font);
        result.add("default", new Label.LabelStyle(font, Color.WHITE));

        TextButton.TextButtonStyle buttons = new TextButton.TextButtonStyle();
        buttons.font = font;
        buttons.fontColor = Color.WHITE;
        buttons.up = result.newDrawable("white", Color.valueOf("283b55"));
        buttons.over = result.newDrawable("white", Color.valueOf("3c597d"));
        buttons.down = result.newDrawable("white", Color.valueOf("176b87"));
        buttons.focused = buttons.over;
        result.add("default", buttons);

        Window.WindowStyle windows = new Window.WindowStyle(font, Color.WHITE,
            result.newDrawable("white", Color.valueOf("182536")));
        windows.stageBackground = result.newDrawable("white", new Color(0f, 0f, 0f, 0.65f));
        result.add("default", windows);
        return result;
    }

    private TextButton button(String text, String name, Runnable action) {
        TextButton button = new TextButton(text, skin);
        button.setName(name);
        button.getLabel().setFontScale(1.4f);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        return button;
    }

    private String musicLabel() {
        return game.isMusicEnabled() ? "Müzik: AÇIK" : "Müzik: KAPALI";
    }

    private void updateMusicButton() {
        musicButton.setText(musicLabel());
    }

    private void showSettings() {
        Dialog dialog = new Dialog("AYARLAR", skin);
        dialog.setName("settings-dialog");
        dialog.setMovable(false);
        dialog.pad(56f, 32f, 28f, 32f);
        dialog.getTitleLabel().setFontScale(1.4f);
        TextButton settingsMusic = button(musicLabel(), "settings-music-toggle", () -> {
            game.toggleMusic();
            updateMusicButton();
        });
        settingsMusic.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                settingsMusic.setText(musicLabel());
            }
        });
        dialog.getContentTable().add(settingsMusic).width(340f).height(56f).padBottom(20f);
        dialog.getContentTable().row();
        dialog.getContentTable().add(new Label("Henüz müzik eklenmedi.", skin)).padBottom(24f);
        TextButton closeButton = new TextButton("KAPAT", skin);
        closeButton.setName("close-settings");
        closeButton.getLabel().setFontScale(1.4f);
        dialog.button(closeButton);
        dialog.getButtonTable().getCell(closeButton).width(200f).height(52f);
        dialog.key(Input.Keys.ESCAPE, null);
        dialog.show(stage);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.08f, 0.10f, 0.14f, 1f);
        stage.act(Math.min(delta, 1f / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
