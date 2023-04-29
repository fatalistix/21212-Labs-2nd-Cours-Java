package ru.nsu.balashov.mousetrapgame.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import ru.nsu.balashov.mousetrapgame.ImagesData;
import ru.nsu.balashov.mousetrapgame.LevelsData;
import ru.nsu.balashov.mousetrapgame.ScreenSwitcher;
import ru.nsu.balashov.mousetrapgame.controllers.switching.SwitchingController;

public class MainController implements SwitchingController {
    private ScreenSwitcher scSwitcher;
    @Override
    public void setScreenParent(ScreenSwitcher sc) {
        this.scSwitcher = sc;
    }
    @Override
    public void initController() {}



    @FXML
    private GridPane mainGridPane;
    @FXML
    private ImageView mouseImageView;
    @FXML
    private Button startButton;
    @FXML
    private ListView<GridPane> levelsListView;



    private final ObservableList<GridPane> levelsListViewItems = FXCollections.observableArrayList();

    private final static double LEVEL_NAME_LABEL_WIDTH_RATIO_PERCENTS = 40;
    private final static double BEST_TIME_LABEL_WIDTH_RATIO_PERCENTS = 20;
    private final static double BEST_PLAYER_LABEL_WIDTH_RATIO_PERCENTS = 40;

    @FXML
    public void initialize() {
//        mouseImageView.setImage(ImagesData.getInstance().getImageByName("Mouse"));
//        ArrayList<String> levelsNames = LevelsData.getInstance().getLevelsNames();
//        levelsListView.setItems(levelsListViewItems);
//        Font itemFont = new Font("SF Pro Display", 15);
//
//        for (String s : levelsNames) {
//
//            Label levelNameLabel  = new Label(s);
//            Label recordTimeLabel = new Label();
//            Label bestPlayerLabel = new Label();
//
//            ScoreData data = Objects.requireNonNull(HighScoresData.getInstance()).getBestLevelScore(s);
//            if (data == null) {
//                recordTimeLabel.setText("");
//                bestPlayerLabel.setText("Not passed");
//            } else {
//                recordTimeLabel.setText(String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(data.seconds()),
//                        TimeUnit.SECONDS.toMinutes(data.seconds()) % 60, data.seconds() % 60));
//                bestPlayerLabel.setText(data.author());
//            }
//
//            levelNameLabel.setFont(itemFont);
//            recordTimeLabel.setFont(itemFont);
//            bestPlayerLabel.setFont(itemFont);
//
//            GridPane item = new GridPane();
//
//            ColumnConstraints columnName = new ColumnConstraints();
//            ColumnConstraints columnTime = new ColumnConstraints();
//            ColumnConstraints columnBest = new ColumnConstraints();
//
//            columnName.setPercentWidth(LEVEL_NAME_LABEL_WIDTH_RATIO_PERCENTS);
//            columnTime.setPercentWidth(BEST_TIME_LABEL_WIDTH_RATIO_PERCENTS);
//            columnBest.setPercentWidth(BEST_PLAYER_LABEL_WIDTH_RATIO_PERCENTS);
//
//            item.getColumnConstraints().addAll(columnName, columnTime, columnBest);
//            item.getChildren().addAll(levelNameLabel, recordTimeLabel, bestPlayerLabel);
//
//            GridPane.setConstraints(levelNameLabel, 0, 0);
//            GridPane.setConstraints(recordTimeLabel, 1, 0);
//            GridPane.setConstraints(bestPlayerLabel, 2, 0);
//
//            GridPane.setHalignment(bestPlayerLabel, HPos.RIGHT);
//            GridPane.setHalignment(recordTimeLabel, HPos.CENTER);
//
//            levelsListViewItems.add(item);
//        }
//
    }


    @FXML
    private void startGame(MouseEvent mouseEvent) {
        scSwitcher.setScreen("game");
    }

    @FXML
    private void selectLevel(MouseEvent mouseEvent) {
        startButton.setDisable(levelsListView.getSelectionModel().isEmpty());
        LevelsData.getInstance().selectForLoading(((Label) levelsListView.getSelectionModel().getSelectedItem().getChildren().get(0)).getText());
    }


}
