package ru.nsu.balashov.mousetrapgame.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.nsu.balashov.mousetrapgame.HighScoresData;
import ru.nsu.balashov.mousetrapgame.ImagesData;
import ru.nsu.balashov.mousetrapgame.ScreenSwitcher;
import ru.nsu.balashov.mousetrapgame.controllers.switching.SwitchingController;
import ru.nsu.balashov.mousetrapgame.game.GameModel;
import ru.nsu.balashov.mousetrapgame.settings.GUIProperties;
import ru.nsu.balashov.mousetrapgame.settings.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import ru.nsu.balashov.mousetrapgame.HighScoresData.HighScoresFactory.ScoreData;

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
        reset();
        ArrayList<GameModel.FieldObjectBaseData> graphicObjects;
        try {
            graphicObjects = model.initLevel();
        } catch (GameModel.InitLevelException e) {
            scSwitcher.setScreen(GUIProperties.badLevelID);
            return;
        }

        levelNameLabel.setText(model.getLevelName());
        stepsMadeResultLabel.setText(String.valueOf(stepsMade));
        ScoreData bestScore = Objects.requireNonNull(HighScoresData.getInstance()).getBestLevelScore(model.getLevelName());
        if (bestScore == null) {
            bestScoreResultLabel.setText("Level not passed (yet?)");
        } else {
            bestScoreResultLabel.setText(String.format("%02d:%02d:%02d by %s with %d steps",
                    bestScore.seconds() / 3600, (bestScore.seconds() / 60) % 60, bestScore.seconds() % 60,
                    bestScore.author(), bestScore.steps()));
        }

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



    private int cellShiftByMouseShift(double mouseShift) {
        int fullyBlocksShifted = (int) (mouseShift / (Settings.GameScreenProperties.PIXELS_PER_BLOCK +
                Settings.GameScreenProperties.LINE_STROKE_WIDTH));
        return ((Math.abs(mouseShift - fullyBlocksShifted *
                (Settings.GameScreenProperties.PIXELS_PER_BLOCK + Settings.GameScreenProperties.LINE_STROKE_WIDTH)) >
                Settings.GameScreenProperties.PIXELS_FOR_SHIFT) ? (int) Math.signum(mouseShift) : 0) +
                fullyBlocksShifted;
    }

    private boolean shiftByOneCell(double mouseShift) {
        return (Math.abs(mouseShift) > Settings.GameScreenProperties.PIXELS_FOR_SHIFT);
    }

    @FXML
    private void onGameFieldMouseDragged(MouseEvent mouseEvent) {
        if (model.canSelectedBeDragged()) {
            if (model.canSelectedBeMovedRight() && mouseEvent.getX() >= startDragPosX ||
                    model.canSelectedBeMovedLeft() && mouseEvent.getX() <= startDragPosX) {
                graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setX(mouseEvent.getX() + imageShiftX);
            }
            if (model.canSelectedBeMovedUp() && mouseEvent.getY() <= startDragPosY ||
                    model.canSelectedBeMovedDown() && mouseEvent.getY() >= startDragPosY) {
                graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setY(mouseEvent.getY() + imageShiftY);
            }


//            if (shiftByOneCell(mouseEvent.getX() - startDragPosX) ||
//                    shiftByOneCell(mouseEvent.getY() - startDragPosY)) {
//                if (model.move(getCellByMousePos(mouseEvent.getX()) + cellShiftByMouseShift(mouseEvent.getX() - startDragPosX),
//                        getCellByMousePos(mouseEvent.getY()) + cellShiftByMouseShift(mouseEvent.getY() - startDragPosY))) {
//                    startDragPosX = getPixelByCord(model.getUpdatedX()) - imageShiftX;
//                    startDragPosY = getPixelByCord(model.getUpdatedY()) - imageShiftY;
//                } else {
////                    System.out.println(mouseEvent.getX() + " : " + mouseEvent.getY() + " : " + startDragPosX + " : " + startDragPosY);
//                    movePictureToCell(model.getUpdatedX(), model.getUpdatedY(), model.getSelectedForDraggingId());
//                }
//            }
            if ((model.canSelectedBeMovedRight() || model.canSelectedBeMovedLeft()) && shiftByOneCell(mouseEvent.getX() - startDragPosX)) {
                if (model.move(model.getSelectedX() + cellShiftByMouseShift(mouseEvent.getX() - startDragPosX), model.getSelectedY())) {
                    startDragPosX = getPixelByCord(model.getUpdatedX()) - imageShiftX;
                } else {
                    movePictureToCell(model.getUpdatedX(), model.getUpdatedY(), model.getSelectedForDraggingId());
                }
            }
            if ((model.canSelectedBeMovedDown() || model.canSelectedBeMovedUp()) && shiftByOneCell(mouseEvent.getY() - startDragPosY)) {
                if (model.move(model.getSelectedX(), model.getSelectedY() + cellShiftByMouseShift(mouseEvent.getY() - startDragPosY))) {
                    startDragPosY = getPixelByCord(model.getUpdatedY()) - imageShiftY;
                } else {
                    movePictureToCell(model.getUpdatedX(), model.getUpdatedY(), model.getSelectedForDraggingId());
                }
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
    private void onGameFieldMouseReleased() {
        if (model.canSelectedBeDragged()) {
            movePictureToCell(model.getUpdatedX(), model.getUpdatedY(), model.getSelectedForDraggingId());
            if (model.isObjectMoved()) {
                stepsMadeResultLabel.setText(String.valueOf(++stepsMade));
            }
        }
        model.unselectForDragging();
        if (model.gameEnded()) {
            timeline.stop();
            int rememberedSeconds = secondsPassed;
            int rememberedSteps   = stepsMade;

            Stage saveScoreStage = new Stage();

            GridPane saveScoreGrid = new GridPane();
            for (int i = 0; i < 2; ++i) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(50);
                saveScoreGrid.getColumnConstraints().add(cc);

                RowConstraints rc = new RowConstraints();
                rc.setPercentHeight(50);
                saveScoreGrid.getRowConstraints().add(rc);
            }

            TextField saveScoreTextField = new TextField();
            Button saveScoreButton = new Button("Save");
            Button dontSaveScoreButton = new Button("Don't save");

            saveScoreTextField.setFont(bestScoreFont);
            saveScoreButton.setFont(bestScoreFont);
            dontSaveScoreButton.setFont(bestScoreFont);

            GridPane.setConstraints(saveScoreTextField, 0, 0, 2, 1, HPos.CENTER, VPos.CENTER);
            GridPane.setConstraints(saveScoreButton, 0, 1, 1, 1, HPos.CENTER, VPos.CENTER);
            GridPane.setConstraints(dontSaveScoreButton, 1, 1, 1, 1, HPos.CENTER, VPos.CENTER);

            saveScoreButton.setOnAction(ae -> {
                Objects.requireNonNull(HighScoresData.getInstance()).saveScore(model.getLevelName(),
                        saveScoreTextField.getText(), rememberedSeconds, rememberedSteps);
                if (!Objects.requireNonNull(HighScoresData.getInstance()).storeScores()) {
                    Alert warnAlert = new Alert(Alert.AlertType.WARNING);
                    warnAlert.setHeaderText("Cannot store high scores");
                    warnAlert.setContentText("Cannot store high scores via some reasons");
                    warnAlert.showAndWait();
                }
                saveScoreStage.close();
            });

            dontSaveScoreButton.setOnAction(ae -> saveScoreStage.close());

            saveScoreGrid.getChildren().addAll(saveScoreButton, saveScoreTextField, dontSaveScoreButton);

            Scene saveScoreScene = new Scene(saveScoreGrid);
            saveScoreStage.setScene(saveScoreScene);
            saveScoreStage.setWidth(Settings.GameScreenProperties.SaveNamePopUp.STAGE_WIDTH);
            saveScoreStage.setHeight(Settings.GameScreenProperties.SaveNamePopUp.STAGE_HEIGHT);
            saveScoreStage.setResizable(false);
            saveScoreStage.show();

            scSwitcher.setScreen("endgame");
        }
    }

    @FXML
    private void onGameFieldMouseExited() {
        mouseOnImagePane = false;
    }

    @FXML
    private void onGameFieldMouseEntered() {
        mouseOnImagePane = true;
    }

    @FXML
    private void onBaseGridDragged(MouseEvent mouseEvent) {
        if (!mouseOnImagePane && model.canSelectedBeDragged()) {
            //            movePictureToCell(model.getUpdatedX(), model.getUpdatedY(), model.getSelectedForDraggingId());

            double shiftY = baseGridPane.getLayoutY() - imageFieldPane.getLayoutY();
            double shiftX = baseGridPane.getLayoutX() - imageFieldPane.getLayoutX();
            if (model.canSelectedBeMovedRight() && mouseEvent.getX() + shiftX >= startDragPosX ||
                    model.canSelectedBeMovedLeft() && mouseEvent.getX() + shiftX <= startDragPosX) {
                graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setX(mouseEvent.getX() + shiftX + imageShiftX);
            }
            if (model.canSelectedBeMovedUp() && mouseEvent.getY() + shiftY <= startDragPosY ||
                    model.canSelectedBeMovedDown() && mouseEvent.getY() + shiftY >= startDragPosY) {
                graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setY(mouseEvent.getY() + shiftY + imageShiftY);
            }
            if ((model.canSelectedBeMovedRight() || model.canSelectedBeMovedLeft()) && shiftByOneCell(mouseEvent.getX() + shiftX - startDragPosX)) {
                if (model.move(model.getSelectedX() + cellShiftByMouseShift(mouseEvent.getX() + shiftX - startDragPosX), model.getSelectedY())) {
                    startDragPosX = getPixelByCord(model.getUpdatedX()) - imageShiftX;
                } else {
                    movePictureToCell(model.getUpdatedX(), model.getUpdatedY(), model.getSelectedForDraggingId());
                }
            }
            if ((model.canSelectedBeMovedDown() || model.canSelectedBeMovedUp()) && shiftByOneCell(mouseEvent.getY() + shiftY - startDragPosY)) {
                if (model.move(model.getSelectedX(), model.getSelectedY() + cellShiftByMouseShift(mouseEvent.getY() + shiftY - startDragPosY))) {
                    startDragPosY = getPixelByCord(model.getUpdatedY()) - imageShiftY;
                } else {
                    movePictureToCell(model.getUpdatedX(), model.getUpdatedY(), model.getSelectedForDraggingId());
                }
            }
        }
    }


    @FXML
    private void resetGame(ActionEvent actionEvent) {
        HighScoresData.getInstance().clearScores();
    }

    @FXML
    private void showScores(ActionEvent actionEvent) {
        timeline.stop();
        scSwitcher.setScreen(GUIProperties.scoresID);
    }
}
