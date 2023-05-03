package ru.nsu.balashov.mousetrapgame.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.util.Duration;
import ru.nsu.balashov.mousetrapgame.HighScoresData;
import ru.nsu.balashov.mousetrapgame.ImagesData;
import ru.nsu.balashov.mousetrapgame.ScreenSwitcher;
import ru.nsu.balashov.mousetrapgame.controllers.switching.SwitchingController;
import ru.nsu.balashov.mousetrapgame.game.GameModel;
import ru.nsu.balashov.mousetrapgame.settings.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class GameController implements SwitchingController {
    private ScreenSwitcher scSwitcher;

    @Override
    public void setScreenParent(ScreenSwitcher sc) {
        this.scSwitcher = sc;
    }


    @FXML
    private GridPane baseGridPane;
    @FXML
    private ToolBar toolbar;
    @FXML
    private MenuButton toolbarGameMenuButton;
    @FXML
    private MenuButton toolbarHelpMenuButton;
    @FXML
    private MenuItem exitMenuItem;
    @FXML
    private MenuItem resetMenuItem;
    @FXML
    private MenuItem scoresMenuItem;
    @FXML
    private MenuItem helpMenuItem;
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private Label levelNameLabel;
    @FXML
    private Label currentTimeTextLabel;
    @FXML
    private Label currentTimeResultLabel;
    @FXML
    private Label stepsMadeTextLabel;
    @FXML
    private Label stepsMadeResultLabel;
    @FXML
    private Label bestScoreTextLabel;
    @FXML
    private Label bestScoreResultLabel;
    @FXML
    private Pane imageFieldPane;

    private final Font levelNameFont = new Font("SF Pro Display", Settings.GameScreenProperties.LEVEL_NAME_FONT_SIZE);
    private final Font currentTimeFont = new Font("SF Pro Display", Settings.GameScreenProperties.CURRENT_TIME_FONT_SIZE);
    private final Font stepsMadeFont = currentTimeFont;
    private final Font bestScoreFont = currentTimeFont;


    private int secondsPassed = 0;
    private int stepsMade = 0;
    private final Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), ae -> {
        int hours = secondsPassed / 3600;
        int minutes = (secondsPassed / 60) % 60;
        int seconds = secondsPassed % 60;
        currentTimeResultLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        secondsPassed++;
    }));

    private final GameModel model = new GameModel();
    private final HashMap<Integer, ImageView> graphicsPlayableObjects = new HashMap<>();
    private double imageShiftX;
    private double imageShiftY;
    private double startDragPosX;
    private double startDragPosY;
    private boolean mouseOnImagePane = false;


    private void drawGrid(Pane pane) {
        for (int i = 0; i < 5; ++i) {
            Line verticalLine = new Line();
            Line horizontalLine = new Line();

            verticalLine.setStrokeWidth(Settings.GameScreenProperties.LINE_STROKE_WIDTH);
            horizontalLine.setStrokeWidth(Settings.GameScreenProperties.LINE_STROKE_WIDTH);

            verticalLine.setDisable(false);
            horizontalLine.setDisable(false);

            verticalLine.setStartX((i + 1) * Settings.GameScreenProperties.PIXELS_PER_BLOCK + i);
            verticalLine.setStartY(0);
            verticalLine.setEndX((i + 1) * Settings.GameScreenProperties.PIXELS_PER_BLOCK + i);
            verticalLine.setEndY(Settings.GameScreenProperties.GAME_FIELD_SIZE);

            horizontalLine.setStartX(0);
            horizontalLine.setStartY((i + 1) * Settings.GameScreenProperties.PIXELS_PER_BLOCK + i);
            horizontalLine.setEndX(Settings.GameScreenProperties.GAME_FIELD_SIZE);
            horizontalLine.setEndY((i + 1) * Settings.GameScreenProperties.PIXELS_PER_BLOCK + i);


            pane.getChildren().add(verticalLine);
            pane.getChildren().add(horizontalLine);
        }
    }

    public void initialize() {
        levelNameLabel.setFont(levelNameFont);
        currentTimeTextLabel.setFont(currentTimeFont);
        currentTimeResultLabel.setFont(currentTimeFont);
        stepsMadeTextLabel.setFont(stepsMadeFont);
        stepsMadeResultLabel.setFont(stepsMadeFont);
        bestScoreTextLabel.setFont(bestScoreFont);
        bestScoreResultLabel.setFont(bestScoreFont);

        imageFieldPane.setMinHeight(Settings.GameScreenProperties.GAME_FIELD_SIZE);
        imageFieldPane.setMaxHeight(Settings.GameScreenProperties.GAME_FIELD_SIZE);
        imageFieldPane.setPrefHeight(Settings.GameScreenProperties.GAME_FIELD_SIZE);

        imageFieldPane.setMinWidth(Settings.GameScreenProperties.GAME_FIELD_SIZE);
        imageFieldPane.setMaxWidth(Settings.GameScreenProperties.GAME_FIELD_SIZE);
        imageFieldPane.setPrefWidth(Settings.GameScreenProperties.GAME_FIELD_SIZE);

        drawGrid(imageFieldPane);
    }


    @Override
    public void initController() {
        ArrayList<GameModel.FieldObjectBaseData> graphicObjects;
        try {
            graphicObjects = model.initLevel();
        } catch (GameModel.InitLevelException e) {
            //!!!!!! NEW WINDOW FOR BAD LEVELS
            throw new RuntimeException();
        }

        levelNameLabel.setText(model.getLevelName());
        stepsMadeResultLabel.setText(String.valueOf(stepsMade));

        for (int i = 0; i < graphicObjects.size(); ++i) {
            ImageView iv = new ImageView(ImagesData.getInstance().getImageByName(graphicObjects.get(i).id()));
            iv.setFitHeight(graphicObjects.get(i).height() *
                    (Settings.GameScreenProperties.PIXELS_PER_BLOCK + Settings.GameScreenProperties.LINE_STROKE_WIDTH) -
                    Settings.GameScreenProperties.LINE_STROKE_WIDTH);

            iv.setFitWidth (graphicObjects.get(i).width()  *
                    (Settings.GameScreenProperties.PIXELS_PER_BLOCK + Settings.GameScreenProperties.LINE_STROKE_WIDTH) -
                    Settings.GameScreenProperties.LINE_STROKE_WIDTH);

            iv.setX(getPixelByCord(graphicObjects.get(i).x()));
            iv.setY(getPixelByCord(graphicObjects.get(i).y()));
            imageFieldPane.getChildren().add(iv);
            graphicsPlayableObjects.put(i, iv);
        }
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }




    private void reset() {
        graphicsPlayableObjects.clear();
        imageFieldPane.getChildren().clear();
        drawGrid(imageFieldPane);
        secondsPassed = 0;
    }



    private int getCellByMousePos(double mousePos) {
        return (int) ((mousePos - (mousePos *
                Settings.GameScreenProperties.LINE_STROKE_WIDTH / Settings.GameScreenProperties.PIXELS_PER_BLOCK))
                / Settings.GameScreenProperties.PIXELS_PER_BLOCK);
    }

    private double getPixelByCord(int modelCord) {
        return modelCord * Settings.GameScreenProperties.PIXELS_PER_BLOCK + modelCord *
                Settings.GameScreenProperties.LINE_STROKE_WIDTH;
    }

    @FXML
    private void onGameFieldMousePressed(MouseEvent mouseEvent) {
        mouseOnImagePane = true;
        model.selectForDragging(getCellByMousePos(mouseEvent.getX()), getCellByMousePos(mouseEvent.getY()));
        if (model.canSelectedBeDragged()) {
            imageShiftX = graphicsPlayableObjects.get(model.getSelectedForDraggingId()).getX() - mouseEvent.getX();
            imageShiftY = graphicsPlayableObjects.get(model.getSelectedForDraggingId()).getY() - mouseEvent.getY();
            startDragPosX = mouseEvent.getX();
            startDragPosY = mouseEvent.getY();
        }
    }



//    @FXML
//    private void onGameFieldMouseDragged(MouseEvent mouseEvent) {
//        if (model.canSelectedBeDragged()) {
//            if (model.getSelectedX() != getCellByMousePos(mouseEvent.getX())
//                    || model.getSelectedY() != getCellByMousePos(mouseEvent.getY())) {
//                model.move(getCellByMousePos(mouseEvent.getX()), getCellByMousePos(mouseEvent.getY()));
//            }
//
//            if (mouseEvent.getX() >= startDragPosX) {
//                if (model.canSelectedBeMovedRight() || (getPixelByCord(model.getUpdatedX()) - imageShiftX >= mouseEvent.getX())) {
//                    graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setX(mouseEvent.getX() + imageShiftX);
//                }
//            } else {
//                if (model.canSelectedBeMovedLeft() || (getPixelByCord(model.getUpdatedX()) - imageShiftX <= mouseEvent.getX())) {
//                    graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setX(mouseEvent.getX() + imageShiftX);
//                }
//            }
//            if (mouseEvent.getY() >= startDragPosY) {
//                if (model.canSelectedBeMovedDown() || (getPixelByCord(model.getUpdatedY()) - imageShiftY >= mouseEvent.getY())) {
//                    graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setY(mouseEvent.getY() + imageShiftY);
//                }
//            } else {
//                if (model.canSelectedBeMovedUp() || (getPixelByCord(model.getUpdatedY()) - imageShiftY <= mouseEvent.getY())) {
//                    graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setY(mouseEvent.getY() + imageShiftY);
//                }
//            }
//        }
//    }

    private int cellShiftByMouseShift(double mouseShift) {
        int fullyBlocksShifted = (int) (mouseShift / (Settings.GameScreenProperties.PIXELS_PER_BLOCK +
                Settings.GameScreenProperties.LINE_STROKE_WIDTH));
        return ((Math.abs(mouseShift - fullyBlocksShifted *
                (Settings.GameScreenProperties.PIXELS_PER_BLOCK + Settings.GameScreenProperties.LINE_STROKE_WIDTH)) >
                Settings.GameScreenProperties.PIXELS_FOR_SHIFT) ? (int) Math.signum(mouseShift) : 0) + fullyBlocksShifted;
    }

    private boolean shiftByOneCell(double mouseShift) {
        return (Math.abs(mouseShift) > Settings.GameScreenProperties.PIXELS_FOR_SHIFT);
    }

    @FXML
    private void onGameFieldMouseDragged(MouseEvent mouseEvent) {
        if (model.canSelectedBeDragged()) {
            if (model.canSelectedBeMovedRight() && mouseEvent.getX() >= startDragPosX) {
                graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setX(mouseEvent.getX() + imageShiftX);
            }
            if (model.canSelectedBeMovedLeft() && mouseEvent.getX() <= startDragPosX) {
                graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setX(mouseEvent.getX() + imageShiftX);
            }
            if (model.canSelectedBeMovedUp() && mouseEvent.getY() <= startDragPosY) {
                graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setY(mouseEvent.getY() + imageShiftY);
            }
            if (model.canSelectedBeMovedDown() && mouseEvent.getY() >= startDragPosY) {
                graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setY(mouseEvent.getY() + imageShiftY);
            }

            if (shiftByOneCell(mouseEvent.getX() - startDragPosX) ||
                    shiftByOneCell(mouseEvent.getY() - startDragPosY)) {
                model.move(getCellByMousePos(mouseEvent.getX()), getCellByMousePos(mouseEvent.getY()));
            }
        }
    }






    private void movePictureToCell(int x, int y, int id) {
        graphicsPlayableObjects.get(id).setX(getPixelByCord(x));
        graphicsPlayableObjects.get(id).setY(getPixelByCord(y));
    }

    @FXML
    private void onGameFieldDragExited(MouseDragEvent mouseDragEvent) {
        if (model.canSelectedBeDragged()) {
            movePictureToCell(model.getUpdatedX(), model.getUpdatedY(), model.getSelectedForDraggingId());
        }
    }

    @FXML
    private void onGameFieldMouseReleased(MouseEvent mouseEvent) {
        if (model.canSelectedBeDragged()) {
            movePictureToCell(model.getUpdatedX(), model.getUpdatedY(), model.getSelectedForDraggingId());
        }
        model.unselectForDragging();
        if (model.gameEnded()) {
            timeline.stop();
            Objects.requireNonNull(HighScoresData.getInstance()).saveScore(model.getLevelName(), "xf", secondsPassed);
            if (!Objects.requireNonNull(HighScoresData.getInstance()).storeScores()) {
                Alert warnAlert = new Alert(Alert.AlertType.WARNING);
                warnAlert.setHeaderText("Cannot store high scores");
                warnAlert.setContentText("Cannot store high scores via some reasons");
                warnAlert.showAndWait();
            }
            scSwitcher.setScreen("endgame");
        }
    }

    @FXML
    private void onGameFieldMouseExited(MouseEvent mouseEvent) {
        mouseOnImagePane = false;
    }

    @FXML
    private void onGameFieldMouseEntered(MouseEvent mouseEvent) {
        mouseOnImagePane = true;
    }

    @FXML
    private void onBaseGridDragged(MouseEvent mouseEvent) {
        if (!mouseOnImagePane && model.canSelectedBeDragged()) {
            movePictureToCell(model.getUpdatedX(), model.getUpdatedY(), model.getSelectedForDraggingId());
        }
    }


    @FXML
    private void resetGame(ActionEvent actionEvent) {
        reset();
        initController();
    }

}
