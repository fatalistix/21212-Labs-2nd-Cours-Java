package ru.nsu.balashov.torrent;

import java.nio.channels.SelectionKey;
import java.util.HashSet;


/**
 * This class is responsible for associating piece index (<code>int</code>) and selector's keys (<code>SelectionKey</code>).<br/>
 * Selection keys are stored in <code>HashSet</code> and can be added or removed via methods.
 */
public class PieceWithKeys {
    private final int index;
    private final HashSet<SelectionKey> associatedKeys = new HashSet<>();
    public PieceWithKeys(int index) {
        this.index = index;
    }

    /**
     * Get index of that piece.
     * @return index with which keys are associated
     */
    public int getIndex() {
        return index;
    }

    /**
     * Adds this key to set of associated keys.
     * @param key that will be associated with that piece
     */
    public void addAssociation(SelectionKey key) {
        associatedKeys.add(key);
    }

    /**
     * Removes key from set of associated keys.
     * @param key that will be removed (if contains)
     */
    public void removeAssociation(SelectionKey key) {
        associatedKeys.remove(key);
    }

    /**
     * @param key to check for containing in set
     * @return is this key in set
     */
    public boolean haveAssociation(SelectionKey key) {
        return associatedKeys.contains(key);
    }
}
