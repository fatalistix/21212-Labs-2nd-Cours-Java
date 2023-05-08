package ru.nsu.balashov.mousetrapgame.game;

import ru.nsu.balashov.mousetrapgame.LevelsData;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class GameModel {
    public record FieldObjectBaseData(int x, int y, int height, int width, String id) {}
    private final int[][] field = new int[6][6];
    private String levelName;
    private int piecesPlaced = 0;
    private boolean mousePlaced = false;
    private int mouseID;
    private final ArrayList<FieldObject> placedObjects = new ArrayList<>();

    private int selectedForDraggingId = -2;
    private int selectedForDraggingX;
    private int selectedForDraggingY;
    private boolean objectMoved = false;

    public String getLevelName() {
        return levelName;
    }



    public void printField() {
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 6; ++j) {
                System.out.print(field[i][j] + " ");
            }
            System.out.println();
        }
    }


    private boolean canBePlaced(FieldObject fo) {
        if (fo.getX() + fo.getSizeX() > 6 || fo.getY() + fo.getSizeY() > 6) {
            return false;
        }
        for (int i = 0; i < fo.getSizeY(); ++i) {
            for (int j = 0; j < fo.getSizeX(); ++j) {
                if (field[fo.getY() + i][fo.getX() + j] != -1) {
                    return false;
                }
            }
        }
        return true;
    }

    private void reset() {
        for (int i = 0; i < 6; ++i) {
            Arrays.fill(field[i], -1);
        }
        selectedForDraggingId = -2;
        mousePlaced = false;
        piecesPlaced = 0;
        placedObjects.clear();
    }

    private void place(FieldObject fo) {
        for (int i = 0; i < fo.getSizeY(); ++i) {
            for (int j = 0; j < fo.getSizeX(); ++j) {
                field[fo.getY() + i][fo.getX() + j] = fo.getFieldID();
            }
        }
    }

    public ArrayList<FieldObjectBaseData> initLevel() throws InitLevelException {
        File levelFile = Objects.requireNonNull(LevelsData.getInstance().getSelectedForLoading());
        this.reset();
        ArrayList<FieldObjectBaseData> currentFieldObjects = new ArrayList<>();

        try (Scanner sc = new Scanner(levelFile)) {
            levelName = sc.nextLine();

            String fieldUnitStr;
            while (sc.hasNext()) {
                fieldUnitStr = sc.next();
                FieldObject fo;
                if (fieldUnitStr.charAt(0) == 'm') {
                    if (mousePlaced) {
                        throw new IllegalArgumentException("More than one mouse in level file");
                    }
                    mousePlaced = true;
                    mouseID = piecesPlaced;
                    fo = new FieldObject(FieldObjectsData.getInstance().getParams(fieldUnitStr), 0, fieldUnitStr.charAt(1) - '0', piecesPlaced++);
                } else {
                    fo = new FieldObject(FieldObjectsData.getInstance().getParams(fieldUnitStr), fieldUnitStr.charAt(1) - '0', fieldUnitStr.charAt(2) - '0', piecesPlaced++);
                }
                if (!canBePlaced(fo)) {
                    throw new IllegalArgumentException("Objects placing conflict");
                }
                place(fo);
                placedObjects.add(fo);
                currentFieldObjects.add(new FieldObjectBaseData(fo.getX(), fo.getY(), fo.getSizeY(), fo.getSizeX(), fo.getGraphicsID()));
            }
            return currentFieldObjects;
        } catch (FileNotFoundException e) {
            //? never thrown
            System.err.println("Exception in MainController:initLevel() with message: " + e.getMessage());
            throw new InitLevelException(e);
        } catch (NoSuchElementException | IllegalArgumentException e) {
            throw new InitLevelException("Invalid level properties", e);
        }
    }

    public void selectForDragging(int x, int y) {
        if (field[y][x] != -1) {
            selectedForDraggingId = field[y][x];
            selectedForDraggingX = x;
            selectedForDraggingY = y;
        }
    }

    public void unselectForDragging() {
        selectedForDraggingId = -2;
    }

    public boolean canSelectedBeDragged() {
        return selectedForDraggingId != -2;
    }

    public int getSelectedForDraggingId() {
        return selectedForDraggingId;
    }

    public boolean canSelectedBeMovedRight() {
        if (canSelectedBeDragged()) {
            FieldObject o = placedObjects.get(selectedForDraggingId);
            if (o.canMoveHorizontal() && o.getX() + o.getSizeX() < 6) {
                for (int i = 0; i < o.getSizeY(); ++i) {
                    if (field[o.getY() + i][o.getX() + o.getSizeX()] != -1) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean canSelectedBeMovedLeft() {
        if (canSelectedBeDragged()) {
            FieldObject o = placedObjects.get(selectedForDraggingId);
            if (o.canMoveHorizontal() && o.getX() > 0) {
                for (int i = 0; i < o.getSizeY(); ++i) {
                    if (field[o.getY() + i][o.getX() - 1] != -1) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean canSelectedBeMovedDown() {
        if (canSelectedBeDragged()) {
            FieldObject o = placedObjects.get(selectedForDraggingId);
            if (o.canMoveVertical() && o.getY() + o.getSizeY() < 6) {
                for (int i = 0; i < o.getSizeX(); ++i) {
                    if (field[o.getY() + o.getSizeY()][o.getX() + i] != -1) {
                        return false;
                    }
                }
                return true;

            }
        }
        return false;
    }

    public boolean canSelectedBeMovedUp() {
        if (canSelectedBeDragged()) {
            FieldObject o = placedObjects.get(selectedForDraggingId);
            if (o.canMoveVertical() && o.getY() > 0) {
                for (int i = 0; i < o.getSizeX(); ++i) {
                    if (field[o.getY() - 1][o.getX() + i] != -1) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }


    private void moveSelectedRight() {
        FieldObject fo = placedObjects.get(selectedForDraggingId);
        for (int i = 0; i < fo.getSizeY(); ++i) {
            for (int j = fo.getSizeX() - 1; j >= 0; --j) {
                field[fo.getY() + i][fo.getX() + j + 1] = field[fo.getY() + i][fo.getX() + j];
                field[fo.getY() + i][fo.getX() + j] = -1;
            }
        }
        fo.moveXRight();
    }

    private void moveSelectedLeft() {
        FieldObject fo = placedObjects.get(selectedForDraggingId);
        for (int i = 0; i < fo.getSizeY(); ++i) {
            for (int j = 0; j < fo.getSizeX(); ++j) {
                field[fo.getY() + i][fo.getX() + j - 1] = field[fo.getY() + i][fo.getX() + j];
                field[fo.getY() + i][fo.getX() + j] = -1;
            }
        }
        fo.moveXLeft();
    }

    private void moveSelectedDown() {
        FieldObject fo = placedObjects.get(selectedForDraggingId);
        for (int i = fo.getSizeY() - 1; i >= 0; --i) {
            for (int j = 0; j < fo.getSizeX(); ++j) {
                field[fo.getY() + i + 1][fo.getX() + j] = field[fo.getY() + i][fo.getX() + j];
                field[fo.getY() + i][fo.getX() + j] = -1;
            }
        }
        fo.moveYDown();
    }

    private void moveSelectedUp() {
        FieldObject fo = placedObjects.get(selectedForDraggingId);
        for (int i = 0; i < fo.getSizeY(); ++i) {
            for (int j = 0; j < fo.getSizeX(); ++j) {
                field[fo.getY() + i - 1][fo.getX() + j] = field[fo.getY() + i][fo.getX() + j];
                field[fo.getY() + i][fo.getX() + j] = -1;
            }
        }
        fo.moveYUp();
    }



    public boolean move(int x, int y) {
        objectMoved = false;
        if (x > selectedForDraggingX) {
            for (int i = 0; i < x - selectedForDraggingX; ++i) {
                if (!canSelectedBeMovedRight()) {
                    break;
                }
                moveSelectedRight();
                ++selectedForDraggingX;
                objectMoved = true;
            }
        } else {
            for (int i = 0; i < selectedForDraggingX - x; ++i) {
                if (!canSelectedBeMovedLeft()) {
                    break;
                }
                moveSelectedLeft();
                --selectedForDraggingX;
                objectMoved = true;
            }
        }
        if (y > selectedForDraggingY) {
            for (int i = 0; i < y - selectedForDraggingY; ++i) {
                if (!canSelectedBeMovedDown()) {
                    break;
                }
                moveSelectedDown();
                ++selectedForDraggingY;
                objectMoved = true;
            }
        } else {
            for (int i = 0; i < selectedForDraggingY - y; ++i) {
                if (!canSelectedBeMovedUp()) {
                    break;
                }
                moveSelectedUp();
                --selectedForDraggingY;
                objectMoved = true;
            }
        }
        return objectMoved;
    }

    public boolean isObjectMoved() {
        return objectMoved;
    }

    public int getSelectedX() {
        return selectedForDraggingX;
    }

    public int getSelectedY() {
        return selectedForDraggingY;
    }

    public int getUpdatedX() {
        return placedObjects.get(selectedForDraggingId).getX();
    }

    public int getUpdatedY() {
        return placedObjects.get(selectedForDraggingId).getY();
    }

    public boolean gameEnded() {
        return placedObjects.get(mouseID).getX() + 1 == 5;
    }















    public static class InitLevelException extends Exception {
        InitLevelException(String message, Throwable cause) {
            super(message, cause);
        }

        InitLevelException(Throwable cause) {
            super(cause);
        }
    }
}
