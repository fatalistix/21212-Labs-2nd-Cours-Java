package ru.nsu.balashov.torrent;

import com.google.common.primitives.Ints;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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

    public static int readAllMessageBytes(ByteBuffer byteBuffer, SocketChannel socketChannel) throws IOException {
        byteBuffer.clear();
        int readBytes = socketChannel.read(byteBuffer);
        if (readBytes == -1) {
            return -1;
        }
        ByteBuffer readCopyBuffer = byteBuffer.asReadOnlyBuffer();
        readCopyBuffer.clear();
        int alreadyReadBytes = readBytes;
        int targetReadBytes = readCopyBuffer.getInt();
        while (alreadyReadBytes < targetReadBytes) {
            readBytes = socketChannel.read(byteBuffer);
            if (readBytes == -1) {
                return -1;
            }
            alreadyReadBytes += readBytes;
        }
        byteBuffer.flip();
        byteBuffer.getInt();
        return alreadyReadBytes;
    }

    public static MessageType readMessageType(ByteBuffer byteBuffer) {
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
        byteBuffer.putInt(Integer.BYTES + Byte.BYTES);
        byteBuffer.put(MessageType.REQUEST_CANCEL.getMessageCode());
        byteBuffer.flip();
    }
    public static void writeUnknown(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        byteBuffer.putInt(Integer.BYTES + Byte.BYTES);
        byteBuffer.put(MessageType.UNKNOWN.getMessageCode());
        byteBuffer.flip();
    }
    public static void writeHandshake(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        byteBuffer.putInt(Integer.BYTES + Byte.BYTES);
        byteBuffer.put(MessageType.HANDSHAKE.getMessageCode());
        byteBuffer.flip();
    }

    public static class Client {
        public record AvailableData(ByteBuffer infoHash, byte[] availableBitfield) {}
        public record PieceData(ByteBuffer infoHash, byte[] piece, int index) {}
        private Client() {}
        public static void writeRequestAvailable(ByteBuffer byteBuffer, ByteBuffer infoHash) {
            byteBuffer.clear();
            infoHash.clear();
            byteBuffer.putInt(Integer.BYTES + Byte.BYTES + infoHash.limit());
            byteBuffer.put(MessageType.REQUEST_AVAILABLE.getMessageCode());
            byteBuffer.put(infoHash);
            byteBuffer.flip();
            infoHash.flip();
        }
        public static AvailableData readResponseAvailable(ByteBuffer byteBuffer) {
            byte[] infoHash = new byte[20];
            byte[] bitfieldLengthBuffer = new byte[Ints.BYTES];
            byteBuffer.get(infoHash).get(bitfieldLengthBuffer);
            byte[] bitfield = new byte[Ints.fromByteArray(bitfieldLengthBuffer)];
            byteBuffer.get(bitfield);
            return new AvailableData(ByteBuffer.wrap(infoHash), bitfield);
        }
        public static void writeRequestPiece(ByteBuffer byteBuffer, ByteBuffer infoHash, int index) {
            byteBuffer.clear();
            infoHash.clear();
            byteBuffer.putInt(Integer.BYTES + Byte.BYTES + infoHash.limit() + Integer.BYTES);
            byteBuffer.put(MessageType.REQUEST_PIECE.getMessageCode());
            byteBuffer.put(infoHash);
            byteBuffer.put(Ints.toByteArray(index));
            byteBuffer.flip();
            infoHash.flip();
        }
        public static PieceData readResponsePiece(ByteBuffer byteBuffer) {
            byte[] infoHash = new byte[20];
            byte[] intBuf = new byte[Ints.BYTES];
            byteBuffer.get(infoHash).get(intBuf);
            int pieceLength = Ints.fromByteArray(intBuf);
            byte[] piece = new byte[pieceLength];
            byteBuffer.get(piece).get(intBuf);
            return new PieceData(ByteBuffer.wrap(infoHash), piece, Ints.fromByteArray(intBuf));
        }
    }

    public static class Server {
        public record PieceData(ByteBuffer infoHash, int index) {}
        private Server() {}
        public static ByteBuffer readRequestAvailable(ByteBuffer byteBuffer) {
            byte[] infoHash = new byte[20];
            byteBuffer.get(infoHash);
            return ByteBuffer.wrap(infoHash);
        }
        public static void writeResponseAvailable(ByteBuffer byteBuffer, ByteBuffer infoHash, byte[] availableBitfield) {
            byteBuffer.clear();
            infoHash.clear();
            byteBuffer.putInt(Integer.BYTES + Byte.BYTES + infoHash.limit() + Integer.BYTES + availableBitfield.length);
            byteBuffer.put(MessageType.RESPONSE_AVAILABLE.getMessageCode());
            byteBuffer.put(infoHash);
            byteBuffer.put(Ints.toByteArray(availableBitfield.length));
            byteBuffer.put(availableBitfield);
            byteBuffer.flip();
            infoHash.flip();
        }
        public static PieceData readRequestPiece(ByteBuffer byteBuffer) {
            byte[] infoHash = new byte[20];
            byte[] indexBuf = new byte[Ints.BYTES];
            byteBuffer.get(infoHash).get(indexBuf);
            return new PieceData(ByteBuffer.wrap(infoHash), Ints.fromByteArray(indexBuf));
        }
        public static void writeResponsePiece(ByteBuffer byteBuffer, ByteBuffer infoHash, byte[] piece, int index) {
            byteBuffer.clear();
            infoHash.clear();
            byteBuffer.putInt(Integer.BYTES + Byte.BYTES + infoHash.limit() + Integer.BYTES + piece.length + Integer.BYTES);
            byteBuffer.put(MessageType.RESPONSE_PIECE.getMessageCode());
            byteBuffer.put(infoHash);
            byteBuffer.put(Ints.toByteArray(piece.length));
            byteBuffer.put(piece);
            byteBuffer.put(Ints.toByteArray(index));
            byteBuffer.flip();
            infoHash.flip();
        }
    }
}
