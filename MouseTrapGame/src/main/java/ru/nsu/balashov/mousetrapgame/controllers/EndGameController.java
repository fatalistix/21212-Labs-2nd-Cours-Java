package ru.nsu.balashov.mousetrapgame.controllers;

import ru.nsu.balashov.mousetrapgame.ScreenSwitcher;
import ru.nsu.balashov.mousetrapgame.controllers.switching.SwitchingController;

public class EndGameController implements SwitchingController {
    private ScreenSwitcher scSwitcher;
    @Override
    public void setScreenParent(ScreenSwitcher sc) {
        this.scSwitcher = sc;
    }
    @Override
    public void initController() {}
}
