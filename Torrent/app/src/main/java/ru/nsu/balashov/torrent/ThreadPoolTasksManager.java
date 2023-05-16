package ru.nsu.balashov.torrent;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPoolTasksManager {

    @FunctionalInterface
    private static interface DownloadAndWritePartsInRange {
        public void run(TorrentFileData torrentFileData, RandomAccessFile file, int start, int end, InputStream in);
    }


    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    private BlockingQueue<Runnable> tasksQueue = new LinkedBlockingQueue<>();
    public ThreadPoolTasksManager() {}

    public void submitDownloadPart(int index, TorrentFileData torrentFileData, RandomAccessFile file) {
//        tasksQueue.add(() -> {})
    }
}
