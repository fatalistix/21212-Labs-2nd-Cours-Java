package ru.nsu.balashov.torrent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Selector;
import java.util.ArrayList;

public class TorrentCore {
    private final SavedFilesManager savedFilesManager;
    private Thread uploadListener;
    private final ThreadPoolTasksManager threadPool = new ThreadPoolTasksManager();
    private final static int DEFAULT_LISTENING_PORT = 6969;
    private final Selector serverSelector = Selector.open();

    /**
     * @throws IOException when problems with loading saved data from json file
     */
    public TorrentCore() throws IOException {
        savedFilesManager = new SavedFilesManager();
    }

    public void uploadTorrents() {
//        uploadListener = new Thread(() -> {
//            try (ServerSocket serverSocketListener = new ServerSocket(DEFAULT_LISTENING_PORT)) {
//                System.out.println("SERVER SOCKET CREATED");
//                while (true) {
//                    Socket connectedSocket = serverSocketListener.accept();
//                    System.out.printf("NEW CONNECTION FROM %s%n", connectedSocket.getInetAddress().getHostAddress());
//                    threadPool.submitUpload(connectedSocket, savedFilesManager);
//                }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
        uploadListener.start();
    }


    public void downloadTorrent(TorrentFileData torrentFileData, String pathToSave, ArrayList<String> ipWithPortList) throws RecordExistsException, FileNotFoundException {
//        if (savedFilesManager.exists(torrentFileData)) {
//            throw new RecordExistsException("Downloaded instance already exists");
//        }
//        RandomAccessFile downloadedInstanceRAF = new RandomAccessFile(pathToSave +
//                    System.getProperty("file.separator") + torrentFileData.getTorrentName(), "rw");
//        threadPool.submitDownload(torrentFileData, downloadedInstanceRAF, ipWithPortList);
    }

    public void addDownloadedTorrent(TorrentFileData torrentFileData, String pathToData) throws RecordExistsException {
        if (savedFilesManager.exists(torrentFileData)) {
            throw new RecordExistsException("Downloaded instance already exists");
        }
        savedFilesManager.registerDownloaded(torrentFileData, pathToData);
    }

    public static class RecordExistsException extends Exception {
        public RecordExistsException(String message) {
            super(message);
        }
    }
}
