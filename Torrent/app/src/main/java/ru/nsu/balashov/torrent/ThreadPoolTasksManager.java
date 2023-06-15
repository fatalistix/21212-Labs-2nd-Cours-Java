package ru.nsu.balashov.torrent;

import com.google.common.base.Splitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.codec.digest.DigestUtils;
import ru.nsu.balashov.torrent.utils.TorrentConnectionStreamsUtils;
import ru.nsu.balashov.torrent.utils.TorrentConnectionStreamsUtils.Client.AvailablePiecesRequestInfo;
import ru.nsu.balashov.torrent.SavedFilesManager.UploadData;
import ru.nsu.balashov.torrent.utils.TorrentConnectionStreamsUtils.MessageType;
import ru.nsu.balashov.torrent.utils.TorrentConnectionStreamsUtils.Server.PieceRequestInfo;


public class ThreadPoolTasksManager {
//    @FunctionalInterface
//    private static interface DownloadAndWritePartsInRange {
//        public void run(TorrentFileData torrentFileData, RandomAccessFile file, int start, int end, InputStream in);
//    }
//    private static class UpAndDown implements Runnable {
//        @Override
//        public void run() {
//        }
//    }
    private static record IpPortDesc(String ip, int port, int descriptor) {}

    private final ConcurrentHashMap<byte[], HashMap<Integer, ArrayList<Socket>>> peersData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<byte[], boolean[]> submittedToDownload = new ConcurrentHashMap<>();



    private final ConcurrentHashMap<byte[], ConcurrentHashMap<Integer, ArrayList<IpPortDesc>>> peersIpData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<byte[], Integer> infoSumToDescriptor = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, UploadData> cachedInfo = new ConcurrentHashMap<>();



    private final ExecutorService downloadExecutorService = Executors.newCachedThreadPool();
    private final ExecutorService uploadExecutorService   = Executors.newCachedThreadPool();
    public ThreadPoolTasksManager() {}





    private void collectData(TorrentFileData torrentFileData, ArrayList<String> ipPortList) {
//        peersIpData.put(torrentFileData.getInfoSha1Sum(), new ConcurrentHashMap<>());
//        Queue<Future<?>> checkQueue = new LinkedList<>();
//        for (String ipPort : ipPortList) {
//            checkQueue.add(downloadExecutorService.submit(() -> {
//                System.out.println("Starting new try with: " + ipPort);
//                List<String> ipPortSplit = Splitter.on(':').splitToList(ipPort);
//                String ip = ipPortSplit.get(0);
//                int port  = Integer.parseInt(ipPortSplit.get(1));
//                System.out.println(ip + ":" + port);
//                try (Socket socket = new Socket(ip, port)) {
//                    System.out.println("CONNECTED");
//                    InputStream  in  = socket.getInputStream();
//                    OutputStream out = socket.getOutputStream();
//                    TorrentConnectionStreamsUtils.Client.sendRequestForAvailablePieces(out, torrentFileData.getInfoSha1Sum());
//                    AvailablePiecesRequestInfo availablePiecesRequestInfo = TorrentConnectionStreamsUtils.Client.getAvailablePieces(in);
//
//                    for (Integer pieceIndex : availablePiecesRequestInfo.availablePieces()) {
//                        peersIpData.get(torrentFileData.getInfoSha1Sum())
//                                .computeIfAbsent(pieceIndex, k -> new ArrayList<>())
//                                .add(new IpPortDesc(ip, port, availablePiecesRequestInfo.descriptor()));
//                    }
//                    System.out.println("working with: " + ipPort);
//                    socket.close();
//                    System.out.println("SOCKET CLOSED");
//                } catch (UnknownHostException e) {
//                    //? do nothing, just not opens socket and does not add anything
//                    System.out.println("UNKNOWN HOST EXCEPTION");
//                } catch (IOException e) {
//                    //? In this case nothing just will be saved
//                    System.out.println("IOEXCEPTION");
//                    System.out.println(e.getMessage());
//                    e.printStackTrace();
//                }
//                System.out.println("END");
//            }));
//        }
//        System.out.println("BEFORE WAITING");
//        while (!checkQueue.isEmpty()) {
//            try {
//                System.out.println(checkQueue.size());
//                checkQueue.poll().get();
//            } catch (InterruptedException | ExecutionException e) {
//                System.out.println(e.getMessage());
//                e.printStackTrace();
//                System.exit(0);
//            }
//        }
//    }
//
//    public void submitDownload(TorrentFileData torrentFileData, RandomAccessFile file, ArrayList<String> ipPortList) {
//        collectData(torrentFileData, ipPortList);
//        Queue<Future<?>> futureQueue = new LinkedList<>();
//        for (int i = 0; i < torrentFileData.getSha1Sums().length; ++i) {
//            int pieceIndex = i;
//            futureQueue.add(downloadExecutorService.submit(() -> {
//                int threadIndex = (int) Thread.currentThread().threadId() %
//                        peersIpData.get(torrentFileData.getInfoSha1Sum()).size();
//                for (int j = 0; j < peersIpData.get(torrentFileData.getInfoSha1Sum()).get(pieceIndex).size(); ++j) {
//                    IpPortDesc connectionData = peersIpData.get(torrentFileData.getInfoSha1Sum()).get(pieceIndex)
//                            .get((j + threadIndex) % peersData.get(torrentFileData.getInfoSha1Sum()).size());
//                    System.out.println("HERERERER");
//                    try (Socket socket = new Socket(connectionData.ip(), connectionData.port())) {
//                        System.out.println("CONNECTED AGAIN");
//                        InputStream  in  = socket.getInputStream();
//                        OutputStream out = socket.getOutputStream();
//                        TorrentConnectionStreamsUtils.Client.sendRequestForPiece(out, connectionData.descriptor, pieceIndex);
//                        byte[] piece = TorrentConnectionStreamsUtils.Client.getPiece(in);
//                        if (Arrays.equals(DigestUtils.sha1(piece), torrentFileData.getSha1ByIndex(pieceIndex))) {
//                            synchronized (file) {
//                                file.seek(torrentFileData.getPieceLength() * pieceIndex);
//                                file.write(piece);
//                            }
//                        }
//                    } catch (IOException e) {
//                        System.out.println("EXCEPTION AT SUBMIT DOWNLOAD");
//                    }
//                }
//            }));
//        }
//        downloadExecutorService.submit(() -> {
//            while (!futureQueue.isEmpty()) {
//                try {
//                    futureQueue.poll().get();
//                } catch (InterruptedException | ExecutionException ignored) {
//                }
//            }
//            try {
//                file.close();
//                System.out.println("file closed");
//            } catch (IOException ignored) {
//
//            }
//
//        });
//    }
//
//
//    public void submitUpload(Socket socket, SavedFilesManager savedFilesManager) {
//        uploadExecutorService.submit(() -> {
//            try {
//                InputStream  in  = socket.getInputStream();
//                OutputStream out = socket.getOutputStream();
//                while (true) {
//                    MessageType type = TorrentConnectionStreamsUtils.Server.readMessageType(in);
//                    switch (type) {
//                        case AVAILABLE_REQUEST -> {
//                            System.out.println("ANSWERED AVAILABLE REQUEST");
//                            byte[] requestSha1Sum = TorrentConnectionStreamsUtils.Server.getRequestForAvailablePieces(in);
//                            UploadData uploadData = savedFilesManager.getUploadData(requestSha1Sum);
//                            if (uploadData == null) {
//                                System.out.println("HERE");
//                                break;
//                            }
//                            int descriptor = -1;
//                            if (infoSumToDescriptor.containsKey(requestSha1Sum)) {
//                                descriptor = infoSumToDescriptor.get(requestSha1Sum);
//                            } else {
//                                for (int i = 0; i < Integer.MAX_VALUE; ++i) {
//                                    if (!cachedInfo.containsKey(i)) {
//                                        descriptor = i;
//                                        infoSumToDescriptor.put(requestSha1Sum, i);
//                                        cachedInfo.put(i, uploadData);
//                                    }
//                                }
//                            }
//                            if (descriptor == -1) {
//                                throw new IllegalArgumentException("too much files");
//                            }
//                            TorrentConnectionStreamsUtils.Server.sendAvailablePieces(out, uploadData.availablePieces(), descriptor);
//                        }
//                        case PIECE_REQUEST -> {
//                            System.out.println("ANSWERED PIECE REQUEST");
//                            PieceRequestInfo requestInfo = TorrentConnectionStreamsUtils.Server.getRequestForPiece(in);
//                            UploadData uploadData = cachedInfo.get(requestInfo.descriptor());
//                            if (uploadData.availablePieces().contains(requestInfo.pieceIndex())) {
//                                try (RandomAccessFile randomAccessFile = new RandomAccessFile(uploadData.pathToDownloaded(), "rw")) {
//                                    randomAccessFile.seek(uploadData.pieceLength() * requestInfo.pieceIndex());
//                                    byte[] piece = new byte[(int) uploadData.pieceLength()];
//                                    System.out.println(piece.length);
//                                    randomAccessFile.read(piece);
//                                    TorrentConnectionStreamsUtils.Server.sendPiece(out, piece);
//                                }
//                            } else {
//                                TorrentConnectionStreamsUtils.Server.sendPiece(out, new byte[0]);
//                            }
//                        }
//                        case DISCONNECT -> {
//                            throw new IllegalArgumentException("Disconnect request");
//                        }
//                        case UNKNOWN_REQUEST -> {
//
//                        }
//                    }
//                }
//            } catch (IOException | IllegalArgumentException ignored) {
//
//            }
//            try {
//                socket.close();
//            } catch (IOException e) {
//                //? Do nothing
//            }
//        });
    }


//    public void submitDownload(TorrentFileData torrentFileData, RandomAccessFile file) {
//        for (int i = 0; i < torrentFileData.getSha1Sums().length; ++i) {
//            int pieceIndex = i;
//            Runnable task = () -> {
//                int threadIndex = (int) Thread.currentThread().threadId() %
//                        peersData.get(torrentFileData.getInfoSha1Sum()).size();
//                for (int j = 0; j < peersData.get(torrentFileData.getInfoSha1Sum()).get(pieceIndex).size(); ++j) {
//                    Socket socket = peersData.get(torrentFileData.getInfoSha1Sum()).get(pieceIndex)
//                            .get((j + threadIndex) % peersData.get(torrentFileData.getInfoSha1Sum()).size());
//                    try {
//                        InputStream  in  = socket.getInputStream();
//                        OutputStream out = socket.getOutputStream();
//                        TorrentConnectionStreamsUtils.sendRequestForPiece(out, pieceIndex);
//                        byte[] piece = TorrentConnectionStreamsUtils.getPiece(in);
//                        if (Arrays.equals(DigestUtils.sha1(piece), torrentFileData.getSha1ByIndex(pieceIndex))) {
//                            synchronized (file) {
//                                file.seek(pieceIndex * torrentFileData.getPieceLength());
//                                file.write(piece);
//                            }
//                            return;
//                        }
//                    } catch (IOException e) {
//                        //TODO: ADD SELF PUSHING TO QUEUE WITH SAME INDEX + REMOVE AND CLOSE SOCKET.
//                    }
//                }
//            };
//            downloadExecutorService.submit(task);
//        }
//    }
//
//    public void openPeersAndSort(ArrayList<String> ipWithPorts, TorrentFileData torrentData) {
//        submittedToDownload.put(torrentData.getInfoSha1Sum(), new boolean[torrentData.getSha1Sums().length]);
//        peersData.put(torrentData.getInfoSha1Sum(), new HashMap<>());
//        for (String str : ipWithPorts) {
//            downloadExecutorService.submit(() -> {
//                String ip = Splitter.on(':').splitToList(str).get(0);
//                int port  = Integer.parseInt(Splitter.on(':').splitToList(str).get(1));
//                try (Socket socket = new Socket(ip, port)) {
//                    InputStream  in  = socket.getInputStream();
//                    OutputStream out = socket.getOutputStream();
//                    TorrentConnectionStreamsUtils.sendInfoShaKey(out, torrentData.getInfoSha1Sum());
//                    ArrayList<Integer> availablePieces = TorrentConnectionStreamsUtils.getAvailablePieces(in);
//                    if (availablePieces != null) {
//                        for (Integer pieceIndex : availablePieces) {
//                            peersData.get(torrentData.getInfoSha1Sum())
//                                    .computeIfAbsent(pieceIndex, k -> new ArrayList<>()).add(socket);
//                        }
//                    }
//
//                } catch (IOException e) {
//                    //? Do nothing
//                }
//            });
//        }
//    }
//
//    public void submitUploading(Socket socket, SavedFilesManager savedFilesManager) {
//        uploadExecutorService.submit(() -> {
//            try {
//                InputStream  in  = socket.getInputStream();
//                OutputStream out = socket.getOutputStream();
//                UploadData upData = savedFilesManager.getUploadData(TorrentConnectionStreamsUtils.getShaDownloadKey(in));
//                if (upData == null) {
//                    //TODO: HANDLE null
//                    return;
//                }
//                TorrentConnectionStreamsUtils.sendAvailablePieces(out, upData.availablePieces());
//
//            } catch (IOException e) {
//                //TODO: HANDLE EXCEPTION
//                throw new RuntimeException(e);
//            }
//        });
//    }
}
