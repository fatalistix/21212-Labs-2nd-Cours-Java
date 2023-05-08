package ru.nsu.balashov.mousetrapgame;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ru.nsu.balashov.mousetrapgame.settings.GUIProperties;
import ru.nsu.balashov.mousetrapgame.settings.Settings;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) {

        ScreenSwitcher switcher = new ScreenSwitcher();
        try {
            switcher.loadScreen(GUIProperties.gameID, GUIProperties.gameFXML);
            switcher.loadScreen(GUIProperties.mainID, GUIProperties.mainFXML);
            switcher.loadScreen(GUIProperties.endgameID, GUIProperties.endgameFXML);
            switcher.loadScreen(GUIProperties.badLevelID, GUIProperties.badLevelFXML);
            switcher.loadScreen(GUIProperties.scoresID, GUIProperties.scoresFXML);
        } catch (ScreenSwitcher.LoadScreensException e) {
            System.out.println(e.getMessage());
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Application files damaged");
            errorAlert.setContentText("Cannot initialize applications screens, fatal error");
            errorAlert.showAndWait();
            stage.close();
            return;
        }


        switcher.setScreen(GUIProperties.mainID);
        Scene scene = new Scene(switcher);

        stage.setScene(scene);
        stage.setTitle("Mouse Trap");
        stage.setMinWidth(Settings.WindowProperties.GAME_WIDTH);
        stage.setMinHeight(Settings.WindowProperties.GAME_HEIGHT);
        stage.setHeight(Settings.WindowProperties.GAME_HEIGHT);
        stage.setWidth(Settings.WindowProperties.GAME_WIDTH);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}