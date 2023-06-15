package ru.nsu.balashov.torrent;

import java.nio.channels.SelectionKey;

public class KeyReferencesCounter {
    private final SelectionKey key;
    private int counter;
    public KeyReferencesCounter(SelectionKey key) {
        this.key = key;
        counter = 1;
    }
    public SelectionKey use() {
        ++counter;
        return key;
    }
    public void release() {
        --counter;
        if (counter == 0) {
            key.cancel();
        }
    }
    public int getCounter() {
        return counter;
    }
}
