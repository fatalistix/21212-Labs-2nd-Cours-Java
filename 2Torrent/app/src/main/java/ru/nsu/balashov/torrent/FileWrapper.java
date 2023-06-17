package ru.nsu.balashov.torrent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

public class FileWrapper implements AutoCloseable{
    private final RandomAccessFile file;
//    private byte[] bitfield;
    private final long fileLength;
    private final int pieceLength;
    private final Object monitor = this;

    public FileWrapper(String pathToFile, int pieceLength, long fileLength) throws IOException {
        this.file = new RandomAccessFile(pathToFile, "rw");
        if (this.file.length() != fileLength) {
            this.file.seek(fileLength - 1);
            this.file.write(0);
        }
//        this.fileLength = fileLength;
        this.pieceLength = pieceLength;
        this.fileLength = fileLength;
//        int numOfPieces = fileLength / pieceLength + ((fileLength % pieceLength == 0) ? 0 : 1);
//        this.bitfield = new byte[numOfPieces / Byte.SIZE
//                + ((numOfPieces % Byte.SIZE == 0) ? 0 : 1)];
//
//        byte[] pieceBuffer = new byte[pieceLength];
//        int readBytes;
//        try {
//            file.seek(0);
//            for (int i = 0; i < numOfPieces; ++i) {
//                readBytes = file.read(pieceBuffer);
//                if (readBytes == pieceLength) {
//                    if (Arrays.equals(sha1Sums[i].getBytes(StandardCharsets.US_ASCII),
//                            pieceBuffer)) {
//                        bitfield[i / 8] |= (1 << (7 - (i % 8)));
//                    }
//                } else {
//                    if (Arrays.equals(sha1Sums[i].getBytes(StandardCharsets.US_ASCII),
//                            Arrays.copyOf(pieceBuffer, readBytes))) {
//                        bitfield[i / 8] |= (1 << (7 - (i % 8)));
//                    }
//                }
//            }
//        } catch (IOException e) {
//            file.close();
//        }
//
    }

    @Override
    public void close() throws IOException {
        synchronized (monitor) {
            file.close();
        }
    }

    public void writePiece(byte[] piece, int index) throws IOException {
        if ((long) index * pieceLength > fileLength) {
            throw new IOException("Index out of file length");
        }
        synchronized (monitor) {
            file.seek((long) index * pieceLength);
            file.write(piece);
        }
    }

    public byte[] readPiece(int index) throws IOException {
        if ((long) index * pieceLength > fileLength) {
            throw new IOException("Index out of file length");
        }
        byte[] buffer = new byte[pieceLength];
        int readBytes;
        synchronized (monitor) {
            file.seek((long) index * pieceLength);
            readBytes = file.read(buffer);
        }
        return Arrays.copyOf(buffer, readBytes);
    }

    public int readPiece(byte[] array, int index) throws IOException {
        if ((long) index * pieceLength > fileLength) {
            throw new IOException("Index out of file length");
        }
        synchronized (monitor) {
            file.seek((long) index * pieceLength);
            return file.read(array);
        }
    }

    public ArrayList<byte[]> readNPieces(int startIndex, int numToRead) throws IOException {
        if (((long) startIndex + numToRead) * pieceLength > fileLength) {
            throw new IOException("Index out of file length");
        }
        byte[] buffer = new byte[pieceLength * numToRead];
        int readBytes;
        ArrayList<byte[]> pieces = new ArrayList<>(numToRead);
        synchronized (monitor) {
            file.seek((long) startIndex * pieceLength);
            readBytes = file.read(buffer);
        }
        for (int i = 0; i < numToRead; ++i) {
            if (i * pieceLength > readBytes) {
                break;
            }
            if ((i + 1) * pieceLength > readBytes) {
                pieces.add(Arrays.copyOfRange(buffer, i * pieceLength, readBytes));
            } else {
                pieces.add(Arrays.copyOfRange(buffer, i * pieceLength, (i + 1) * pieceLength));
            }

        }
        return pieces;
    }
}
