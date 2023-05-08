package ru.nsu.balashov.mousetrapgame.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import ru.nsu.balashov.mousetrapgame.ScreenSwitcher;
import ru.nsu.balashov.mousetrapgame.controllers.switching.SwitchingController;
import ru.nsu.balashov.mousetrapgame.settings.GUIProperties;
import ru.nsu.balashov.mousetrapgame.settings.Settings;

public class BadLevelController implements SwitchingController {
    private ScreenSwitcher scSwitcher;

    @Override
    public void setScreenParent(ScreenSwitcher sc) {
        scSwitcher = sc;
    }

    @Override
    public void initController() {}



    @FXML
    private Label badLevelLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Button mainMenuButton;


    private final Font badLevelFont = new Font("SF Pro Display Bold", Settings.BadLevelScreenProperties.BAD_LEVEL_FONT_SIZE);
    private final Font descriptionFont = new Font("SF Pro Display Italic", Settings.BadLevelScreenProperties.DESCRIPTION_FONT_SIZE);
    private final Font buttonFont = new Font("SF Pro Display", Settings.BadLevelScreenProperties.BUTTONS_FONT_SIZE);

    public void initialize() {
        badLevelLabel.setFont(badLevelFont);
        descriptionLabel.setFont(descriptionFont);
        mainMenuButton.setFont(buttonFont);
    }

    @FXML
    private void toMainMenu(ActionEvent actionEvent) {
        scSwitcher.setScreen(GUIProperties.mainID);
    }
}
