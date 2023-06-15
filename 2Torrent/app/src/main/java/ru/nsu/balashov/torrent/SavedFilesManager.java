package ru.nsu.balashov.torrent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class SavedFilesManager {
    private record DownloadedInfo(String pathToDownloaded, byte[][] sha1Sums, long singleFileLength, String name,
                                  long pieceLength, byte[] infoHash) {}
    private record FileWithBitmask(FileWrapper fileWrapper, byte[] bitmask) {}
    public final static String pathToJson = "/home/vyacheslav/.config/xf/Saved.json";
    private final ConcurrentHashMap<ByteBuffer, DownloadedInfo> allDownloadedInfo = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ByteBuffer, FileWithBitmask> torrentsExistingPartsCache = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File jsonFile;

    private byte[] createExistingPartsBitmask(final DownloadedInfo downloadedInfo, FileWrapper file) {
        int numOfPieces = (int) (downloadedInfo.singleFileLength() / downloadedInfo.pieceLength()
                + ((downloadedInfo.singleFileLength() % downloadedInfo.pieceLength() == 0) ? 0 : 1));
        byte[] pieceBuffer = new byte[(int) downloadedInfo.pieceLength()];
        byte[] bitfield = new byte[numOfPieces / Byte.SIZE + ((numOfPieces % Byte.SIZE == 0) ? 0 : 1)];
        int readBytes;
        try {
            for (int i = 0; i < numOfPieces; ++i) {
                readBytes = file.readPiece(pieceBuffer, i);
                if (readBytes == downloadedInfo.pieceLength()) {
                    if (Arrays.equals(downloadedInfo.sha1Sums()[i], DigestUtils.sha1(pieceBuffer))) {
                        bitfield[i / 8] |= (1 << (7 - (i % 8)));
                    }
                } else {
                    if (Arrays.equals(downloadedInfo.sha1Sums()[i], DigestUtils.sha1(Arrays.copyOf(pieceBuffer, readBytes)))) {
                        bitfield[i / 8] |= (1 << (7 - (i % 8)));
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        return bitfield;
    }

    public SavedFilesManager() throws IOException {
        jsonFile = new File(pathToJson);
        if (!jsonFile.exists()) {
            jsonFile.createNewFile();
            return;
        }
        try (InputStream jsonInputStream = new FileInputStream(jsonFile)) {
            JsonArray mainJsonArray = gson.fromJson(new InputStreamReader(jsonInputStream, StandardCharsets.UTF_8),
                    JsonArray.class);
            if (mainJsonArray != null) {
                for (JsonElement dataJe : mainJsonArray) {
                    DownloadedInfo instance = gson.fromJson(dataJe, DownloadedInfo.class);
//                    for (byte b : instance.infoHash) {
//                        System.out.print(b + " ");
//                    }
//                    System.out.println();
                    System.out.println(instance);
                    try {
                        FileWrapper fileWrapper = new FileWrapper(instance.pathToDownloaded(), (int) instance.pieceLength(), instance.singleFileLength());
                        byte[] existingParts = createExistingPartsBitmask(instance, fileWrapper);
                        if (existingParts != null) {
                            allDownloadedInfo.put(ByteBuffer.wrap(instance.infoHash()), instance);
                            torrentsExistingPartsCache.put(ByteBuffer.wrap(instance.infoHash()),
                                    new FileWithBitmask(fileWrapper, existingParts));
                        }
                    } catch (IOException ignored) {

                    }
                }
            }
        }
    }

    public void registerDownloaded(TorrentFileData torrentFileData, String pathToDownloaded) throws RecordExistsException, IOException {
        if (allDownloadedInfo.containsKey(ByteBuffer.wrap(torrentFileData.getInfoHash()))) {
            throw new RecordExistsException(torrentFileData.getTorrentName() + " already exists");
        }
        allDownloadedInfo.put(ByteBuffer.wrap(torrentFileData.getInfoHash()), new DownloadedInfo(pathToDownloaded,
                torrentFileData.getSha1Sums(), torrentFileData.getSingleFileLength(),
                torrentFileData.getTorrentName(), torrentFileData.getPieceLength(), torrentFileData.getInfoHash()));
        FileWrapper fileWrapper = new FileWrapper(pathToDownloaded, (int) torrentFileData.getPieceLength(), torrentFileData.getSingleFileLength());
        torrentsExistingPartsCache.put(ByteBuffer.wrap(torrentFileData.getInfoHash()),
                new FileWithBitmask(fileWrapper, createExistingPartsBitmask(allDownloadedInfo.get(ByteBuffer.wrap(torrentFileData.getInfoHash())), fileWrapper)));
    }

    public void storeDownloaded() throws IOException {
//        URL jsonUrl = this.getClass().getResource(pathToJson);
//        if (jsonUrl == null) {
//            System.out.println("STORE DOWNLOADED FAIL");
//            throw new FileNotFoundException("Cannot open json file");
//        }
        try (Writer writer = new FileWriter(jsonFile)) {
            gson.toJson(allDownloadedInfo.values().toArray(new DownloadedInfo[0]), writer);
        }
    }

    public byte[] getExistingParts(byte[] infoHash) {
        if (!allDownloadedInfo.containsKey(ByteBuffer.wrap(infoHash))) {
            return null;
        }
//        if (torrentsExistingPartsCache.containsKey(infoHash)) {
//            return torrentsExistingPartsCache.get(infoHash).bitmask();
//        } else {
//
//            byte[] existingParts = createExistingPartsBitmask(allDownloadedInfo.get(infoHash), );
//            torrentsExistingPartsCache.put(infoHash, existingParts);
//            return existingParts;
//        }
        return torrentsExistingPartsCache.get(ByteBuffer.wrap(infoHash)).bitmask();
    }

    public byte[] getPiece(byte[] infoHash, int index) {
        if (!torrentsExistingPartsCache.containsKey(ByteBuffer.wrap(infoHash))) {
            return null;
        }
        try {
            return torrentsExistingPartsCache.get(ByteBuffer.wrap(infoHash)).fileWrapper.readPiece(index);
        } catch (IOException e) {
            return null;
        }
    }

    public void writePiece(byte[] infoHash, byte[] piece, int pieceIndex) throws IOException {
        if (torrentsExistingPartsCache.containsKey(ByteBuffer.wrap(infoHash))) {
            torrentsExistingPartsCache.get(ByteBuffer.wrap(infoHash)).fileWrapper().writePiece(piece, pieceIndex);
            torrentsExistingPartsCache.get(ByteBuffer.wrap(infoHash)).bitmask[pieceIndex / 8] |= (1 >> (7 - pieceIndex % 8)) & 1;
        }
    }

    public byte[] getHash(byte[] infoHash, int pieceIndex) {
        if (torrentsExistingPartsCache.containsKey(ByteBuffer.wrap(infoHash))) {
            return allDownloadedInfo.get(ByteBuffer.wrap(infoHash)).sha1Sums()[pieceIndex];
        }
        return null;
    }

    public boolean exists(TorrentFileData torrentFileData) {
        return allDownloadedInfo.containsKey(ByteBuffer.wrap(torrentFileData.getInfoHash()));
    }

    public static class RecordExistsException extends Exception {
        public RecordExistsException(String message) {
            super(message);
        }
    }

}
