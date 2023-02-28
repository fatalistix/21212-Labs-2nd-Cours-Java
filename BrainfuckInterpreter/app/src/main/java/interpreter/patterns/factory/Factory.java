package interpreter.patterns.factory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class Factory <Abstract> {
    private Properties properties = new Properties();

    private Map<String, Class<?>> cachedClasses = new HashMap<>();

    public Factory(String path) throws FactoryCreatingFailureException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(path)) {
            properties.load(is);
        }
        catch (IOException e) {
            throw new FactoryCreatingFailureException(e);
        }
    }
    public Abstract createObject(String name) throws FactoryObjectCreatingException {
        try {
            Class<?> clazz = cachedClasses.get(name);
            if (clazz == null) {
                String s = properties.getProperty(name);
                if (s == null) {
                    throw new FactoryObjectCreatingException(name + " exc: No class found with that name");
                }
                clazz = Class.forName(s);
                cachedClasses.put(name, clazz);
            }
            //? All checks say that it will be casted correctly
            return (Abstract) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new FactoryObjectCreatingException(name + " exc: " + e.getMessage(), e);
        }
    }
}
