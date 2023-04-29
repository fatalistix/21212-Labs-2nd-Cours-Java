package ru.nsu.balashov.mousetrapgame.controllers.switching;

import ru.nsu.balashov.mousetrapgame.ScreenSwitcher;

public interface SwitchingController {
    public void setScreenParent(ScreenSwitcher sc);
    public void initController();
}
