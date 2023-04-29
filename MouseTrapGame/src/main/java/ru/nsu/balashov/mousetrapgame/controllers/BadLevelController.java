package ru.nsu.balashov.mousetrapgame.controllers;

import ru.nsu.balashov.mousetrapgame.ScreenSwitcher;
import ru.nsu.balashov.mousetrapgame.controllers.switching.SwitchingController;

public class BadLevelController implements SwitchingController {
    private ScreenSwitcher scSwitcher;

    @Override
    public void setScreenParent(ScreenSwitcher sc) {
        scSwitcher = sc;
    }

    @Override
    public void initController() {}


}
