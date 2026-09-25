package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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
        skin = MenuSkin.create();

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
        content.add(button("OYNA", "play", () -> Gdx.app.postRunnable(game::showLevelSelection)))
            .width(300f).height(76f);
        root.add(content).grow();
        root.row();

        Label footer = new Label("Oyna ile bölüm seç.", skin);
        footer.setColor(Color.valueOf("aab8cc"));
        root.add(footer).padTop(20f);
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
        stage.getViewport().apply();
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
