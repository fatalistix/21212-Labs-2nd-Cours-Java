package interpreter.patterns.factory;

public class FactoryObjectCreatingFailure extends Exception {
    public FactoryObjectCreatingFailure() {
    }

    public FactoryObjectCreatingFailure(String message) {
        super(message);
    }

    public FactoryObjectCreatingFailure(Throwable cause) {
        super(cause);
    }

    public FactoryObjectCreatingFailure(String message, Throwable cause) {
        super(message, cause);
    }

    public FactoryObjectCreatingFailure(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
