package ru.nsu.balashov.torrent;

import com.google.common.primitives.Ints;

import java.nio.ByteBuffer;

public class TorrentBBMessagesParser {
    public static enum MessageType {
        REQUEST_AVAILABLE(0),
        REQUEST_PIECE(1),
        REQUEST_CANCEL(2),
        RESPONSE_AVAILABLE(3),
        RESPONSE_PIECE(4),
        HANDSHAKE(5),
        UNKNOWN(100);


        private final byte messageCode;
        MessageType(int messageCode) {
            this.messageCode = (byte) messageCode;
        }
        private byte getMessageCode() {
            return messageCode;
        }
    }

    public static MessageType readMessageType(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        byte messageCode = byteBuffer.get();
        for (MessageType type : MessageType.values()) {
            if (type.getMessageCode() == messageCode) {
                return type;
            }
        }
        return MessageType.UNKNOWN;
    }
    public static void writeRequestCancel(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        byteBuffer.put(MessageType.REQUEST_CANCEL.getMessageCode());
        byteBuffer.flip();
    }
    public static void writeUnknown(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        byteBuffer.put(MessageType.UNKNOWN.getMessageCode());
        byteBuffer.flip();
    }
    public static void writeHandshake(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        byteBuffer.put(MessageType.HANDSHAKE.getMessageCode());
        byteBuffer.flip();
    }

    public static class Client {
        public static record AvailableData(byte[] infoHash, byte[] availableBitfield) {}
        public static record PieceData(byte[] infoHash, byte[] piece, int index) {}
        private Client() {}
        public static void writeRequestAvailable(ByteBuffer byteBuffer, byte[] infoHash) {
            byteBuffer.clear();
            byteBuffer.put(MessageType.REQUEST_AVAILABLE.getMessageCode());
            byteBuffer.put(infoHash);
            byteBuffer.flip();
        }
        public static AvailableData readResponseAvailable(ByteBuffer byteBuffer) {
            byte[] infoHash = new byte[20];
            byte[] bitfieldLengthBuffer = new byte[Ints.BYTES];
            byteBuffer.get(infoHash).get(bitfieldLengthBuffer);
            byte[] bitfield = new byte[Ints.fromByteArray(bitfieldLengthBuffer)];
            byteBuffer.get(bitfield);
            return new AvailableData(infoHash, bitfield);
        }
        public static void writeRequestPiece(ByteBuffer byteBuffer, byte[] infoHash, int index) {
            byteBuffer.clear();
            byteBuffer.put(MessageType.REQUEST_PIECE.getMessageCode());
            byteBuffer.put(infoHash);
            byteBuffer.put(Ints.toByteArray(index));
            byteBuffer.flip();
        }
        public static PieceData readResponsePiece(ByteBuffer byteBuffer) {
            byte[] infoHash = new byte[20];
            byte[] intBuf = new byte[Ints.BYTES];
            byteBuffer.get(infoHash).get(intBuf);
            int pieceLength = Ints.fromByteArray(intBuf);
            byte[] piece = new byte[pieceLength];
            byteBuffer.get(piece).get(intBuf);
            return new PieceData(infoHash, piece, Ints.fromByteArray(intBuf));
        }
    }

    public static class Server {
        public static record PieceData(byte[] infoHash, int index) {}
        private Server() {}
        public static byte[] readRequestAvailable(ByteBuffer byteBuffer) {
            byte[] infoHash = new byte[20];
            byteBuffer.get(infoHash);
            return infoHash;
        }
        public static void writeResponseAvailable(ByteBuffer byteBuffer, byte[] infoHash, byte[] availableBitfield) {
            byteBuffer.clear();
            byteBuffer.put(MessageType.RESPONSE_AVAILABLE.getMessageCode());
            byteBuffer.put(infoHash);
            byteBuffer.put(Ints.toByteArray(availableBitfield.length));
            byteBuffer.put(availableBitfield);
            byteBuffer.flip();
        }
        public static PieceData readRequestPiece(ByteBuffer byteBuffer) {
            byte[] infoHash = new byte[20];
            byte[] indexBuf = new byte[Ints.BYTES];
            byteBuffer.get(infoHash).get(indexBuf);
            return new PieceData(infoHash, Ints.fromByteArray(indexBuf));
        }
        public static void writeResponsePiece(ByteBuffer byteBuffer, byte[] infoHash, byte[] piece, int index) {
            byteBuffer.clear();
            byteBuffer.put(MessageType.RESPONSE_PIECE.getMessageCode());
            byteBuffer.put(infoHash);
            byteBuffer.put(Ints.toByteArray(piece.length));
            byteBuffer.put(piece);
            byteBuffer.put(Ints.toByteArray(index));
            byteBuffer.flip();
        }
    }
}
