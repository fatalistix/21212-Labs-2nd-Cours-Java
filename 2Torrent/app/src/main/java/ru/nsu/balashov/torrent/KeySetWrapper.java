package ru.nsu.balashov.torrent;

import java.nio.channels.SelectionKey;
import java.util.HashSet;
import java.util.Set;

public class KeySetWrapper {
    private final Set<SelectionKey> keySet = new HashSet<>();
    private boolean inUse = false;
    public KeySetWrapper() {}
    public Set<SelectionKey> getKeySet() {
        return keySet;
    }
    public boolean isInUse() {
        return inUse;
    }
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }
}
