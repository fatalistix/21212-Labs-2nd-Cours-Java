package ru.nsu.balashov.torrent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class SavedFilesManager {
    private record TorrentInfo(SerializableDownloadedInfo serializableInfo, FileWrapper file) {}
    private record SerializableDownloadedInfo(String pathToDownloaded, byte[][] sha1Sums, long singleFileLength, String name,
                                              long pieceLength, byte[] infoHash) {}
    public final static String pathToJson = System.getProperty("user.home") + "/.config/xf/Saved1.json";
    private final ConcurrentHashMap<ByteBuffer, TorrentInfo> allDownloadedInfo = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().create();
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
                    System.out.println(instance);
                    try {
                        FileWrapper fileWrapper = new FileWrapper(instance.pathToDownloaded(), (int) instance.pieceLength(), instance.singleFileLength(), instance.sha1Sums());
                        allDownloadedInfo.put(ByteBuffer.wrap(instance.infoHash()), new TorrentInfo(instance, fileWrapper));
                    } catch (IOException ignored) {

                    }
                }
            }
        }
    }

    public void registerDownloaded(TorrentFileData torrentFileData, String pathToDownloaded) throws RecordExistsException, IOException {
        if (allDownloadedInfo.containsKey(torrentFileData.getInfoHash())) {
            throw new RecordExistsException(torrentFileData.getTorrentName() + " already exists");
        }
        SerializableDownloadedInfo sDInfo = new SerializableDownloadedInfo(pathToDownloaded,
                torrentFileData.getSha1Sums(), torrentFileData.getSingleFileLength(),
                torrentFileData.getTorrentName(), torrentFileData.getPieceLength(), torrentFileData.getInfoHash().array());
        FileWrapper fileWrapper = new FileWrapper(pathToDownloaded, (int) torrentFileData.getPieceLength(), torrentFileData.getSingleFileLength(), torrentFileData.getSha1Sums());
        allDownloadedInfo.put(torrentFileData.getInfoHash(), new TorrentInfo(sDInfo, fileWrapper));
    }

    public synchronized void storeDownloaded() throws IOException {
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
        return allDownloadedInfo.get(infoHash).file().getBitmask();
    }


    public byte[] getPiece(ByteBuffer infoHash, int index) {
        if (!allDownloadedInfo.containsKey(infoHash)) {
            return null;
        }
        try {
            return allDownloadedInfo.get(infoHash).file().readPiece(index);
        } catch (IOException e) {
            return null;
        }
    }

    public void writePiece(ByteBuffer infoHash, byte[] piece, int pieceIndex) throws IOException {
        if (allDownloadedInfo.containsKey(infoHash)) {
            allDownloadedInfo.get(infoHash).file().writePiece(piece, pieceIndex);
//            allDownloadedInfo.get(infoHash).file().bitmask()[pieceIndex / 8] |= (1 >> (7 - pieceIndex % 8)) & 1;
        }
    }

    public byte[] getHash(ByteBuffer infoHash, int pieceIndex) {
        if (allDownloadedInfo.containsKey(infoHash) &&
                pieceIndex < allDownloadedInfo.get(infoHash).serializableInfo().sha1Sums().length) {
            return allDownloadedInfo.get(infoHash).serializableInfo().sha1Sums()[pieceIndex];
        }
        return null;
    }

    public boolean exists(TorrentFileData torrentFileData) {
        return allDownloadedInfo.containsKey(torrentFileData.getInfoHash());
    }

    public int getNumOfPieces(ByteBuffer infoHash) {
        if (allDownloadedInfo.containsKey(infoHash)) {
            return allDownloadedInfo.get(infoHash).file().getNumberOfPieces();
        }
        return -1;
    }

    public static class RecordExistsException extends Exception {
        public RecordExistsException(String message) {
            super(message);
        }
    }
}
