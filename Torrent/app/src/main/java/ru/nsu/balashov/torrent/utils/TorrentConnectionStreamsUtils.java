package ru.nsu.balashov.torrent.utils;

import com.google.common.primitives.Ints;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class TorrentConnectionStreamsUtils {
    public static enum MessageType {
        AVAILABLE_REQUEST(0),
        PIECE_REQUEST(1),
        UNKNOWN_REQUEST(2),
        DISCONNECT(3);

        private final byte messageCode;
        private MessageType(int messageCode) {
            this.messageCode = (byte) messageCode;
        }
        private byte getMessageCode() {
            return messageCode;
        }
    }
    private TorrentConnectionStreamsUtils() {}


    public static class Client {
        public static record AvailablePiecesRequestInfo(int descriptor, ArrayList<Integer> availablePieces) {}
        public static void sendRequestForAvailablePieces(OutputStream out, byte[] infoSha1Key) throws IOException {
            byte[] message = new byte[infoSha1Key.length + 1 + Ints.BYTES];
            message[0] = MessageType.AVAILABLE_REQUEST.getMessageCode();
            System.arraycopy(Ints.toByteArray(infoSha1Key.length), 0, message, 1, Ints.BYTES);
            System.arraycopy(infoSha1Key, 0, message, 1, infoSha1Key.length);
            out.write(message);
        }

        public static AvailablePiecesRequestInfo getAvailablePieces(InputStream in) throws IOException {
            byte[] readArray = new byte[Ints.BYTES];
            in.read(readArray);
            int descriptor = Ints.fromByteArray(readArray);
            in.read(readArray);
            ArrayList<Integer> available = new ArrayList<>(Ints.fromByteArray(readArray));
            for (int i = 0; i < Ints.fromByteArray(readArray); ++i) {
                in.read(readArray);
//                if (readBytes < Ints.BYTES) {
//                    return new AvailablePiecesRequestInfo(descriptor, available);
//                }
                available.add(Ints.fromByteArray(readArray));
            }
            return new AvailablePiecesRequestInfo(descriptor, available);
        }

        public static void sendRequestForPiece(OutputStream out, int descriptor, int pieceIndex) throws IOException {
            out.write(MessageType.PIECE_REQUEST.getMessageCode());
            out.write(Ints.toByteArray(descriptor));
            out.write(Ints.toByteArray(pieceIndex));
        }

        public static byte[] getPiece(InputStream in) throws IOException {
            byte[] sizeArray = in.readNBytes(Ints.BYTES);
            return in.readNBytes(Ints.fromByteArray(sizeArray));
        }

        public static void sendDisconnect(OutputStream out) throws IOException {
            out.write(MessageType.DISCONNECT.getMessageCode());
        }
    }


    public static class Server {
        public static record PieceRequestInfo(int descriptor, int pieceIndex) {}
        public static MessageType readMessageType(InputStream in) throws IOException {
            int messageCode = in.read();
            for (MessageType type : MessageType.values()) {
                if (type.getMessageCode() == messageCode) {
                    return type;
                }
            }
            return MessageType.UNKNOWN_REQUEST;
        }

        public static byte[] getRequestForAvailablePieces(InputStream in) throws IOException {
            int messageLength = Ints.fromByteArray(in.readNBytes(4));
            return in.readNBytes(messageLength);
        }

        public static void sendAvailablePieces(OutputStream out, ArrayList<Integer> available, int descriptor) throws IOException {
            out.write(Ints.toByteArray(descriptor));
            out.write(Ints.toByteArray(available.size()));
            for (Integer integer : available) {
                out.write(Ints.toByteArray(integer));
            }
        }

        public static PieceRequestInfo getRequestForPiece(InputStream in) throws IOException {
            byte[] readArray = new byte[Ints.BYTES];
            in.read(readArray);
            int descriptor = Ints.fromByteArray(readArray);
            in.read(readArray);
            return new PieceRequestInfo(descriptor, Ints.fromByteArray(readArray));
        }

        public static void sendPiece(OutputStream out, byte[] piece) throws IOException {
            out.write(Ints.toByteArray(piece.length));
            out.write(piece);
        }
    }



//    public static int getRequestForPiece(InputStream in) throws IOException {
//        return Ints.fromByteArray(in.readNBytes(Ints.BYTES));
//    }
//
//    public static byte[] getPiece(InputStream in) throws IOException {
//        int pieceSize = Ints.fromByteArray(in.readNBytes(Ints.BYTES));
//        byte[] piece = new byte[pieceSize];
//        int gotSize = in.read(piece);
//        if (gotSize < pieceSize) {
//            return null;
//        }
//        return piece;
//    }
//
//    public static void sendPiece(OutputStream out, byte[] piece) throws IOException {
//        out.write(Ints.toByteArray(piece.length));
//        out.write(piece);
//    }
//
//    public static void sendInfoShaKey(OutputStream out, byte[] key) throws IOException {
//        out.write(key);
//    }
//
//    public static byte[] getShaDownloadKey(InputStream in) throws IOException {
//       return in.readNBytes(20);
//    }
}
