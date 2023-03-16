package interpreter.commands.base;

import interpreter.patterns.factory.FactoryObjectCreatingException;

/**
 * Provides interface for all Brainfuck Interpreter commands.
 * It is used in {@code Factory} as a base class for all {@code Brainfuck} commands.
 */
public interface Command {
    /**
     * This command is called by {@code Brainfuck} class first. Checks if
     * this command can be executed without compilation errors.
     *
     * <p>If it's a simple command that cannot have compilation errors
     * e.g. {@code "+"} command or {@code ">"} command, {@code debug} function
     * can be empty.</p>
     * @param ec - {@code ExecutionContext} instance - provides interface for manipulating with
     *           {@code Brainfuck} data. Also provides interface for forcing debugging after fail.
     * @param cm - {@code CommandManager}, that called this command - must be called if
     *           other commands seemed to be executed.
     * @throws CommandDebugException - is thrown by user if command cannot be executed correctly.
     * @throws FactoryObjectCreatingException - mustn't be thrown by user. Throws only by {@code cm}
     * aka {@code CommandManager}
     */
    public void debug(ExecutionContext ec, CommandManager cm) throws CommandDebugException, FactoryObjectCreatingException;


    public void run  (ExecutionContext ec, CommandManager cm) throws CommandRuntimeException;
    public void skip (ExecutionContext ec, CommandManager cm);

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
