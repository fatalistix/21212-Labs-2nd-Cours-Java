package ru.nsu.balashov.mousetrapgame.settings;


import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class Settings {
    public static class DeviceScreenProperties {
        private final static Rectangle2D SCREEN_RESOLUTION = Screen.getPrimary().getBounds();
        public final static double SCREEN_HEIGHT = SCREEN_RESOLUTION.getHeight();
        public final static double SCREEN_WIDTH  = SCREEN_RESOLUTION.getWidth();
        private DeviceScreenProperties() {}
    }

    //!!!!!!!!!!!!!!! for 16:9
    public static class WindowProperties {
        private final static double SCREEN_WIDTH_TO_GAME_WIDTH_RATIO = 900. / 1920.;
        private final static double GAME_WIDTH_TO_GAME_HEIGHT_RATIO = 1.;
        public final static double GAME_WIDTH = DeviceScreenProperties.SCREEN_WIDTH * SCREEN_WIDTH_TO_GAME_WIDTH_RATIO;
        public final static double GAME_HEIGHT = GAME_WIDTH * GAME_WIDTH_TO_GAME_HEIGHT_RATIO;
        private WindowProperties() {}
    }

    public static class MainMenuScreenProperties {
        private final static double MOUSE_IMAGE_SCALE = 2. / 1080. * DeviceScreenProperties.SCREEN_HEIGHT;
        private final static double TITLE_FONT_SCALE = 48. / 1080.;
        private final static double DESCRIPTION_FONT_SCALE = 18. / 1080.;
        private final static double START_GAME_BUTTON_FONT_SCALE = 24. / 1080.;
        private final static double SELECT_LEVEL_FONT_SCALE = 15. / 1080.;

        public final static double MOUSE_IMAGE_WIDTH = 181 * MOUSE_IMAGE_SCALE;
        public final static double MOUSE_IMAGE_HEIGHT = 90 * MOUSE_IMAGE_SCALE;
        public final static double TITLE_FONT_SIZE = DeviceScreenProperties.SCREEN_HEIGHT * TITLE_FONT_SCALE;
        public final static double DESCRIPTION_FONT_SIZE = DeviceScreenProperties.SCREEN_HEIGHT * DESCRIPTION_FONT_SCALE;
        public final static double START_GAME_BUTTON_FONT_SIZE = DeviceScreenProperties.SCREEN_HEIGHT * START_GAME_BUTTON_FONT_SCALE;
        public final static double SELECT_LEVEL_FONT_SIZE = DeviceScreenProperties.SCREEN_HEIGHT * SELECT_LEVEL_FONT_SCALE;
        public final static double SELECT_LEVEL_LIST_VIEW_PREF_WIDTH = WindowProperties.GAME_WIDTH;
        public final static double START_GAME_PANE_BUTTON_PREF_WIDTH = WindowProperties.GAME_WIDTH;
        public final static double LEVEL_NAME_LABEL_WIDTH_RATIO_PERCENTS = 40;
        public final static double BEST_TIME_LABEL_WIDTH_RATIO_PERCENTS = 20;
        public final static double BEST_PLAYER_LABEL_WIDTH_RATIO_PERCENTS = 40;
        private MainMenuScreenProperties() {}
    }

    public static class GameScreenProperties {
        private final static double IMAGES_SCALE = 1. / 1080. * DeviceScreenProperties.SCREEN_HEIGHT;
        private final static double GAME_FIELD_WIDTH_SCALE = 545. / 1920.;
        private final static double PIXEL_FOR_SHIFT_SCALE = 1. / 3.;

        public final static double PIXELS_PER_BLOCK = 90 * IMAGES_SCALE;
        public final static double PIXELS_FOR_SHIFT = PIXEL_FOR_SHIFT_SCALE * PIXELS_PER_BLOCK;
        public final static double LEVEL_NAME_FONT_SIZE = MainMenuScreenProperties.TITLE_FONT_SIZE;
        public final static double CURRENT_TIME_FONT_SIZE = MainMenuScreenProperties.DESCRIPTION_FONT_SIZE;
        public final static double STEPS_MADE_FONT_SIZE = MainMenuScreenProperties.DESCRIPTION_FONT_SIZE;
        public final static double BEST_SCORE_FONT_SIZE = MainMenuScreenProperties.DESCRIPTION_FONT_SIZE;
        public final static double GAME_FIELD_SIZE = GAME_FIELD_WIDTH_SCALE * DeviceScreenProperties.SCREEN_WIDTH;
        public final static double LINE_STROKE_WIDTH = 1. / 1920. * DeviceScreenProperties.SCREEN_WIDTH;

        private GameScreenProperties() {}
    }





    private Settings() {}
}
