package ru.nsu.balashov.torrent.utils;

public class TorrentConnectionByteBufferUtils {
    public enum MessageType {
        REQUEST_AVAILABLE(0),
        REQUEST_PIECE(1),
        REQUEST_CANCEL(2);
//        RESPONSE_AVAILABLE



        private final byte messageCode;
        MessageType(int messageCode) {
            this.messageCode = (byte) messageCode;
        }
        private byte getMessageCode() {
            return messageCode;
        }
    }
}
