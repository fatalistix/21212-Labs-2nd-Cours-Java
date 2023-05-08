package ru.nsu.balashov.torrent;

import com.google.common.base.Splitter;
import net.seedboxer.bencode.BDecoder;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class TorrentFileData {
    private static record FileData(String path, long size) {}
    private final BDecoder bDecoder = new BDecoder();
    private Map<String, Object> torrentFileStructure = null;
    private Instant creationTime = null;
    private String urlListStr = null;
    private String comment = null;
    private ArrayList<ArrayList<byte[]>> announceList = null;
    private String author = null;
    private String announce = null;
    private Map<String, Object> info = null;
    private long singleFileLength = -1;
    private String torrentName = null;
    private long pieceLength = -1;
    private ArrayList<String> sha1Sums = null;
    private ArrayList<FileData> torrentFilesPartsList = null;



    public TorrentFileData() {}

    public TorrentFileData(@NonNull InputStream is) throws IOException {
        this.decode(is);
    }

    @SuppressWarnings("unchecked")
    public void decode(@NonNull InputStream is) throws IOException {
        torrentFileStructure = (Map<String, Object>) bDecoder.decodeStream(new BufferedInputStream(is));
        for (String fileKeys : torrentFileStructure.keySet()) {
            switch (fileKeys) {
                case "creation date"    -> creationTime = Instant.ofEpochSecond((long) torrentFileStructure.get(fileKeys));
                case "url-list"         -> urlListStr   = new String((byte[]) torrentFileStructure.get(fileKeys), StandardCharsets.UTF_8);
                case "comment"          -> comment      = new String((byte[]) torrentFileStructure.get(fileKeys), StandardCharsets.UTF_8);
                case "announce-list"    -> announceList = (ArrayList<ArrayList<byte[]>>) torrentFileStructure.get(fileKeys);
                case "created by"       -> author       = new String((byte[]) torrentFileStructure.get(fileKeys), StandardCharsets.UTF_8);
                case "announce"         -> announce     = new String((byte[]) torrentFileStructure.get(fileKeys), StandardCharsets.UTF_8);
                case "info"             -> {
//                    info = (Map<String, Object>) torrentFileStructure.get(fileKeys);
//                    for (String infoKey : info.keySet()) {
//                        switch (infoKey) {
//                            case "pieces"       -> sha1Sums = new ArrayList<>(Splitter.fixedLength(20)
//                                    .splitToList(new String((byte[]) info.get(infoKey), StandardCharsets.UTF_8)));
//                            case "length"       -> singleFileLength = (long) info.get(infoKey);
//                            case "name"         -> torrentName = new String((byte []) info.get(infoKey), StandardCharsets.UTF_8);
//                            case "piece length" -> pieceLength = (long) info.get(infoKey);
//                            case "files"        -> {
//                                torrentFilesPartsList = new ArrayList<>();
//                                for (Map<String, Object>  : (ArrayList<Map<String, Object>>) info.get(infoKey)) {
//
//                                }
//                            }
//                        }
//                    }
                }
            }
        }
    }
}
