package ru.nsu.balashov.mousetrapgame;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.*;

public class ImagesData {
    private static volatile ImagesFactory instance;

    private ImagesData() {}

    /**
     * @return null, when Database cannot be loaded due to bad levels directory location
     */
    public static ImagesFactory getInstance() {
        if (instance == null) {
            try {
                instance = new ImagesFactory();
            } catch (NotDirectoryException e) {
                instance = null;
            }
        }
        return instance;
    }


    public static class ImagesFactory {
        private final Map<String, Image> nameToImage = new HashMap<>();
        private final String pathToImagesDir = "ru/nsu/balashov/mousetrapgame/Images";

        private void searchForImages(final File dir) {
            for (File f : Objects.requireNonNull(dir.listFiles())) {
                if (f.isDirectory()) {
                    searchForImages(f);
                } else {
                    if (f.canRead()) {
                        String s = f.getName();
                        int pos = s.lastIndexOf('.');
                        if (pos != -1) {
                            s = s.substring(0, pos);
                        }

                        try {
                            nameToImage.put(s, new Image(new FileInputStream(f)));
                        } catch (FileNotFoundException e) {
                            //? impossible clause
                        }
                    }
                }
            }
        }

        private ImagesFactory() throws NotDirectoryException {
            File imagesDir = new File(System.getProperty("java.class.path") + '/' + pathToImagesDir);
            if (!imagesDir.exists() || !imagesDir.isDirectory()) {
                System.out.println(imagesDir.getAbsolutePath());
                System.out.println(System.getProperty("java.class.path"));
                throw new NotDirectoryException("Cannot find directory with Images");
            }
            searchForImages(imagesDir);
        }

        public Image getImageByName(String name) {
            return nameToImage.get(name);
        }
    }
}
