package ru.nsu.balashov.mousetrapgame.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import ru.nsu.balashov.mousetrapgame.ScreenSwitcher;
import ru.nsu.balashov.mousetrapgame.controllers.switching.SwitchingController;
import ru.nsu.balashov.mousetrapgame.settings.GUIProperties;
import ru.nsu.balashov.mousetrapgame.settings.Settings;

public class EndGameController implements SwitchingController {
    private ScreenSwitcher scSwitcher;
    @Override
    public void setScreenParent(ScreenSwitcher sc) {
        this.scSwitcher = sc;
    }
    @Override
    public void initController() {}


    @FXML
    private GridPane baseGrid;
    @FXML
    private Label levelCompleteLabel;
    @FXML
    private Button restartThisLevelButton;
    @FXML
    private Button mainMenuButton;

    private final Font levelCompleteFont = new Font("SF Pro Display Bold", Settings.EndGameScreenProperties.LEVEL_COMPLETE_FONT_SIZE);
    private final Font buttonsFont = new Font("SF Pro Display", Settings.EndGameScreenProperties.BUTTONS_FONT_SIZE);

    public void initialize() {
        levelCompleteLabel.setFont(levelCompleteFont);
        restartThisLevelButton.setFont(buttonsFont);
        mainMenuButton.setFont(buttonsFont);
    }



    @FXML
    private void restartLevel(ActionEvent actionEvent) {
        scSwitcher.setScreen(GUIProperties.gameID);
    }

    @FXML
    private void toMainMenu(ActionEvent actionEvent) {
        scSwitcher.setScreen(GUIProperties.mainID);
    }
}
