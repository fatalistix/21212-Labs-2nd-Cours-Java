package ru.nsu.balashov.torrent;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import com.google.common.base.Splitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;

public class TorrentFileData {
    private Instant creationTime = null;
    private String urlListStr = null;
    private String comment = null;
    private ArrayList<ArrayList<byte[]>> announceList = null;
    private String author = null;
    private String announce = null;
    private long singleFileLength = -1;
    private String torrentName = null;
    private long pieceLength = -1;
    private ArrayList<String> sha1Sums = null;
    private byte[] infoHash = null;



    public TorrentFileData() {}

    public TorrentFileData(@NonNull InputStream is) throws IOException {
        this.decode(is);
    }

//    private void recountInfoSha1Sum() {
//        byte[] buffer = torrentName.getBytes(StandardCharsets.UTF_8);
//        for (String array : sha1Sums) {
//            buffer = Bytes.concat(array, buffer);
//        }
//        infoHash = DigestUtils.sha1(buffer);
//    }

    public void decode(@NonNull String path) throws IOException {
        File torrentFile = new File(path);
        try (InputStream torrentFileIn = Files.newInputStream(torrentFile.toPath())) {
            decode(torrentFileIn);
        }
    }

    @SuppressWarnings("unchecked")
    public void decode(@NonNull InputStream is) throws IOException {
        byte[] torrentFileBytes = is.readAllBytes();
        Bencode bencode = new Bencode(StandardCharsets.US_ASCII);
        Map<String, Object> torrentFileStructure = bencode.decode(torrentFileBytes, Type.DICTIONARY);
        for (String fileKeys : torrentFileStructure.keySet()) {
            switch (fileKeys) {
                case "creation date"    -> creationTime = Instant.ofEpochSecond((long) torrentFileStructure.get(fileKeys));
                case "url-list"         -> urlListStr   = (String) torrentFileStructure.get(fileKeys);
                case "comment"          -> comment      = (String) torrentFileStructure.get(fileKeys);
                case "announce-list"    -> announceList = (ArrayList<ArrayList<byte[]>>) torrentFileStructure.get(fileKeys);
                case "created by"       -> author       = (String) torrentFileStructure.get(fileKeys);
                case "announce"         -> announce     = (String) torrentFileStructure.get(fileKeys);
                case "info"             -> {
                    Map<String, Object> info = (Map<String, Object>) torrentFileStructure.get(fileKeys);
                    for (String infoKey : info.keySet()) {
                        switch (infoKey) {
                            case "pieces"       -> {
                                sha1Sums = new ArrayList<>(Splitter.fixedLength(20).splitToList((String) info.get(infoKey)));
                            }
                            case "length"       -> {
                                singleFileLength = (long) info.get(infoKey);
                            }
                            case "name"         -> {
                                torrentName = (String) info.get(infoKey);
                            }
                            case "piece length" -> {
                                pieceLength = (long) info.get(infoKey);
                            }
//                            case "files"        -> {
//                                torrentFilesPartsList = new ArrayList<>();
//                                for (Map<String, Object>  : (ArrayList<Map<String, Object>>) info.get(infoKey)) {
//
//                                }
//                            }
                        }
                    }
                    infoHash = DigestUtils.sha1(bencode.encode(info));
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

    public String getSha1ByIndex(int index) {
        return sha1Sums.get(index);
    }
    public String[] getSha1Sums() {
        return sha1Sums.toArray(new String[0]);
    }
    public byte[] getInfoHash() {
        return infoHash;
    }
}
