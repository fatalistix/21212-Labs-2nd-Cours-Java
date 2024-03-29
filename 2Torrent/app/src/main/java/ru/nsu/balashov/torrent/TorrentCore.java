package ru.nsu.balashov.torrent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class TorrentCore {
    private final SavedFilesManager savedFilesManager;
    private final Client client;
    private final Server server;
    private Thread uploadListener;
    private Thread downloader;
    private boolean errorHappened = false;
    public TorrentCore() throws IOException {
        savedFilesManager = new SavedFilesManager();
        client = new Client(savedFilesManager);
        server = new Server();
    }

    public void uploadTorrents() throws CoreException {
        if (errorHappened) {
            throw new CoreException("Error happened");
        }
        if (uploadListener == null) {
            uploadListener = new Thread(() -> {
                try {
                    server.startUploading(savedFilesManager);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            uploadListener.setUncaughtExceptionHandler((t, e) -> {
                errorHappened = true;
                e.printStackTrace();
            });
            uploadListener.start();
        }
//        try {
//            server.startUploading(savedFilesManager);
//        } catch (Exception e) {
//            System.out.println("GOT EXCEPTION: " + e.getMessage());
//            e.printStackTrace();
//        }
    }

    public void downloadTorrent(TorrentFileData torrentFileData, String pathToSave, ArrayList<String> ipWithPorts) throws CoreException, RecordException {
        if (errorHappened) {
            throw new CoreException("Error happened");
        }
        if (savedFilesManager.exists(torrentFileData)) {
            throw new RecordException("Is downloaded or in downloading process");
        }
        try {
            savedFilesManager.registerDownloaded(torrentFileData, pathToSave + "/" + torrentFileData.getTorrentName());
        } catch (Exception e) {
            throw new RecordException("Error registering download");
        }
        lazyDownload();
        client.newDownload(ipWithPorts, torrentFileData.getInfoHash());
//        try {
//            client.startDownload();
//        } catch (Exception e) {
//            System.out.println("EXCEPTION: " + e.getMessage());
//            e.printStackTrace();
//        }
    }

    public void stopServer() throws CoreException {
        if (errorHappened) {
            throw new CoreException("Error happened");
        }
        uploadListener.interrupt();
    }

    public void killServer() {
        server.kill();
    }

    public void addDownloadedTorrent(TorrentFileData torrentFileData, String pathToSave) throws RecordException, CoreException {
        if (errorHappened) {
            throw new CoreException("Error happened");
        }
        if (savedFilesManager.exists(torrentFileData)) {
            throw new RecordException("Already exists");
        }
        try {
            savedFilesManager.registerDownloaded(torrentFileData, pathToSave);
        } catch (SavedFilesManager.RecordExistsException | IOException e) {
            throw new RecordException(e);
        }
    }

    public void close() {
        try {
            savedFilesManager.storeDownloaded();
            if (uploadListener != null) {
                uploadListener.interrupt();
            }
            if (downloader != null) {
                downloader.interrupt();
            }
            server.kill();
            client.kill();
        } catch (IOException ignored) {
        }
    }

    public ArrayList<NotCompleteDownloaded> getNotCompleteDownloadedList() {
        return savedFilesManager.getNotCompleteDownloadedList();
    }

    public void resumeDownloading(ByteBuffer infoHash, ArrayList<String> ipWithPort) {
        lazyDownload();
        if (client.isDownloading(infoHash)) {
            client.addSources(infoHash, ipWithPort);
        } else {
            client.newDownload(ipWithPort, infoHash);
        }
    }

    private void lazyDownload() {
        if (downloader == null) {
            downloader = new Thread(() -> {
                try {
                    client.startDownload();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            downloader.setUncaughtExceptionHandler((t, e) -> {
                errorHappened = true;
//                e.printStackTrace();
            });
            downloader.start();
        }
    }

    public static class CoreException extends Exception {
        public CoreException(String message) {
            super(message);
        }
        public CoreException(Throwable cause) {
            super(cause);
        }
    }

    public static class RecordException extends Exception {
        public RecordException(String message, Throwable cause) {
            super(message, cause);
        }
        public RecordException(String message) {
            super(message);
        }
        public RecordException(Throwable cause) {
            super(cause);
        }
    }
}
