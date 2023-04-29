package ru.nsu.balashov.mousetrapgame;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.*;

public class LevelsData {
    private static volatile LevelFactory instance;

    private LevelsData() {}

    /**
     * @return null, when Database cannot be loaded due to bad levels directory location
     */
    public static LevelFactory getInstance() {
        if (instance == null) {
            try {
                instance = new LevelFactory();
            } catch (NotDirectoryException e) {
                instance = null;
            }
        }
        return instance;
    }


    public static class LevelFactory {
        private final Map<String, File> nameToLevel = new HashMap<>();
        private final String pathToLevelsDir = "ru/nsu/balashov/mousetrapgame/Levels";
        private final ArrayList<String> levelsNames = new ArrayList<>();

        private File selectedForLoading;

        public ArrayList<String> getLevelsNames() {
            return levelsNames;
        }

        private void searchForLevels(final File dir) {
            for (File f : Objects.requireNonNull(dir.listFiles())) {
                if (f.isDirectory()) {
                    searchForLevels(f);
                } else {
                    if (f.canRead()) {
                        try (Scanner sc = new Scanner(f)) {
                            String name = sc.nextLine();
                            if (!nameToLevel.containsKey(name)) {
                                nameToLevel.put(name, f);
                                levelsNames.add(name);
                            }
                        } catch (FileNotFoundException e) {
                            //? impossible clause
                            // throw new RuntimeException("IMPOSSIBLE CLAUSE in searchForLevels func");
                        } catch (NoSuchElementException e) {
                            //? No name of that file, illegal format, skipped
                        }
                    }
                }
            }
        }
        private LevelFactory() throws NotDirectoryException {
            File levelsDir = new File(System.getProperty("java.class.path") + "/" + pathToLevelsDir);
            if (!levelsDir.exists() || !levelsDir.isDirectory()) {
                throw new NotDirectoryException("Cannot find directory with Levels");
            }
            searchForLevels(levelsDir);
        }

        public File getLevelFileByName(String name) {
            return nameToLevel.get(name);
        }

        public void selectForLoading(String name) {
            selectedForLoading = nameToLevel.get(name);
        }

        public File getSelectedForLoading() {
            return selectedForLoading;
        }
    }
}
