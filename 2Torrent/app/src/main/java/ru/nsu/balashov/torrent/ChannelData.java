package ru.nsu.balashov.torrent;

public class ChannelData {
    private final byte[] infoHash;
    private boolean dataActual = false;

    public ChannelData(byte[] infoHash, boolean actual) {
        this.infoHash = infoHash;
        this.dataActual = actual;
    }
    public boolean isDataActual() {
        return dataActual;
    }
    public byte[] getInfoHash() {
        return infoHash;
    }
    public void setDataActual(boolean actual) {
        this.dataActual = actual;
    }
}