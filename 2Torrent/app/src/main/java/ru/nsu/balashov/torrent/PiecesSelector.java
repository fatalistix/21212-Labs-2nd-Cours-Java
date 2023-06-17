package ru.nsu.balashov.torrent;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

class OneHashPiecesSelector {
//    private final HashSet<PieceWithKeys> piecesInUse     = new HashSet<>();
    private final HashSet<PieceWithKeys> availablePieces = new HashSet<>();
    private final HashMap<SelectionKey, PieceWithKeys> keyToPieceInUse = new HashMap<>();

    public OneHashPiecesSelector(byte[] bitfieldDownloaded, int numOfPieces) {
        for (int i = 0; i < bitfieldDownloaded.length; ++i) {
            if (bitfieldDownloaded[i] == -1) { //? MAX UNSIGNED BYTE VALUE
                continue;
            }
            for (int j = 0; j < Byte.SIZE; ++j) {
                if (i * Byte.SIZE + j < numOfPieces && ((bitfieldDownloaded[i] >> (7 - j)) & 1) == 0) {
                    availablePieces.add(new PieceWithKeys(i * Byte.SIZE + j));
                }
            }
        }
    }

    private boolean bitfieldIndexTrue(byte[] bitfield, int index) {
        return ((bitfield[index / Byte.SIZE] >> (7 - (index % 8))) & 1) == 1;
    }

    public void addAssociation(SelectionKey key, byte[] bitfield) {
        for (PieceWithKeys piece : availablePieces) {
            if (bitfieldIndexTrue(bitfield, piece.getIndex())) {
                piece.addAssociation(key);
            }
        }
        for (PieceWithKeys piece : keyToPieceInUse.values()) {
            if (bitfieldIndexTrue(bitfield, piece.getIndex())) {
                piece.addAssociation(key);
            }
        }
    }

    public int selectForKey(SelectionKey key) {
        Iterator<PieceWithKeys> iter = availablePieces.iterator();
        while (iter.hasNext()) {
            PieceWithKeys piece = iter.next();
            if (piece.haveAssociation(key)) {
                keyToPieceInUse.put(key, piece);
                iter.remove();
                return piece.getIndex();
            }
        }
        return -1;
    }

    public void deselect(SelectionKey key, boolean success) {
        if (keyToPieceInUse.containsKey(key)) {
            if (!success) {
                keyToPieceInUse.get(key).removeAssociation(key);
                availablePieces.add(keyToPieceInUse.get(key));
            }
            keyToPieceInUse.remove(key);
        }
    }

//    public void removeAssociation(SelectionKey key, int index) {
//        for (PieceWithKeys piece : availablePieces) {
//            if (piece.getIndex() == index && piece.haveAssociation(key)) {
//                piece.removeAssociation(key);
//                return;
//            }
//        }
//    }

    public void removeAllAssociations(SelectionKey key) {
        for (PieceWithKeys piece : availablePieces) {
            if (piece.haveAssociation(key)) {
                piece.removeAssociation(key);
            }
        }
        keyToPieceInUse.remove(key);
        for (PieceWithKeys piece : keyToPieceInUse.values()) {
            if (piece.haveAssociation(key)) {
                piece.removeAssociation(key);
            }
        }
    }

    public boolean havePieces() {
        return !availablePieces.isEmpty() || !keyToPieceInUse.isEmpty();
    }

//    public void removeAvailableIndex(int index) {
//        Iterator<PieceWithKeys> iter = availablePieces.iterator();
//        while (iter.hasNext()) {
//            PieceWithKeys piece = iter.next();
//            if (piece.getIndex() == index) {
//                iter.remove();
//                return;
//            }
//        }
//    }
}



public class PiecesSelector {
    private final ConcurrentHashMap<ByteBuffer, OneHashPiecesSelector> hashToSelectorHM = new ConcurrentHashMap<>();

    public PiecesSelector() {}

    public void register(ByteBuffer infoHash, byte[] downloadedBitfield, int numOfPieces) {
        hashToSelectorHM.put(infoHash, new OneHashPiecesSelector(downloadedBitfield, numOfPieces));
    }
    public void unregister(ByteBuffer infoHash) {
        hashToSelectorHM.remove(infoHash);
    }
    public int selectPiece(ByteBuffer infoHash, SelectionKey key) {
        if (hashToSelectorHM.containsKey(infoHash)) {
            return hashToSelectorHM.get(infoHash).selectForKey(key);
        }
        return -1;
    }
//    public void deselectPiece(ByteBuffer infoHash, int index) {
//        if (hashToSelectorHM.containsKey(infoHash)) {
//            hashToSelectorHM.get(infoHash).deselect(index);
//        }
//    }
    public boolean contains(ByteBuffer infoHash) {
        return hashToSelectorHM.contains(infoHash);
    }
    public void deselectPiece(ByteBuffer infoHash, SelectionKey key, boolean success) {
        if (hashToSelectorHM.containsKey(infoHash)) {
            hashToSelectorHM.get(infoHash).deselect(key, success);
        }
    }
    public void addAssociation(ByteBuffer infoHash, SelectionKey key, byte[] bitfield) {
        if (hashToSelectorHM.containsKey(infoHash)) {
            hashToSelectorHM.get(infoHash).addAssociation(key, bitfield);
        }
    }
//    public void removeAssociation(ByteBuffer infoHash, SelectionKey key, int index) {
//        if (hashToSelectorHM.containsKey(infoHash)) {
//            hashToSelectorHM.get(infoHash).removeAssociation(key, index);
//        }
//    }

    public void removeAllAssociations(ByteBuffer infoHash, SelectionKey key) {
        if (hashToSelectorHM.containsKey(infoHash)) {
            hashToSelectorHM.get(infoHash).removeAllAssociations(key);
        }
    }

    public boolean havePieces(ByteBuffer infoHash) {
        if (hashToSelectorHM.containsKey(infoHash)) {
            hashToSelectorHM.get(infoHash).havePieces();
        }
        return false;
    }

//    public void removeAvailableIndex(ByteBuffer infoHash, int index) {
//        if (hashToSelectorHM.containsKey(infoHash)) {
//            hashToSelectorHM.get(infoHash).removeAvailableIndex(index);
//        }
//    }
}
