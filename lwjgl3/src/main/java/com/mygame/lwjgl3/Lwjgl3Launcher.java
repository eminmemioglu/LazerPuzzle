package com.mygame.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.mygame.MainGame;

/**
 * Desktop launcher entry point using libGDX's recommended LWJGL3 backend.
 */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("2D Game Prototype");
        configuration.setWindowedMode(1280, 720);
        configuration.useVsync(true);
        configuration.setForegroundFPS(60);

        new Lwjgl3Application(new MainGame(), configuration);
    }
}
