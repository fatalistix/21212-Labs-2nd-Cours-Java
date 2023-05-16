package ru.nsu.balashov.torrent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TorrentCore {
    private SavedFilesManager savedFilesManager;


    /**
     * @throws IOException when problems with loading saved data from .json file
     */
    public TorrentCore() throws IOException {
        savedFilesManager = new SavedFilesManager();
    }


    /**
     * @param path to .torrent file
     * @throws IOException if specified {@code path} it's not a torrent file
     *
     */
    private void downloadTorrent(String path, String pathToSave, ArrayList<String> ipWithPortList) throws IOException {
        File torrentFile = new File(path);
        TorrentFileData torrentFileData = new TorrentFileData();
        try (InputStream torrentFileIn = Files.newInputStream(torrentFile.toPath())) {
            torrentFileData.decode(torrentFileIn);
        }
        if (savedFilesManager.exists(torrentFileData)) {
            throw new IllegalArgumentException("Downloaded instance already exists");
        }

    }
}
