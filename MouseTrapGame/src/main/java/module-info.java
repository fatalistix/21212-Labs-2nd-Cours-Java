module ru.nsu.balashov.mousetrapgame {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    requires com.google.gson;
    requires org.apache.commons.collections4;

    opens ru.nsu.balashov.mousetrapgame to javafx.fxml;
    exports ru.nsu.balashov.mousetrapgame;
    exports ru.nsu.balashov.mousetrapgame.controllers;
    opens ru.nsu.balashov.mousetrapgame.controllers to javafx.fxml;
}