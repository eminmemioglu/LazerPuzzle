package com.mygame;

/** Initial layouts. Level one preserves the original puzzle; the others are placeholders. */
public enum LevelDefinition {
    LEVEL_1(1, 400f, 680f, 520f, 135f, 680f, 200f, 45f, 1140f, 560f, 1140f, 180f),
    LEVEL_2(2, 460f, 740f, 480f, 110f, 740f, 240f, 70f, 1080f, 500f, 1080f, 220f),
    LEVEL_3(3, 520f, 820f, 540f, 155f, 760f, 180f, 25f, 1160f, 440f, 1040f, 120f),
    LEVEL_4(4, 580f, 860f, 460f, 95f, 840f, 260f, 85f, 1040f, 580f, 1160f, 140f);

    public final int number;
    public final float prismX;
    public final float mirror1X, mirror1Y, mirror1Angle;
    public final float mirror2X, mirror2Y, mirror2Angle;
    public final float redX, redY, cyanX, cyanY;

    LevelDefinition(int number, float prismX,
                    float mirror1X, float mirror1Y, float mirror1Angle,
                    float mirror2X, float mirror2Y, float mirror2Angle,
                    float redX, float redY, float cyanX, float cyanY) {
        this.number = number;
        this.prismX = prismX;
        this.mirror1X = mirror1X;
        this.mirror1Y = mirror1Y;
        this.mirror1Angle = mirror1Angle;
        this.mirror2X = mirror2X;
        this.mirror2Y = mirror2Y;
        this.mirror2Angle = mirror2Angle;
        this.redX = redX;
        this.redY = redY;
        this.cyanX = cyanX;
        this.cyanY = cyanY;
    }
}
