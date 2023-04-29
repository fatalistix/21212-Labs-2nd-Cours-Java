package ru.nsu.balashov.mousetrapgame.game;

public class FieldObject {
    private int x;
    private int y;
    private final int sizeX;
    private final int sizeY;
    private final boolean canMoveVertical;
    private final boolean canMoveHorizontal;
    private final String graphicsID;
    private final int fieldID;

    FieldObject(FieldObjectParams p, int x, int y, int fieldID) {
        this.x = x;
        this.y = y;
        this.sizeX = p.sizeX();
        this.sizeY = p.sizeY();
        this.canMoveVertical = p.canMoveVertical();
        this.canMoveHorizontal = p.canMoveHorizontal();
        this.graphicsID = p.graphicsID();
        this.fieldID = fieldID;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getGraphicsID() {
        return graphicsID;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public boolean canMoveHorizontal() {
        return canMoveHorizontal;
    }

    public boolean canMoveVertical() {
        return canMoveVertical;
    }

    public int getFieldID() {
        return fieldID;
    }

    public void moveXRight() { ++x; }
    public void moveXLeft() { --x; }
    public void moveYUp() { --y; }
    public void moveYDown() { ++y; }
}
