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
    private record RegisteredData(String pathToDownloaded, byte[][] sha1sums, long singleFileLength, String name,
                                  long pieceLength, int[] downloadedParts, byte[] compareSum) {}
    private final static String pathToData = "ru/nsu/balashov/torrent/DownloadedTorrentsData.json";
    private final Gson gson = new Gson();
    private final HashMap<String, RegisteredData> downloaded = new HashMap<>();
    private final File downloadedDataJson;


    private boolean validateData(RegisteredData regData) {
        if (regData == null) {
            return false;
        }
        File downloadedInstance = new File(regData.pathToDownloaded());
        if (!downloadedInstance.exists()) {
            return false;
        }
        try (InputStream downloadedInStream = new FileInputStream(downloadedInstance)) {
            for (int i = 0; i < regData.singleFileLength() / regData.pieceLength() +
                    ((regData.singleFileLength() % regData.pieceLength() == 0) ? 0 : 1); ++i) {
                byte[] piece = downloadedInStream.readNBytes((int) regData.pieceLength());
                if (!Arrays.equals(DigestUtils.sha1(piece), regData.sha1sums[i])) {
                    System.out.printf("FAILED ON i = %d%n", i);
                    return false;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean validateDataParts(RegisteredData regData) {
        if (regData == null) {
            return false;
        }
        File downloadedInstanceFile = new File(regData.pathToDownloaded());
        if (!downloadedInstanceFile.exists()) {
            return false;
        }
        try (RandomAccessFile downloadedInstanceRAF = new RandomAccessFile(downloadedInstanceFile, "r")) {
            byte[] piece = new byte[20];
            int readBytes;
            for (int index : regData.downloadedParts()) {
                downloadedInstanceRAF.seek(index * regData.pieceLength());
                readBytes = downloadedInstanceRAF.read(piece);
                if (readBytes == 20) {
                    if (!Arrays.equals(DigestUtils.sha1(piece), regData.sha1sums()[index])) {
                        return false;
                    }
                } else {
                    if (!Arrays.equals(DigestUtils.sha1(Arrays.copyOfRange(piece, 0, readBytes)),
                            regData.sha1sums()[index])) {
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }


    public SavedFilesManager() throws IOException {
        downloadedDataJson = new File(Splitter.on(':').splitToList(System.getProperty("java.class.path")).get(0) + '/' + pathToData);
        if (!downloadedDataJson.exists()) {
            if (downloadedDataJson.createNewFile()) {
                return;
            }
        }
        try (FileReader jsonReader = new FileReader(downloadedDataJson, StandardCharsets.UTF_8)) {
            JsonArray mainArray = gson.fromJson(jsonReader, JsonArray.class);
            if (mainArray != null) {
                for (JsonElement dataJe : mainArray) {
                    RegisteredData regData = gson.fromJson(dataJe, RegisteredData.class);
                    if (validateDataParts(regData)) {
                        downloaded.put(regData.name(), regData);
                    }
                }
            }
        }
    }

    public void registerDownloaded(String pathToDownloaded, TorrentFileData decodedData, int[] downloadedIndexes)
            throws IllegalArgumentException {
        if (downloaded.containsKey(decodedData.getTorrentName())) {
            throw new IllegalArgumentException("Name exists");
        }
        ArrayList<Byte> concatenatedSums = new ArrayList<>(20 * decodedData.getSha1Sums().length);
        for (byte[] array : decodedData.getSha1Sums()) {
            for (byte b : array) {
                concatenatedSums.add(b);
            }
        }
        byte[] compareSum = DigestUtils.sha1(decodedData.getTorrentName() + decodedData.getPieceLength() +
                decodedData.getSingleFileLength() + concatenatedSums.stream().toString());
        downloaded.put(decodedData.getTorrentName(), new RegisteredData(pathToDownloaded, decodedData.getSha1Sums(),
                decodedData.getSingleFileLength(), decodedData.getTorrentName(), decodedData.getPieceLength(),
                downloadedIndexes, compareSum));
    }

    public void storeDownloaded() throws IOException {
        try (JsonWriter jsonWriter = gson.newJsonWriter(new FileWriter(downloadedDataJson))) {
            JsonArray storeArray = new JsonArray();
            for (String name : downloaded.keySet()) {
                RegisteredData registeredData = downloaded.get(name);
                JsonObject jo = new JsonObject();
                JsonArray sumsJa = new JsonArray(registeredData.sha1sums().length);

                for (byte[] sum : registeredData.sha1sums()) {
                    JsonArray oneSumJa = new JsonArray(20);
                    for (byte b : sum) {
                        oneSumJa.add(b);
                    }
                    sumsJa.add(oneSumJa);
                }

                JsonArray downloadedPartsJa = new JsonArray(registeredData.downloadedParts().length);
                for (int index : registeredData.downloadedParts()) {
                    downloadedPartsJa.add(index);
                }

                JsonArray compareSumJa = new JsonArray(registeredData.compareSum().length);
                for (byte b : registeredData.compareSum()) {
                    compareSumJa.add(b);
                }


                jo.add("downloadedParts", downloadedPartsJa);
                jo.add("sha1sums", sumsJa);
                jo.add("compareSum", compareSumJa);
                jo.addProperty("pathToDownloaded",  registeredData.pathToDownloaded());
                jo.addProperty("singleFileLength",  registeredData.singleFileLength());
                jo.addProperty("name",              registeredData.name());
                jo.addProperty("pieceLength",       registeredData.pieceLength());

                storeArray.add(jo);
            }
            gson.toJson(storeArray, jsonWriter);
        }
    }

    public boolean exists(TorrentFileData torrentData) {
        return downloaded.containsKey(torrentData.getTorrentName());
    }

    public int[] getAvailableParts(String name) {
        return downloaded.get(name).downloadedParts();
    }

//    public int[] check
}
