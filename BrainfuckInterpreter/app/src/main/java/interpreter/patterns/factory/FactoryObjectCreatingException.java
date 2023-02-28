package interpreter.patterns.factory;

public class FactoryObjectCreatingException extends Exception {
    public FactoryObjectCreatingException() {
    }

    public FactoryObjectCreatingException(String message) {
        super(message);
    }

    public FactoryObjectCreatingException(Throwable cause) {
        super(cause);
    }

    public FactoryObjectCreatingException(String message, Throwable cause) {
        super(message, cause);
    }

    public FactoryObjectCreatingException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
