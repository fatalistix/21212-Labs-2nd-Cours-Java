package ru.nsu.balashov.mousetrapgame.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import ru.nsu.balashov.mousetrapgame.HighScoresData;
import ru.nsu.balashov.mousetrapgame.HighScoresData.HighScoresFactory.ScoreData;
import ru.nsu.balashov.mousetrapgame.ImagesData;
import ru.nsu.balashov.mousetrapgame.LevelsData;
import ru.nsu.balashov.mousetrapgame.ScreenSwitcher;
import ru.nsu.balashov.mousetrapgame.controllers.switching.SwitchingController;
import ru.nsu.balashov.mousetrapgame.settings.Settings;

import java.util.ArrayList;
import java.util.Objects;


public class MainController implements SwitchingController {
    private ScreenSwitcher scSwitcher;
    @Override
    public void setScreenParent(ScreenSwitcher sc) {
        this.scSwitcher = sc;
    }



    @FXML
    private GridPane mainGridPane;
    @FXML
    private ImageView mouseImageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private AnchorPane buttonAnchorPane;
    @FXML
    private Button startButton;
    @FXML
    private ListView<GridPane> levelsListView;

    Font listViewItemFont = new Font("SF Pro Display", Settings.MainMenuScreenProperties.SELECT_LEVEL_FONT_SIZE);
    Font titleFont        = new Font("SF Pro Display Bold", Settings.MainMenuScreenProperties.TITLE_FONT_SIZE);
    Font descriptionFont  = new Font("SF Pro Display Regular Italic", Settings.MainMenuScreenProperties.DESCRIPTION_FONT_SIZE);
    Font startButtonFont  = new Font("SF Pro Display Regular", Settings.MainMenuScreenProperties.START_GAME_BUTTON_FONT_SIZE);

    private final ObservableList<GridPane> levelsListViewItems = FXCollections.observableArrayList();



    @FXML
    public void initialize() {
        mouseImageView.setImage(ImagesData.getInstance().getImageByName("Mouse"));
        mouseImageView.setFitHeight(Settings.MainMenuScreenProperties.MOUSE_IMAGE_HEIGHT);
        mouseImageView.setFitWidth(Settings.MainMenuScreenProperties.MOUSE_IMAGE_WIDTH);

        titleLabel.setFont(titleFont);
        descriptionLabel.setFont(descriptionFont);
        levelsListView.setPrefWidth(Settings.MainMenuScreenProperties.SELECT_LEVEL_LIST_VIEW_PREF_WIDTH);
        buttonAnchorPane.setPrefWidth(Settings.MainMenuScreenProperties.START_GAME_PANE_BUTTON_PREF_WIDTH);
        startButton.setFont(startButtonFont);

        levelsListView.setItems(levelsListViewItems);
    }

    @Override
    public void initController() {
        levelsListViewItems.clear();
        ArrayList<String> levelsNames = LevelsData.getInstance().getLevelsNames();

        for (String s : levelsNames) {

            Label levelNameLabel  = new Label(s);
            Label recordTimeLabel = new Label();
            Label bestPlayerLabel = new Label();

            ScoreData data = Objects.requireNonNull(HighScoresData.getInstance()).getBestLevelScore(s);
            if (data == null) {
                recordTimeLabel.setText("");
                bestPlayerLabel.setText("Not passed");
            } else {
                recordTimeLabel.setText(String.format("%02d:%02d:%02d", data.seconds() / 3600,
                        (data.seconds() / 60) % 60, data.seconds() % 60));
                bestPlayerLabel.setText(data.author());
            }

            levelNameLabel.setFont(listViewItemFont);
            recordTimeLabel.setFont(listViewItemFont);
            bestPlayerLabel.setFont(listViewItemFont);

            GridPane item = new GridPane();

            ColumnConstraints columnName = new ColumnConstraints();
            ColumnConstraints columnTime = new ColumnConstraints();
            ColumnConstraints columnBest = new ColumnConstraints();

            columnName.setPercentWidth(Settings.MainMenuScreenProperties.LEVEL_NAME_LABEL_WIDTH_RATIO_PERCENTS);
            columnTime.setPercentWidth(Settings.MainMenuScreenProperties.BEST_TIME_LABEL_WIDTH_RATIO_PERCENTS);
            columnBest.setPercentWidth(Settings.MainMenuScreenProperties.BEST_PLAYER_LABEL_WIDTH_RATIO_PERCENTS);

            item.getColumnConstraints().addAll(columnName, columnTime, columnBest);
            item.getChildren().addAll(levelNameLabel, recordTimeLabel, bestPlayerLabel);

            GridPane.setConstraints(levelNameLabel, 0, 0);
            GridPane.setConstraints(recordTimeLabel, 1, 0);
            GridPane.setConstraints(bestPlayerLabel, 2, 0);

            GridPane.setHalignment(bestPlayerLabel, HPos.RIGHT);
            GridPane.setHalignment(recordTimeLabel, HPos.CENTER);

            levelsListViewItems.add(item);
        }

    }


    @FXML
    private void startGame() {
        scSwitcher.setScreen("game");
    }

    @FXML
    private void selectLevel() {
        boolean levelSelected = !levelsListView.getSelectionModel().isEmpty();
        startButton.setDisable(!levelSelected);
        if (levelSelected) {
            LevelsData.getInstance().selectForLoading(((Label) levelsListView.getSelectionModel().getSelectedItem().getChildren().get(0)).getText());
        }
    }


}
