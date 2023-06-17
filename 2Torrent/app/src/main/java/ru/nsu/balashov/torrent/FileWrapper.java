package ru.nsu.balashov.torrent;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

public class FileWrapper implements AutoCloseable {
    private final RandomAccessFile file;
//    private byte[] bitfield;
    private final long fileLength;
    private final int pieceLength;
    private final Object monitor = this;
    private final byte[] bitmask;
    private final int numOfPieces;
    private int currentlyAvailablePieces = 0;

    public FileWrapper(String pathToFile, int pieceLength, long fileLength, byte[][] shaSums) throws IOException {
        this.file = new RandomAccessFile(pathToFile, "rw");
        if (this.file.length() != fileLength) {
            this.file.seek(fileLength - 1);
            this.file.write(0);
            this.file.setLength(fileLength);
        }
        this.pieceLength = pieceLength;
        this.fileLength  = fileLength;
        this.numOfPieces = countNumOfPieces((int) fileLength, pieceLength);
        this.bitmask = createExistingPartsBitmask(shaSums);
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
            ++currentlyAvailablePieces;
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

    public byte[] getBitmask() {
        synchronized (monitor) {
            return bitmask;
        }
    }

    public int getNumberOfPieces() {
        return numOfPieces;
    }

    public int getCurrentlyAvailablePieces() {
        synchronized (monitor) {
            return currentlyAvailablePieces;
        }
    }


    private int countNumOfPieces(int fileLength, int pieceLength) {
        return (fileLength / pieceLength) + ((fileLength % pieceLength == 0) ? 0 : 1);
    }

    private byte[] createExistingPartsBitmask(byte[][] shaSums) throws IOException {
        byte[] pieceBuffer = new byte[pieceLength];
        byte[] bitfield = new byte[numOfPieces / Byte.SIZE + ((numOfPieces % Byte.SIZE == 0) ? 0 : 1)];
        int readBytes;
        for (int i = 0; i < numOfPieces; ++i) {
            readBytes = this.readPiece(pieceBuffer, i);
            if (readBytes == pieceLength) {
                if (Arrays.equals(shaSums[i], DigestUtils.sha1(pieceBuffer))) {
                    bitfield[i / 8] |= (1 << (7 - (i % 8)));
                    ++currentlyAvailablePieces;
                }
            } else {
                if (Arrays.equals(shaSums[i], DigestUtils.sha1(Arrays.copyOf(pieceBuffer, readBytes)))) {
                    bitfield[i / 8] |= (1 << (7 - (i % 8)));
                    ++currentlyAvailablePieces;
                }
            }
        }
        return bitfield;
    }
}
