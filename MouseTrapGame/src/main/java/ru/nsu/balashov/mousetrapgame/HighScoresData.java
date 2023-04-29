package ru.nsu.balashov.mousetrapgame;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.bag.TreeBag;
import org.apache.commons.collections4.multiset.HashMultiSet;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class HighScoresData {
    private static volatile HighScoresFactory instance;

    private HighScoresData() {}

    /**
     * @return null, when Database cannot be loaded due to bad levels directory location
     */
    public static HighScoresFactory getInstance() {
        if (instance == null) {
            try {
                instance = new HighScoresFactory();
            } catch (IOException e) {
                return null;
            }
        }
        return instance;
    }



    public static class HighScoresFactory {
        public static record ScoreData(Integer seconds, String author) implements Comparable<ScoreData> {
            @Override
            public int compareTo(ScoreData o) {
                return this.seconds - o.seconds;
            }
        }
        private final String pathToHighScoresFile = "ru/nsu/balashov/mousetrapgame/HighScores/HighScores.json";
        private final HashMap<String, TreeBag<ScoreData>> scores = new HashMap<>();
        private final File highScoresJson;
        private final Gson gson = new Gson();

        private HighScoresFactory() throws IOException{
            highScoresJson = new File(System.getProperty("java.class.path") + "/" + pathToHighScoresFile);
            if (!highScoresJson.exists()) {
                if (highScoresJson.createNewFile()) {
                    return;
                } else {
                    throw new IOException("Cannot create scores file");
                }
            }
            try (FileReader jsonReader = new FileReader(highScoresJson, StandardCharsets.UTF_8)) {
                JsonArray levelsDataJa = gson.fromJson(jsonReader, JsonArray.class);
                if (levelsDataJa != null) {
                    for (JsonElement levelsDataJe : levelsDataJa) {
                        TreeBag<ScoreData> levelScores = new TreeBag<>();

                        JsonObject levelsDataJo = levelsDataJe.getAsJsonObject();
                        JsonArray levelScoresJa = levelsDataJo.get("scores").getAsJsonArray();
                        for (JsonElement levelScoresJe : levelScoresJa) {
                            levelScores.add(gson.fromJson(levelScoresJe, ScoreData.class));
                        }

                        scores.put(levelsDataJo.get("name").getAsString(), levelScores);
                    }
                }
            }
        }

        public void saveScore(String levelName, String scoreAuthor, int seconds) {
            if (!scores.containsKey(levelName)) {
                scores.put(levelName, new TreeBag<>());
            }
            scores.get(levelName).add(new ScoreData(seconds, scoreAuthor));
        }

        public boolean storeScores() {
            try (FileWriter jsonWriter = new FileWriter(highScoresJson)) {
                JsonArray allLevelsScoresToStoreJa = new JsonArray();
                for (String name : scores.keySet()) {
                    JsonArray levelScoresJa = new JsonArray();
                    for (ScoreData sd : scores.get(name)) {
                        JsonObject jo = new JsonObject();
                        jo.addProperty("seconds", sd.seconds);
                        jo.addProperty("author", sd.author);
                        levelScoresJa.add(jo);
                    }

                    JsonObject levelScoresJo = new JsonObject();

                    levelScoresJo.addProperty("name", name);
                    levelScoresJo.add("scores", levelScoresJa);
                    allLevelsScoresToStoreJa.add(levelScoresJo);
                }
                gson.toJson(allLevelsScoresToStoreJa, jsonWriter);
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        public ScoreData getBestLevelScore(String levelName) {
            if (scores.containsKey(levelName)) {
                var it = scores.get(levelName).iterator();
                if (it.hasNext()) {
                    return it.next();
                }
            }
            return null;
        }

        public ArrayList<ScoreData> getAllLevelScores(String levelName) {
            if (scores.containsKey(levelName)) {
                return new ArrayList<>(scores.get(levelName));
            }
            return null;
        }
    }
}
