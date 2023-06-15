package ru.nsu.balashov.torrent;

import java.nio.channels.SelectionKey;
import java.util.HashSet;

public class PieceWithKeys {
    private final int index;
    private final HashSet<SelectionKey> associatedKeys = new HashSet<>();
    public PieceWithKeys(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
    public void addAssociation(SelectionKey key) {
        associatedKeys.add(key);
    }
    public void removeAssociation(SelectionKey key) {
        associatedKeys.remove(key);
    }
    public boolean haveAssociation(SelectionKey key) {
        return associatedKeys.contains(key);
    }
}
