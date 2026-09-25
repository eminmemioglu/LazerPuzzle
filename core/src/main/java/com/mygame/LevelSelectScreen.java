package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Simple numbered level buttons; final artwork will be added later. */
public class LevelSelectScreen extends ScreenAdapter {
    private final MainGame game;
    private final Stage stage;
    private final Skin skin;
    private boolean leaving;

    public LevelSelectScreen(MainGame game) {
        this.game = game;
        stage = new Stage(new FitViewport(GameScreen.WORLD_WIDTH, GameScreen.WORLD_HEIGHT));
        skin = MenuSkin.create();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(32f);
        stage.addActor(root);

        Label title = new Label("BÖLÜM SEÇ", skin);
        title.setFontScale(2.5f);
        root.add(title).padBottom(48f);
        root.row();

        Table levels = new Table();
        for (LevelDefinition level : LevelDefinition.values()) {
            levels.add(button(Integer.toString(level.number), "level-" + level.number,
                () -> game.startGame(level))).size(112f).pad(12f);
        }
        root.add(levels).padBottom(48f);
        root.row();
        root.add(button("Geri", "back", game::showMenu)).width(220f).height(56f);
        root.row();
        root.add(new Label("ESC: Ana menü", skin)).padTop(24f);
    }

    private TextButton button(String text, String name, Runnable action) {
        TextButton button = new TextButton(text, skin);
        button.setName(name);
        button.getLabel().setFontScale(1.8f);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!leaving) {
                    leaving = true;
                    // Let this input event finish before disposing its stage.
                    Gdx.app.postRunnable(action);
                }
            }
        });
        return button;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        if (!leaving && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMenu();
            return;
        }
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
