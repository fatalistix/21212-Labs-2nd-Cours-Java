package interpreter.commands.base;

public interface Command {
    public void debug(ExecutionContext ec, CommandManager cm) throws CommandDebugException;
    public void run  (ExecutionContext ec, CommandManager cm) throws CommandRuntimeException;

    public class CommandDebugException extends Exception {
        public CommandDebugException() {
        }
    
        public CommandDebugException(String message) {
            super(message);
        }
    
        public CommandDebugException(Throwable cause) {
            super(cause);
        }
    
        public CommandDebugException(String message, Throwable cause) {
            super(message, cause);
        }
    
        public CommandDebugException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
        
    }

    public class CommandRuntimeException extends Exception {
        public CommandRuntimeException() {
        }

        public CommandRuntimeException(String message) {
            super(message);
        }

        public CommandRuntimeException(Throwable cause) {
            super(cause);
        }

        public CommandRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }

        public CommandRuntimeException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
        
    }
}
