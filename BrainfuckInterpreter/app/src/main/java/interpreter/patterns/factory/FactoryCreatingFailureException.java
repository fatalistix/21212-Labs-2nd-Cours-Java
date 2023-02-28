package interpreter.patterns.factory;

public class FactoryCreatingFailureException extends Exception {

    public FactoryCreatingFailureException() {
    }

    public FactoryCreatingFailureException(String message) {
        super(message);
    }

    public FactoryCreatingFailureException(Throwable cause) {
        super(cause);
    }

    public FactoryCreatingFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public FactoryCreatingFailureException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
