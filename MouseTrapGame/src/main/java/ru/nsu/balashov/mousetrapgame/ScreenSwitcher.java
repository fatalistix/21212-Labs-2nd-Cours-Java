package ru.nsu.balashov.mousetrapgame;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ru.nsu.balashov.mousetrapgame.controllers.switching.SwitchingController;

import java.util.HashMap;

public class ScreenSwitcher extends StackPane {
    private final HashMap<String, Node> screens = new HashMap<>();
    private final HashMap<String, SwitchingController> controllers = new HashMap<>();

    public ScreenSwitcher() {
        super();
    }



    public void loadScreen(String name, FXMLLoader loader) throws LoadScreensException {
        try {
            Parent loadedScreen = loader.load();
            SwitchingController controller = loader.getController();
            controller.setScreenParent(this);
            controllers.put(name, controller);
            screens.put(name, loadedScreen);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new LoadScreensException("Cannot load screen with name: " + name, e);
        }
    }

    private void playAnimation(String name) {
        DoubleProperty opacity = this.opacityProperty();

        if (!this.getChildren().isEmpty()) {
            Timeline fade = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(opacity, 1.0)),
                    new KeyFrame(new Duration(1000), ae -> {
                        getChildren().remove(0);
                        getChildren().add(0, screens.get(name));
                        Timeline fadein = new Timeline(
                                new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
                                new KeyFrame(new Duration(800), new KeyValue(opacity, 1.0))
                        );
                        fadein.play();
                    }, new KeyValue(opacity, 0.))
            );
            fade.play();
        } else {
            this.setOpacity(0.);
            this.getChildren().add(screens.get(name));
            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.)),
                    new KeyFrame(new Duration(2500), new KeyValue(opacity, 1.))
            );
            fadeIn.play();
        }
    }

    public void setScreen(final String name) {
        if (screens.get(name) != null) {
            this.playAnimation(name);
            controllers.get(name).initController();
        }
    }

    public static class LoadScreensException extends Exception {
        LoadScreensException(String s, Throwable e) {
            super(s, e);
        }
    }
}
