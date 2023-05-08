package ru.nsu.balashov.mousetrapgame.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import ru.nsu.balashov.mousetrapgame.HighScoresData;
import ru.nsu.balashov.mousetrapgame.HighScoresData.HighScoresFactory.ScoreData;
import ru.nsu.balashov.mousetrapgame.LevelsData;
import ru.nsu.balashov.mousetrapgame.ScreenSwitcher;
import ru.nsu.balashov.mousetrapgame.controllers.switching.SwitchingController;
import ru.nsu.balashov.mousetrapgame.settings.GUIProperties;
import ru.nsu.balashov.mousetrapgame.settings.Settings;

import java.util.ArrayList;
import java.util.Objects;

public class ScoresController implements SwitchingController {
    private ScreenSwitcher scSwitcher;
    @Override
    public void setScreenParent(ScreenSwitcher sc) {
        this.scSwitcher = sc;
    }

    @FXML
    private ListView<GridPane> scoresList;
    @FXML
    private Button backButton;

    private final ObservableList<GridPane> scoreListItems = FXCollections.observableArrayList();
    private final Font viewElementFont = new Font("SF Pro Display", Settings.ScoreScreenProperties.VIEW_ELEMENT_FONT_SIZE);


    public void initialize() {
        backButton.setFont(viewElementFont);
    }


    @Override
    public void initController() {
        ArrayList<String> levelsNames = LevelsData.getInstance().getLevelsNames();
        for (String levelName : levelsNames) {
            ArrayList<ScoreData> levelScores = Objects.requireNonNull(HighScoresData.getInstance()).getAllLevelScores(levelName);
            if (levelScores != null) {
                for (ScoreData sc : levelScores) {
                    Label levelNameLabel = new Label(levelName);
                    Label authorLabel = new Label(sc.author());
                    Label timeLabel   = new Label(String.format("%02d:%02d:%02d", sc.seconds() / 3600,
                            (sc.seconds() / 60) % 60, sc.seconds() % 60));
                    Label stepsLabel  = new Label(String.valueOf(sc.steps()));


                    levelNameLabel.setFont(viewElementFont);
                    authorLabel.setFont(viewElementFont);
                    timeLabel.setFont(viewElementFont);
                    stepsLabel.setFont(viewElementFont);

                    GridPane item = new GridPane();

                    ColumnConstraints columnLevelName   = new ColumnConstraints();
                    ColumnConstraints columnAuthor = new ColumnConstraints();
                    ColumnConstraints columnTime   = new ColumnConstraints();
                    ColumnConstraints columnSteps  = new ColumnConstraints();

                    columnLevelName.setPercentWidth(Settings.ScoreScreenProperties.LEVEL_NAME_LABEL_RATIO_PERCENTS);
                    columnAuthor.setPercentWidth(Settings.ScoreScreenProperties.AUTHOR_LABEL_WIDTH_RATIO_PERCENTS);
                    columnTime.setPercentWidth(Settings.ScoreScreenProperties.TIME_LABEL_WIDTH_RATIO_PERCENTS);
                    columnSteps.setPercentWidth(Settings.ScoreScreenProperties.STEPS_LABEL_WIDTH_RATIO_PERCENTS);

                    item.getColumnConstraints().addAll(columnLevelName, columnAuthor, columnTime, columnSteps);
                    item.getChildren().addAll(levelNameLabel, authorLabel, timeLabel, stepsLabel);

                    GridPane.setConstraints(levelNameLabel, 0, 0);
                    GridPane.setConstraints(authorLabel, 1, 0);
                    GridPane.setConstraints(timeLabel, 2, 0);
                    GridPane.setConstraints(stepsLabel, 3, 0);

                    scoreListItems.add(item);
                }
            }
        }
        scoresList.setItems(scoreListItems);
    }

    @FXML
    private void backButtonPressed() {
        scSwitcher.setScreen(GUIProperties.mainID);
    }
}
