package ru.nsu.balashov.torrent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class SavedFilesManager {
    private record TorrentInfo(SerializableDownloadedInfo serializableInfo, FileWithBitmask file) {}
    private record SerializableDownloadedInfo(String pathToDownloaded, byte[][] sha1Sums, long singleFileLength, String name,
                                              long pieceLength, byte[] infoHash) {}
    private record FileWithBitmask(FileWrapper fileWrapper, byte[] bitmask, int numOfPieces) {}
    public final static String pathToJson = System.getProperty("user.home") + "/.config/xf/Saved1.json";
//    private final ConcurrentHashMap<ByteBuffer, SerializableDownloadedInfo> allDownloadedInfo = new ConcurrentHashMap<>();
//    private final ConcurrentHashMap<ByteBuffer, FileWithBitmask> torrentsExistingPartsCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ByteBuffer, TorrentInfo> allDownloadedInfo = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File jsonFile;


    @SuppressWarnings("ResultOfMethodCallIgnored")
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
                    SerializableDownloadedInfo instance = gson.fromJson(dataJe, SerializableDownloadedInfo.class);
//                    for (byte b : instance.infoHash) {
//                        System.out.print(b + " ");
//                    }
//                    System.out.println();
                    System.out.println(instance);
                    try {
                        FileWrapper fileWrapper = new FileWrapper(instance.pathToDownloaded(), (int) instance.pieceLength(), instance.singleFileLength());
                        byte[] existingParts = createExistingPartsBitmask(instance, fileWrapper);
                        if (existingParts != null) {
                            allDownloadedInfo.put(ByteBuffer.wrap(instance.infoHash()), new TorrentInfo(instance,
                                    new FileWithBitmask(fileWrapper, existingParts, countNumOfPieces(instance))));
                        }
                    } catch (IOException ignored) {

                    }
                }
            }
        }
    }

    private int countNumOfPieces(SerializableDownloadedInfo downloadedInfo) {
        return (int) (downloadedInfo.singleFileLength() / downloadedInfo.pieceLength()
                + ((downloadedInfo.singleFileLength() % downloadedInfo.pieceLength() == 0) ? 0 : 1));
    }

    private byte[] createExistingPartsBitmask(final SerializableDownloadedInfo downloadedInfo, FileWrapper file) {
        int numOfPieces = countNumOfPieces(downloadedInfo);
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

    public void registerDownloaded(TorrentFileData torrentFileData, String pathToDownloaded) throws RecordExistsException, IOException {
        if (allDownloadedInfo.containsKey(torrentFileData.getInfoHash())) {
            throw new RecordExistsException(torrentFileData.getTorrentName() + " already exists");
        }
        SerializableDownloadedInfo sDInfo = new SerializableDownloadedInfo(pathToDownloaded,
                torrentFileData.getSha1Sums(), torrentFileData.getSingleFileLength(),
                torrentFileData.getTorrentName(), torrentFileData.getPieceLength(), torrentFileData.getInfoHash().array());
        FileWrapper fileWrapper = new FileWrapper(pathToDownloaded, (int) torrentFileData.getPieceLength(), torrentFileData.getSingleFileLength());
        byte[] existingParts = createExistingPartsBitmask(sDInfo, fileWrapper);
        int numOfPieces = countNumOfPieces(sDInfo);
        allDownloadedInfo.put(torrentFileData.getInfoHash(), new TorrentInfo(sDInfo, new FileWithBitmask(fileWrapper, existingParts, numOfPieces)));
    }

    public void storeDownloaded() throws IOException {
//        URL jsonUrl = this.getClass().getResource(pathToJson);
//        if (jsonUrl == null) {
//            System.out.println("STORE DOWNLOADED FAIL");
//            throw new FileNotFoundException("Cannot open json file");
//        }
        try (Writer writer = new FileWriter(jsonFile)) {
            ArrayList<SerializableDownloadedInfo> serializableList = new ArrayList<>(allDownloadedInfo.size());
            for (TorrentInfo torrentInfo : allDownloadedInfo.values()) {
                serializableList.add(torrentInfo.serializableInfo());
            }
            gson.toJson(serializableList.toArray(), writer);
        }
    }

    public byte[] getExistingParts(ByteBuffer infoHash) {
        if (!allDownloadedInfo.containsKey(infoHash)) {
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
        return allDownloadedInfo.get(infoHash).file().bitmask();
    }


    public byte[] getPiece(ByteBuffer infoHash, int index) {
        if (!allDownloadedInfo.containsKey(infoHash)) {
            return null;
        }
        try {
            return allDownloadedInfo.get(infoHash).file().fileWrapper().readPiece(index);
        } catch (IOException e) {
            return null;
        }
    }

    public void writePiece(ByteBuffer infoHash, byte[] piece, int pieceIndex) throws IOException {
        if (allDownloadedInfo.containsKey(infoHash)) {
            allDownloadedInfo.get(infoHash).file().fileWrapper().writePiece(piece, pieceIndex);
            allDownloadedInfo.get(infoHash).file().bitmask()[pieceIndex / 8] |= (1 >> (7 - pieceIndex % 8)) & 1;
        }
    }

    public byte[] getHash(ByteBuffer infoHash, int pieceIndex) {
        if (allDownloadedInfo.containsKey(infoHash)) {
            return allDownloadedInfo.get(infoHash).serializableInfo().sha1Sums()[pieceIndex];
        }
        return null;
    }

    public boolean exists(TorrentFileData torrentFileData) {
        return allDownloadedInfo.containsKey(torrentFileData.getInfoHash());
    }

    public int getNumOfPieces(ByteBuffer infoHash) {
        if (allDownloadedInfo.containsKey(infoHash)) {
            return allDownloadedInfo.get(infoHash).file().numOfPieces();
        }
        return -1;
    }

    public static class RecordExistsException extends Exception {
        public RecordExistsException(String message) {
            super(message);
        }
    }
}
