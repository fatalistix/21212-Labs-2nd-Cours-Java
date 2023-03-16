package interpreter.patterns.factory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class Factory <Abstract> {

    private static Logger logger = LogManager.getLogger(Factory.class);
    private Properties properties = new Properties();

    private Map<String, Class<?>> cachedClasses = new HashMap<>();

    public Factory(String path) throws FactoryCreatingFailureException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(path)) {
            properties.load(is);
            logger.info("Config file loaded with success");
        }
        catch (IOException e) {
            logger.error("Error with reading from config file");
            throw new FactoryCreatingFailureException(e);
        }
    }
    public Abstract createObject(String name) throws FactoryObjectCreatingException {
        try {
            logger.info("Searching for cached class...");
            Class<?> clazz = cachedClasses.get(name);
            if (clazz == null) {
                logger.info("Cached class not found, trying to create new");
                String s = properties.getProperty(name);
                if (s == null) {
                    logger.warn(name + " exc: No class found with that key");
                    throw new FactoryObjectCreatingException(name + " exc: No class found with that key");
                }
                clazz = Class.forName(s);
                logger.info("Loaded class with key " + name);
                cachedClasses.put(name, clazz);
                logger.info("Class cached");
            }
            //? All checks say that it will be cast correctly
            //@SuppressWarnings(""); //???????????????????????????????????????????????????
            return (Abstract) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            logger.warn(name + " exc: " + e.getMessage());
            throw new FactoryObjectCreatingException(name + " exc: " + e.getMessage(), e);
        }
    }
}
