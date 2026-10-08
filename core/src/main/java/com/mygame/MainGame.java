package com.mygame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;

/**
 * Entry game class for libGDX lifecycle management.
 * Owns screen transitions and settings shared during the current session.
 */
public class MainGame extends Game {
    private boolean musicEnabled = true;
    private Music menuMusic;
    private Preferences settings;

    @Override
    public void create() {
        settings = Gdx.app.getPreferences("lichtstrahlen-settings");
        musicEnabled = settings.getBoolean("music-enabled", true);
        menuMusic = Gdx.audio.newMusic(Gdx.files.classpath("audio/lichtstrahlen-menu.wav"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.35f);
        showMenu();
    }

    public void showMenu() {
        changeScreen(new MenuScreen(this));
    }

    public void showLevelSelection() {
        changeScreen(new LevelSelectScreen(this));
    }

    public void startGame(LevelDefinition level) {
        changeScreen(new GameScreen(this, level));
    }

    private void changeScreen(Screen nextScreen) {
        Screen previousScreen = getScreen();
        setScreen(nextScreen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
        updateMusic();
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void toggleMusic() {
        musicEnabled = !musicEnabled;
        settings.putBoolean("music-enabled", musicEnabled).flush();
        updateMusic();
    }

    private void updateMusic() {
        if (musicEnabled && !(getScreen() instanceof GameScreen)) menuMusic.play();
        else menuMusic.pause();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
        if (menuMusic != null) menuMusic.dispose();
    }
}
