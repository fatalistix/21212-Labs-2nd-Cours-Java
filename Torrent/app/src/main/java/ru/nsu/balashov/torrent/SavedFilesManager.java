package ru.nsu.balashov.torrent;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;



public class SavedFilesManager {
    private record RegisteredData(String pathToDownloaded, String[] sha1Sums, long singleFileLength, String name,
                                  long pieceLength, String infoSha1Sum) {}
    public record UploadData(String pathToDownloaded, byte[][] sha1sums, ArrayList<Integer> availablePieces, long pieceLength) {}
    private final static String pathToData = "ru/nsu/balashov/torrent/DownloadedTorrentsData.json";
    private final Gson gson = new Gson();
    private final HashMap<byte[], RegisteredData> downloaded = new HashMap<>();
    private final HashMap<byte[], ArrayList<Integer>> validatedParts = new HashMap<>();
//    private final File downloadedDataJson;


    public SavedFilesManager() throws IOException {
//        downloadedDataJson = new File(Splitter.on(':').splitToList(System.getProperty("java.class.path")).get(0) + '/' + pathToData);
//        if (!downloadedDataJson.exists()) {
//            if (downloadedDataJson.createNewFile()) {
//                return;
//            }
//        }
//        try (FileReader jsonReader = new FileReader(downloadedDataJson, StandardCharsets.UTF_8)) {
//            JsonArray mainArray = gson.fromJson(jsonReader, JsonArray.class);
//            if (mainArray != null) {
//                for (JsonElement dataJe : mainArray) {
//                    RegisteredData regData = gson.fromJson(dataJe, RegisteredData.class);
//                    ArrayList<Integer> parts = getValidatedParts(regData);
//                    if (parts != null) {
//                        validatedParts.put(regData.infoSha1Sum(), parts);
//                        downloaded.put(regData.infoSha1Sum(), regData);
//                    }
//                }
//            }
//        }
    }


    private ArrayList<Integer> getValidatedParts(RegisteredData regData) {
//        File downloadedInstanceFile = new File(regData.pathToDownloaded());
//        if (!downloadedInstanceFile.exists()) {
//            return null;
//        }
//        ArrayList<Integer> forRet = new ArrayList<>();
//        try (FileInputStream fileIn = new FileInputStream(downloadedInstanceFile)) {
//            for (int i = 0; i < regData.singleFileLength() / regData.pieceLength() +
//                    ((regData.singleFileLength() % regData.pieceLength() == 0) ? 0 : 1); ++i) {
//                byte[] piece = fileIn.readNBytes((int) regData.pieceLength);
//                if (Arrays.equals(DigestUtils.sha1(piece), regData.sha1Sums()[i])) {
//                    forRet.add(i);
//                }
//            }
//        } catch (IOException e) {
//            return null;
//        }
//        return forRet;
        return null;
    }

    private boolean validateData(RegisteredData regData) {
//        if (regData == null) {
//            return false;
//        }
//        File downloadedInstance = new File(regData.pathToDownloaded());
//        if (!downloadedInstance.exists()) {
//            return false;
//        }
//        try (InputStream downloadedInStream = new FileInputStream(downloadedInstance)) {
//            for (int i = 0; i < regData.singleFileLength() / regData.pieceLength() +
//                    ((regData.singleFileLength() % regData.pieceLength() == 0) ? 0 : 1); ++i) {
//                byte[] piece = downloadedInStream.readNBytes((int) regData.pieceLength());
//                if (!Arrays.equals(DigestUtils.sha1(piece), regData.sha1Sums[i])) {
//                    System.out.printf("FAILED ON i = %d%n", i);
//                    return false;
//                }
//            }
//        } catch (IOException e) {
//            return false;
//        }
        return true;
    }

//    private boolean validateDataParts(RegisteredData regData) {
//        if (regData == null) {
//            return false;
//        }
//        File downloadedInstanceFile = new File(regData.pathToDownloaded());
//        if (!downloadedInstanceFile.exists()) {
//            return false;
//        }
//        try (RandomAccessFile downloadedInstanceRAF = new RandomAccessFile(downloadedInstanceFile, "r")) {
//            byte[] piece = new byte[20];
//            int readBytes;
//            for (int index : regData.downloadedParts()) {
//                downloadedInstanceRAF.seek(index * regData.pieceLength());
//                readBytes = downloadedInstanceRAF.read(piece);
//                if (readBytes == 20) {
//                    if (!Arrays.equals(DigestUtils.sha1(piece), regData.sha1Sums()[index])) {
//                        return false;
//                    }
//                } else {
//                    if (!Arrays.equals(DigestUtils.sha1(Arrays.copyOfRange(piece, 0, readBytes)),
//                            regData.sha1Sums()[index])) {
//                        return false;
//                    }
//                }
//            }
//        } catch (IOException e) {
//            return false;
//        }
//        return true;
//    }
//

    public void registerDownloaded(TorrentFileData decodedData, String pathToDownloaded)
            throws IllegalArgumentException {
//        if (downloaded.containsKey(decodedData.getInfoSha1Sum())) {
//            throw new IllegalArgumentException("Name exists");
//        }
//        downloaded.put(decodedData.getInfoSha1Sum(), new RegisteredData(pathToDownloaded, decodedData.getSha1Sums(),
//                decodedData.getSingleFileLength(), decodedData.getTorrentName(), decodedData.getPieceLength(),
//                decodedData.getInfoSha1Sum()));
    }

    public void storeDownloaded() throws IOException {
//        try (JsonWriter jsonWriter = gson.newJsonWriter(new FileWriter(downloadedDataJson))) {
//            JsonArray storeArray = new JsonArray();
//            for (byte[] key : downloaded.keySet()) {
//                RegisteredData registeredData = downloaded.get(key);
//                JsonObject jo = new JsonObject();
//                JsonArray sumsJa = new JsonArray(registeredData.sha1Sums().length);
//
//                for (byte[] sum : registeredData.sha1Sums()) {
//                    JsonArray oneSumJa = new JsonArray(20);
//                    for (byte b : sum) {
//                        oneSumJa.add(b);
//                    }
//                    sumsJa.add(oneSumJa);
//                }
//
//                JsonArray compareSumJa = new JsonArray(registeredData.infoSha1Sum().length);
//                for (byte b : registeredData.infoSha1Sum()) {
//                    compareSumJa.add(b);
//                }
//
//
//                jo.addProperty("pathToDownloaded",  registeredData.pathToDownloaded());
//                jo.add("sha1Sums", sumsJa);
//                jo.addProperty("singleFileLength",  registeredData.singleFileLength());
//                jo.addProperty("name",              registeredData.name());
//                jo.addProperty("pieceLength",       registeredData.pieceLength());
//                jo.add("infoSha1Sum", compareSumJa);
//
//                storeArray.add(jo);
//            }
//            gson.toJson(storeArray, jsonWriter);
//        }
    }

    public boolean exists(TorrentFileData torrentData) {
        return downloaded.containsKey(torrentData.getInfoHash());
    }

    public ArrayList<Integer> getAvailableParts(TorrentFileData torrentData) {
        if (downloaded.get(torrentData.getInfoHash()) == null) {
            return null;
        }
        return validatedParts.get(torrentData.getInfoHash());
    }



    public UploadData getUploadData(byte[] infoSha1Sum) {
//        if (downloaded.get(infoSha1Sum) == null) {
//            return null;
//        }
//        return new UploadData(downloaded.get(infoSha1Sum).pathToDownloaded, downloaded.get(infoSha1Sum).sha1Sums(),
//                validatedParts.get(infoSha1Sum), downloaded.get(infoSha1Sum).pieceLength);
        return null;
    }
}
