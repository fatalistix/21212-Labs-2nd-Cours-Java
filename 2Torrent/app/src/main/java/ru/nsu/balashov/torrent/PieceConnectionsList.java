package ru.nsu.balashov.torrent;

import java.nio.channels.SelectionKey;
import java.util.*;

public class PieceConnectionsList /*implements Iterable<PieceConnectionsList.PiecesChannels>*/ {
    //    public static record PiecesChannels(int pieceIndex, Set<SelectionKey> keys) {}
//    private final LinkedList<PiecesChannels> list = new LinkedList<>();

    private final HashMap<Integer, KeySetWrapper> hashMap = new HashMap<>();

    private final int bitfieldLength;
    public PieceConnectionsList(byte[] bitfieldDownloaded) {
        bitfieldLength = bitfieldDownloaded.length;
        for (int i = 0; i < bitfieldDownloaded.length; ++i) {
            if (bitfieldDownloaded[i] == -1) { //? MAX UNSIGNED VALUE
                continue;
            }
            for (int j = 0; j < Byte.SIZE; ++j) {
                if (((bitfieldDownloaded[i] >> (7 - j)) & 1) == 0) {
//                    list.add(new PiecesChannels(i * Byte.SIZE + j, new HashSet<>()));
                    hashMap.put(i * Byte.SIZE + j, new KeySetWrapper());
                }
            }
        }
    }

    private boolean bitfieldIndexTrue(byte[] bitfield, int index) {
        return ((bitfield[index / Byte.SIZE] >> (7 - (index % 8))) & 1) == 1;
    }

    public boolean addAssociation(SelectionKey key, byte[] bitfield) {
        if (bitfield.length != this.bitfieldLength) {
            System.out.println(bitfield.length + " " + this.bitfieldLength);
            System.out.println("SHOULDN'T BE");
            return false;
        }
//        for (PiecesChannels piecesChannels : list) {
//            int index = piecesChannels.pieceIndex();
//            if (bitfieldIndexTrue(bitfield, index)) {
//                piecesChannels.keys().add(key);
//            }
//        }
        for (Integer pieceIndex : hashMap.keySet()) {
            if (bitfieldIndexTrue(bitfield, pieceIndex)) {
                hashMap.get(pieceIndex).getKeySet().add(key);
            }
        }
        return true;
    }

    public void removeAssociation(int index, SelectionKey key) {
        if (hashMap.containsKey(index)) {
            hashMap.get(index).getKeySet().remove(key);
            if (hashMap.get(index).getKeySet().isEmpty()) {
                hashMap.remove(index);
            }
        }
    }

    public void removeAssociation(SelectionKey key) {
        for (KeySetWrapper wrapper : hashMap.values()) {
            wrapper.getKeySet().remove(key);
        }
    }

    public void remove(int index) {
        hashMap.remove(index);
    }

    public boolean contains(int index, SelectionKey key) {
        return hashMap.containsKey(index) && hashMap.get(index).getKeySet().contains(key);
    }

    public Set<Integer> getIndexesAsSet() {
        return hashMap.keySet();
    }
    public boolean isInUse(int index) {
        return hashMap.get(index).isInUse();
    }

    public boolean usePiece(int index) {
        if (hashMap.get(index).isInUse()) {
            return false;
        }
        hashMap.get(index).setInUse(true);
        return true;
    }

    public boolean releasePiece(int index) {
        if (!hashMap.get(index).isInUse()) {
            return false;
        }
        hashMap.get(index).setInUse(false);
        return true;
    }

//    @Override
//    public Iterator<PiecesChannels> iterator() {
//        return list.iterator();
//    }
}
