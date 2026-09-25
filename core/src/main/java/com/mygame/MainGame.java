package com.mygame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

/**
 * Entry game class for libGDX lifecycle management.
 * Owns screen transitions and settings shared during the current session.
 */
public class MainGame extends Game {
    private boolean musicEnabled = true;

    @Override
    public void create() {
        showMenu();
    }

    public void showMenu() {
        changeScreen(new MenuScreen(this));
    }

    public void startGame() {
        changeScreen(new GameScreen(this));
    }

    private void changeScreen(Screen nextScreen) {
        Screen previousScreen = getScreen();
        setScreen(nextScreen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void toggleMusic() {
        musicEnabled = !musicEnabled;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
