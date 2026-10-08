package com.mygame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

/** Shared placeholder styling until final menu artwork is available. */
final class MenuSkin {
    private MenuSkin() {}

    static Skin create() {
        Skin result = new Skin();
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        Texture texture = new Texture(pixel);
        pixel.dispose();
        result.add("white", texture);

        BitmapFont font = new BitmapFont(Gdx.files.classpath("ui/lichtstrahlen/pixel-font.fnt"));
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
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

}
