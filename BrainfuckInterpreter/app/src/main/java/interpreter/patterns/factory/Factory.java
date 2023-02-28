package interpreter.patterns.factory;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class Factory <Abstract> {
    public Factory(String path) throws FactoryCreatingFailureException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(path)) {
            properties.load(is);
            System.err.println(properties.getProperty("+"));
        }
        catch (IOException e) {
            throw new FactoryCreatingFailureException(e);
        }
    }

    public Abstract createObject(String name) throws FactoryObjectCreatingFailure {
        try {
            Class<?> clazz = cachedClasses.get(name);
            if (clazz == null) {
                clazz = Class.forName(properties.getProperty(name));
                cachedClasses.put(name, clazz);
                return (Abstract) clazz.getDeclaredConstructor().newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new FactoryObjectCreatingFailure(e);
        }
        return null;
    }

    private Properties properties = new Properties();
    private Map<String, Class<?>> cachedClasses = new HashMap<>();
}
