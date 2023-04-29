package ru.nsu.balashov.mousetrapgame.settings;

import javafx.fxml.FXMLLoader;

import java.util.Objects;

public class GUIProperties {
    private static final String pathToFXMLDir = "ru/nsu/balashov/mousetrapgame/FXML";
    public static final String mainID = "main";
    public static final String gameID = "game";
    public static final String endgameID = "endgame";
    public static final String badLevelID = "badLevel";

    public static final FXMLLoader mainFXML = new FXMLLoader(Objects.requireNonNull(ClassLoader.getSystemResource(pathToFXMLDir + "/MainScreen.fxml")));
    public static final FXMLLoader gameFXML = new FXMLLoader(Objects.requireNonNull(ClassLoader.getSystemResource(pathToFXMLDir + "/GameScreen.fxml")));
    public static final FXMLLoader endgameFXML = new FXMLLoader(Objects.requireNonNull(ClassLoader.getSystemResource(pathToFXMLDir + "/EndGameScreen.fxml")));
    public static final FXMLLoader badLevelFXML = new FXMLLoader(Objects.requireNonNull(ClassLoader.getSystemResource(pathToFXMLDir + "/EndGameScreen.fxml")));

    private GUIProperties() {}
}
