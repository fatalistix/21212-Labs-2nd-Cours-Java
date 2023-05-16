package ru.nsu.balashov.torrent;

import com.google.common.base.Splitter;
import net.seedboxer.bencode.BDecoder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.units.qual.A;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class TorrentFileData {
    private record FileData(String path, long size) {}
    private final BDecoder bDecoder = new BDecoder();
    private Instant creationTime = null;
    private String urlListStr = null;
    private String comment = null;
    private ArrayList<ArrayList<byte[]>> announceList = null;
    private String author = null;
    private String announce = null;
    private long singleFileLength = -1;
    private String torrentName = null;
    private long pieceLength = -1;
    private ArrayList<byte[]> sha1Sums = null;
    private final ArrayList<FileData> torrentFilesPartsList = null;



    public TorrentFileData() {}

    public TorrentFileData(@NonNull InputStream is) throws IOException {
        this.decode(is);
    }

    @SuppressWarnings("unchecked")
    public void decode(@NonNull InputStream is) throws IOException {
        Map<String, Object> torrentFileStructure = (Map<String, Object>) bDecoder.decodeStream(new BufferedInputStream(is));
        for (String fileKeys : torrentFileStructure.keySet()) {
            switch (fileKeys) {
                case "creation date"    -> creationTime = Instant.ofEpochSecond((long) torrentFileStructure.get(fileKeys));
                case "url-list"         -> urlListStr   = new String((byte[]) torrentFileStructure.get(fileKeys), StandardCharsets.UTF_8);
                case "comment"          -> comment      = new String((byte[]) torrentFileStructure.get(fileKeys), StandardCharsets.UTF_8);
                case "announce-list"    -> announceList = (ArrayList<ArrayList<byte[]>>) torrentFileStructure.get(fileKeys);
                case "created by"       -> author       = new String((byte[]) torrentFileStructure.get(fileKeys), StandardCharsets.UTF_8);
                case "announce"         -> announce     = new String((byte[]) torrentFileStructure.get(fileKeys), StandardCharsets.UTF_8);
                case "info"             -> {
                    Map<String, Object> info = (Map<String, Object>) torrentFileStructure.get(fileKeys);
                    for (String infoKey : info.keySet()) {
                        switch (infoKey) {
                            case "pieces"       -> {
                                byte[] buffer = (byte[]) info.get(infoKey);
                                sha1Sums = new ArrayList<>(buffer.length / 20);
                                for (int i = 0; i < buffer.length; i += 20) {
                                    sha1Sums.add(Arrays.copyOfRange(buffer, i, i + 20));
                                }
                            }
                            case "length"       -> singleFileLength = (long) info.get(infoKey);
                            case "name"         -> torrentName = new String((byte []) info.get(infoKey), StandardCharsets.UTF_8);
                            case "piece length" -> pieceLength = (long) info.get(infoKey);
//                            case "files"        -> {
//                                torrentFilesPartsList = new ArrayList<>();
//                                for (Map<String, Object>  : (ArrayList<Map<String, Object>>) info.get(infoKey)) {
//
//                                }
//                            }
                        }
                    }
                }
            }
        }
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public String getUrlListStr() {
        return urlListStr;
    }

    public String getComment() {
        return comment;
    }

    public ArrayList<ArrayList<byte[]>> getAnnounceList() {
        return announceList;
    }

    public String getAuthor() {
        return author;
    }

    public String getAnnounce() {
        return announce;
    }

    public long getSingleFileLength() {
        return singleFileLength;
    }

    public String getTorrentName() {
        return torrentName;
    }

    public long getPieceLength() {
        return pieceLength;
    }

    public byte[] getSha1ByIndex(int index) {
        return sha1Sums.get(index);
    }
    public byte[][] getSha1Sums() {
        return sha1Sums.toArray(new byte[][]{});
    }
}
