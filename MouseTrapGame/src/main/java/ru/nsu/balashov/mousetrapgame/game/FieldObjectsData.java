package ru.nsu.balashov.mousetrapgame.game;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

public class FieldObjectsData {
    private static volatile FieldObjectsFactory instance;

    private FieldObjectsData() {}

    public static FieldObjectsFactory getInstance() {
        if (instance == null) {
            try {
                instance = new FieldObjectsFactory();
            } catch (IOException e) {
                instance = null;
            }
        }
        return instance;
    }

    public static class FieldObjectsFactory {
        private final HashMap<String, FieldObjectParams> namesToFieldObjectProperties = new HashMap<>();
        private final Properties nameProperties = new Properties();

        private FieldObjectsFactory() throws IOException {
            try (InputStream is = ClassLoader.getSystemResourceAsStream("ru/nsu/balashov/mousetrapgame/FieldObjects.properties")) {
                nameProperties.load(is);
            } catch (IOException e) {
                throw new IOException(e);
            }
        }


        public FieldObjectParams getParams(String name) {
            name = name.charAt(0) + (name.length() > 2 ? name.substring(3) : "");
            FieldObjectParams forRet = namesToFieldObjectProperties.get(name);
            if (forRet == null) {
                String property = nameProperties.getProperty(name);
                if (property == null) {
                    return null;
                }
                String[] params = property.split(" ");
                try {
                    forRet = new FieldObjectParams(Integer.parseInt(params[0]), Integer.parseInt(params[1]),
                            Boolean.parseBoolean(params[2]), Boolean.parseBoolean(params[3]), params[4]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    return null;
                }
                namesToFieldObjectProperties.put(name, forRet);
            }
            return forRet;
        }
    }
}
