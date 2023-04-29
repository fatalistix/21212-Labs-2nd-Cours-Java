package ru.nsu.balashov.mousetrapgame.settings;


import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class Settings {
    public static class ScreenProperties {
        private final static Rectangle2D SCREEN_RESOLUTION = Screen.getPrimary().getBounds();
        public final static double SCREEN_HEIGHT = SCREEN_RESOLUTION.getHeight();
        public final static double SCREEN_WIDTH  = SCREEN_RESOLUTION.getWidth();
        private ScreenProperties() {}
    }

    //!!!!!!!!!!!!!!! for 16:9
    public static class GameWindowProperties {
        private final static double SCREEN_WIDTH_TO_GAME_WIDTH_RATIO = 900. / 1920.;
        private final static double GAME_WIDTH_TO_GAME_HEIGHT_RATIO = 1.;
        public final static double GAME_WIDTH = ScreenProperties.SCREEN_WIDTH * SCREEN_WIDTH_TO_GAME_WIDTH_RATIO;
        public final static double GAME_HEIGHT = GAME_WIDTH * GAME_WIDTH_TO_GAME_HEIGHT_RATIO;
        private GameWindowProperties() {}
    }

    public static class MainMenuProperties {
        private final static double MOUSE_IMAGE_SCALE = 2. / 1080. * ScreenProperties.SCREEN_HEIGHT;
        private final static double MOUSE_TRAP_TITLE_FONT_SCALE = 48. / 1080.;
        private final static double DESCRIPTION_FONT_SCALE = 18. / 1080.;

        private MainMenuProperties() {}
    }



    public final static double fieldCeilScale = 90;



//    public final static double MAIN_MOUSE_IMAGE_WIDTH = 181 * MOUSE_IMAGE_SCALE;
//    public final static double MAIN_MOUSE_IMAGE_HEIGHT = 90 * MOUSE_IMAGE_SCALE;


    private Settings() {}
}
