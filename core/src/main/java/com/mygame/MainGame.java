package com.mygame;

import com.badlogic.gdx.Game;

/**
 * Entry game class for libGDX lifecycle management.
 * Switches immediately to the main 2D GameScreen.
 */
public class MainGame extends Game {

    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
