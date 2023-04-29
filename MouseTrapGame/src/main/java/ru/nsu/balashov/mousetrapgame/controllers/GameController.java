package ru.nsu.balashov.mousetrapgame.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
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
    @FXML
    private Pane imageFieldPane;
    @FXML
    private GridPane textPane;
    @FXML
    private MenuItem exitMenuItem;
    @FXML
    private MenuItem resetMenuItem;
    @FXML
    private MenuItem scoresMenuItem;


    private int secondsPassed = 0;
    private final Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), ae -> { secondsPassed++; }));


    private ScreenSwitcher scSwitcher;
    private final GameModel model = new GameModel();
    private final HashMap<Integer, ImageView> graphicsPlayableObjects = new HashMap<>();
    private double imageShiftX;
    private double imageShiftY;
    private double startDragPosX;
    private double startDragPosY;

    @Override
    public void setScreenParent(ScreenSwitcher sc) {
        this.scSwitcher = sc;
    }


    private void reset() {
        graphicsPlayableObjects.clear();
        imageFieldPane.getChildren().clear();
    }



    @Override
    public void initController() {
        reset();
        ArrayList<GameModel.FieldObjectBaseData> graphicObjects;
        try {
            graphicObjects = model.initLevel();
        } catch (GameModel.InitLevelException e) {
            //!!!!!! NEW WINDOW FOR BAD LEVELS
            throw new RuntimeException();
        }

        for (int i = 0; i < graphicObjects.size(); ++i) {
            ImageView iv = new ImageView(ImagesData.getInstance().getImageByName(graphicObjects.get(i).id()));
            iv.setX(graphicObjects.get(i).x() * Settings.fieldCeilScale);
            iv.setY(graphicObjects.get(i).y() * Settings.fieldCeilScale);
            imageFieldPane.getChildren().add(iv);
            graphicsPlayableObjects.put(i, iv);
        }
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }


    @FXML
    private void onGameFieldMousePressed(MouseEvent mouseEvent) {
        model.selectForDragging((int) (mouseEvent.getX() / Settings.fieldCeilScale), (int) (mouseEvent.getY() / Settings.fieldCeilScale));
        if (model.canSelectedBeDragged()) {
//            System.out.println(model.canSelectedBeMovedRight());
            imageShiftX = graphicsPlayableObjects.get(model.getSelectedForDraggingId()).getX() - mouseEvent.getX();
            imageShiftY = graphicsPlayableObjects.get(model.getSelectedForDraggingId()).getY() - mouseEvent.getY();
            startDragPosX = mouseEvent.getX();
            startDragPosY = mouseEvent.getY();
//            System.out.println(imageShiftX + " : " + imageShiftY + " : " + startDragPosX + " : " + startDragPosY);
        }
    }


    @FXML
    private void onGameFieldMouseDragged(MouseEvent mouseEvent) {
//        System.out.println(model.canSelectedBeDragged());
        if (model.canSelectedBeDragged()) {
//            System.out.println(mouseEvent.getX() + " : " + startDragPosX + " : " + model.canSelectedBeDragged() + " : " + model.canSelectedBeMovedRight());
            if (mouseEvent.getX() >= startDragPosX) {
                if (model.canSelectedBeMovedRight() || (model.getUpdatedX() * Settings.fieldCeilScale - imageShiftX >= mouseEvent.getX())) {
                    graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setX(mouseEvent.getX() + imageShiftX);
                }
            } else {
                if (model.canSelectedBeMovedLeft() || (model.getUpdatedX() * Settings.fieldCeilScale - imageShiftX <= mouseEvent.getX())) {
                    graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setX(mouseEvent.getX() + imageShiftX);
                }
            }
            if (mouseEvent.getY() >= startDragPosY) {
                if (model.canSelectedBeMovedDown() || (model.getUpdatedY() * Settings.fieldCeilScale - imageShiftY >= mouseEvent.getY())) {
                    graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setY(mouseEvent.getY() + imageShiftY);
                }
            } else {
                if (model.canSelectedBeMovedUp() || (model.getUpdatedY() * Settings.fieldCeilScale - imageShiftY <= mouseEvent.getY())) {
                    graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setY(mouseEvent.getY() + imageShiftY);
                }
            }

            if (model.getSelectedX() != (int) (mouseEvent.getX() / Settings.fieldCeilScale)
                    || model.getSelectedY() != (int) (mouseEvent.getY() / Settings.fieldCeilScale)) {
//                System.out.println(model.getSelectedX() + " : " + ((int) (mouseEvent.getX() / Settings.fieldCeilScale)));
//                System.out.println((int) (mouseEvent.getX() / Settings.fieldCeilScale) + " : " + (int) (mouseEvent.getY() / Settings.fieldCeilScale));
                model.move((int) (mouseEvent.getX() / Settings.fieldCeilScale), (int) (mouseEvent.getY() / Settings.fieldCeilScale));
            }
        }
    }



    @FXML
    private void onGameFieldMouseReleased(MouseEvent mouseEvent) {
        if (model.canSelectedBeDragged()) {
            graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setX(model.getUpdatedX() * Settings.fieldCeilScale);
            graphicsPlayableObjects.get(model.getSelectedForDraggingId()).setY(model.getUpdatedY() * Settings.fieldCeilScale);
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
    private void resetGame(ActionEvent actionEvent) {
        initController();
    }
}
