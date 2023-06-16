package ru.nsu.balashov.torrent;

import java.nio.ByteBuffer;

public class ChannelData {
    private final ByteBuffer infoHash;
    private final ByteBuffer byteBuffer;

    public ChannelData(ByteBuffer infoHash, int capacity) {
        this.infoHash = infoHash;
        this.byteBuffer = ByteBuffer.allocate(capacity);
    }
    public ByteBuffer getInfoHash() {
        return infoHash;
    }
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }
}