package ru.nsu.balashov.torrent;

import com.google.common.base.Splitter;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class App {
    private static class StopException extends Exception {}

//    private static boolean renameTorrent(Scanner scanner, PrintStream output, TorrentFileData torrentFileData) {
//        output.println("E==> This torrent already exists");
//        output.print  ("?==> Would you like to save it with another name? [y/n]: ");
//        String answer = scanner.nextLine();
//        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
//            while (true) {
//                output.print(" ==> Enter new name: ");
//                String newName = scanner.nextLine();
//                if (!newName.equals(torrentFileData.getTorrentName())) {
//                    torrentFileData.setNewTorrentName(newName);
//                    return true;
//                }
//                output.println("E==> Already exists");
//            }
//        }
//        return false;
//    }


    private static void start(InputStream input, PrintStream output, String[] args) {
        TorrentCore core;

        try {
            core = new TorrentCore();
        } catch (IOException e) {
            output.println("Error reading from config.json file. Downloads data corrupted");
            return;
        }

        try (Scanner scanner = new Scanner(input)) {
            while (true) {
                output.print("~~> ");
                switch (scanner.nextLine()) {
                    case "download" -> {
                        output.print  (" ==> Enter path to torrent file: ");
                        String torrentFilepath  = scanner.nextLine();
                        output.print  (" ==> Enter path to download folder: ");
                        String downloadFilepath = scanner.nextLine();
                        output.println(" ==> Enter list of ip with format 'ip:port', separated via space:");
                        output.print  (" ==> ");
                        ArrayList<String> ipList = new ArrayList<>(Splitter.on(' ')
                                .splitToList(scanner.nextLine()));
                        TorrentFileData torrentFileData = new TorrentFileData();
                        try {
                            torrentFileData.decode(torrentFilepath);
                            core.downloadTorrent(torrentFileData, downloadFilepath, ipList);
                            output.println(" ==> Downloading started");
                        } catch (TorrentCore.RecordExistsException e) {
//                            if (renameTorrent(scanner, output, torrentFileData)) {
//                                try {
//                                    core.downloadTorrent(torrentFileData, downloadFilepath, ipList);
//                                    output.println(" ==> Downloading started");
//                                } catch (Exception newExc) {
//                                    output.println("E==> Cannot start downloading, please try again\nError message: "
//                                            + newExc.getMessage());
//                                }
//                            }
                            output.println("E==> Torrent already exists");
                        } catch (FileNotFoundException | SecurityException e) {
                            output.println("E==> Cannot download to that directory");
                        } catch (IOException e) {
                            output.println("E==> Torrent file damaged");
                        }
                    }
                    case "upload" -> {
                        core.uploadTorrents();
                        output.println(" ==> Started uploading torrents");
                    }
                    case "add" -> {
                        output.print(" ==> Enter path to torrent file: ");
                        String torrentFilePath = scanner.nextLine();
                        output.print(" ==> Enter path to downloaded file: ");
                        String instanceFilePath = scanner.nextLine();
                        TorrentFileData torrentFileData = new TorrentFileData();
                        try {
                            torrentFileData.decode(torrentFilePath);
                            core.addDownloadedTorrent(torrentFileData, instanceFilePath);
                            output.println(" ==> Added successfully");
                        } catch (IOException e) {
                            output.println("E==> Torrent file damaged");
                        } catch (TorrentCore.RecordExistsException e) {
                            if (renameTorrent(scanner, output, torrentFileData)) {
                                try {
                                    core.addDownloadedTorrent(torrentFileData, instanceFilePath);
                                    output.println(" ==> Added successfully");
                                } catch (Exception newExc) {
                                    output.println("E==> Cannot add instance, please try again\nError message: "
                                            + newExc.getMessage());
                                }
                            }
                        }

                    }
                    case "exit" -> {
                        throw new StopException();
                    }
                    case "DEBUG" -> {

                    }
                }
            }
        } catch (StopException e) {
            output.println(" ==> EXITING...");
        } catch (Exception e) {
            output.println("E==> Got unexpected exception: " + e.getMessage());
        }
    }


    public static void test() {
        TorrentFileData torrentFileData = new TorrentFileData();
        try {
            torrentFileData.decode("/home/vyacheslav/Documents/Programming/OOOP/21212/21212-Labs-2nd-Cours-Java/Torrent/app/src/main/resources/ru/nsu/balashov/torrent/TorrentFiles/EndeavourOS_Cassini_Nova-03-2023_R1.iso.torrent");
        } catch (IOException e) {
            System.out.println("GOT EXCEPTION: " + e.getMessage());
        }

    }

    public static void main(String[] args) {
//        start(System.in, System.out, args);
        test();
    }
}



//    public static record Record(String[] array, long size) {}

//    private static void start(InputStream in, OutputStream out, String[] args) {
//        Gson gson = new Gson();
//        Writer writer = new StringWriter();
//        Record first  = new Record(new String[]{"123", "456"}, 2);
//        Record second = new Record(new String[]{"890", "135", "468"}, 3);
//        gson.toJson(first,  writer);
//        gson.toJson(second, writer);
//        System.out.println(writer);
//    }






//    private static void start(InputStream in, OutputStream out, String[] args) throws IOException {
//        File torrent = new File("/home/vyacheslav/Documents/Programming/OOOP/21212/21212-Labs-2nd-Cours-Java/Torrent/app/src/main/resources/ru/nsu/balashov/torrent/TorrentFiles/EndeavourOS_Cassini_Nova-03-2023_R1.iso.torrent");
//        InputStream torrentIs = new FileInputStream(torrent);
//        TorrentFileData data = new TorrentFileData(torrentIs);
//        if (args[0].equals("get")) {
//            File toDownloadFile = new File("/home/vyacheslav/Documents/Programming/OOOP/21212/21212-Labs-2nd-Cours-Java/Torrent/app/src/main/resources/ru/nsu/balashov/torrent/Downloaded/Test");
//            if (toDownloadFile.exists()) {
//                toDownloadFile.delete();
//            }
//            toDownloadFile.createNewFile();
//            OutputStream os = new FileOutputStream(toDownloadFile);
//            Socket socket = new Socket("localhost", 6969);
//            InputStream socketIs = socket.getInputStream();
////            int size = 0;
////            while (size < data.getPieceLength()) {
////                os.write(socketIs.read());
////                size++;
////            }
//            System.out.println(data.getPieceLength());
//            socketIs.transferTo(os);
//            os.close();
//            InputStream is = new FileInputStream(toDownloadFile);
//            System.out.println(new String(DigestUtils.sha1(is.readNBytes((int) data.getPieceLength())), StandardCharsets.UTF_8).equals(data.getSha1ByIndex(0)));
//        } else {
//            ServerSocket serverSocket = new ServerSocket(6969);
//            Socket socket = serverSocket.accept();
//            File downloaded = new File("/home/vyacheslav/Documents/Programming/OOOP/21212/21212-Labs-2nd-Cours-Java/Torrent/app/src/main/resources/ru/nsu/balashov/torrent/EndeavourOS_Cassini_Nova-03-2023_R1.iso");
//            InputStream downloadedIs = new FileInputStream(downloaded);
//            OutputStream os = socket.getOutputStream();
//            os.write(downloadedIs.readNBytes((int) data.getPieceLength()));
//        }
//    }
//
//    public static void test() throws IOException {
//        System.out.write(System.in.readNBytes(10));
//    }




//    private static Runnable parametrizedCallable(final int i) {
//        return () -> {
//            System.out.printf("I am %d with i = %d%n", Thread.currentThread().threadId(), i);
//            int time = 0;
//            while (time++ < 5000) {
//                ArrayList<Integer> list = new ArrayList<>();
//                for (int j = 0; j < time; ++j) {
//                    list.add(j);
//                }
//                int res = 0;
//                for (int j = 0; j < time; ++j) {
//                    for (int k = 0; k < time; ++k) {
//                        res += list.get(j) * list.get(k);
//                    }
//                }
//            }
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        };
//    }



// BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
//        for (int i = 0; i < 1_000_000; ++i) {
//            tasks.add(parametrizedCallable(i));
//        }
//        try (ExecutorService executorService = Executors.newFixedThreadPool(4)) {
//            while (!tasks.isEmpty()) {
//                executorService.submit(tasks.poll());
//            }
//        }




//        String dataPath = "/home/vyacheslav/Documents/Programming/OOOP/21212/21212-Labs-2nd-Cours-Java/Torrent/app/src/main/resources/ru/nsu/balashov/torrent/EndeavourOS_Cassini_Nova-03-2023_R1.iso";
//        String torrentPath = "/home/vyacheslav/Documents/Programming/OOOP/21212/21212-Labs-2nd-Cours-Java/Torrent/app/src/main/resources/ru/nsu/balashov/torrent/TorrentFiles/EndeavourOS_Cassini_Nova-03-2023_R1.iso.torrent";
//        File torrentFile = new File(torrentPath);
//        FileInputStream torrentIn = new FileInputStream(torrentFile);
//        TorrentFileData tfd = new TorrentFileData(torrentIn);
//        SavedFilesManager savedFilesManager = new SavedFilesManager();
//        System.out.println(1);
//        try {
//            savedFilesManager.registerDownloaded(dataPath, tfd);
//            savedFilesManager.storeDownloaded();
//        } catch (IllegalArgumentException e) {
//            //? Nothing
//        }