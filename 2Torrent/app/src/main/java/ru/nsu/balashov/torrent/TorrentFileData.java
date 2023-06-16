package ru.nsu.balashov.torrent;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import com.google.common.base.Splitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
    private ArrayList<byte[]> sha1Sums = null;
    private ByteBuffer infoHash = null;



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
        Bencode bencode = new Bencode(StandardCharsets.UTF_8, true);
        Map<String, Object> torrentFileStructure = bencode.decode(torrentFileBytes, Type.DICTIONARY);
        for (String fileKeys : torrentFileStructure.keySet()) {
            switch (fileKeys) {
                case "creation date"    -> creationTime = Instant.ofEpochSecond((long) torrentFileStructure.get(fileKeys));
                case "url-list"         -> urlListStr   = new String(((ByteBuffer) torrentFileStructure.get(fileKeys)).array()).trim();
                case "comment"          -> comment      = new String(((ByteBuffer) torrentFileStructure.get(fileKeys)).array()).trim();
                case "announce-list"    -> announceList = (ArrayList<ArrayList<byte[]>>) torrentFileStructure.get(fileKeys);
                case "created by"       -> author       = new String(((ByteBuffer) torrentFileStructure.get(fileKeys)).array()).trim();
                case "announce"         -> announce     = new String(((ByteBuffer) torrentFileStructure.get(fileKeys)).array()).trim();
                case "info"             -> {
                    Map<String, Object> info = (Map<String, Object>) torrentFileStructure.get(fileKeys);
                    for (String infoKey : info.keySet()) {
                        switch (infoKey) {
                            case "pieces"       -> {
                                byte[] buffer = ((ByteBuffer) info.get(infoKey)).array();
                                sha1Sums = new ArrayList<>(buffer.length / 20);
                                for (int i = 0; i < buffer.length; i += 20) {
                                    sha1Sums.add(Arrays.copyOfRange(buffer, i, i + 20));
                                }
                            }
                            case "length"       -> singleFileLength = (long) info.get(infoKey);
                            case "name"         -> torrentName = new String(((ByteBuffer) info.get(infoKey)).array()).trim();
                            case "piece length" -> pieceLength = (long) info.get(infoKey);
//                            case "files"        -> {
//                                torrentFilesPartsList = new ArrayList<>();
//                                for (Map<String, Object>  : (ArrayList<Map<String, Object>>) info.get(infoKey)) {
//
//                                }
//                            }
                        }
                    }
                    infoHash = ByteBuffer.wrap(DigestUtils.sha1(bencode.encode(info)));
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
        return sha1Sums.toArray(new byte[0][0]);
    }
    public ByteBuffer getInfoHash() {
        return infoHash;
    }
}
