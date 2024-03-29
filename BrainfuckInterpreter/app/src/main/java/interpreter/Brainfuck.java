package interpreter;

import interpreter.commands.base.CommandManager;
import interpreter.commands.base.ExecutionContext;
import interpreter.commands.base.Command.CommandDebugException;
import interpreter.commands.base.Command.CommandRuntimeException;
import interpreter.patterns.factory.FactoryCreatingFailureException;
import interpreter.patterns.factory.FactoryObjectCreatingException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Brainfuck {

    private static Logger logger = LogManager.getLogger(Brainfuck.class);

    private CommandManager cManager;

    private ExecutionContext context = new ExecutionContext();

    public Brainfuck() throws BrainfuckCreatingException {
        try {
            cManager = new CommandManager();
        } catch (FactoryCreatingFailureException e) {
            logger.error("Fatal error: cannot create factory");
            throw new BrainfuckCreatingException(e);
        }
    }

    public void load(String commands) throws BrainfuckDebugException, BrainfuckIncompleteCommandsInputException {
        context.loadInputCommands(commands);
        context.resetToFirstMarkedCommand();
        int c;
        try {
            while ((c = context.readCommand()) != -1) {
                cManager.debug(c, context);
            }
        } catch (FactoryObjectCreatingException e) {
            logger.warn("Command " + e.getMessage().charAt(0) + " wasn't found");
            throw new BrainfuckDebugException("Error with command \"" + e.getMessage().charAt(0) + "\": debug failed", e);
        } catch (CommandDebugException e) {
            logger.warn("Failed debugging command " + e.getMessage().charAt(0));
            throw new BrainfuckIncompleteCommandsInputException(e.getMessage(), e);
        }
    }

    public void debug(String commands) throws BrainfuckDebugException, BrainfuckIncompleteCommandsInputException {
        context.setInputCommands(commands);
        int c;
        try {
            while ((c = context.readCommand()) != -1) {
                cManager.debug(c, context);
            }
        } catch (FactoryObjectCreatingException e) {
            logger.warn("Command " + e.getMessage().charAt(0) + " wasn't found");
            throw new BrainfuckDebugException("Error with command \"" + e.getMessage().charAt(0) + "\": debug failed", e);
        } catch (CommandDebugException e) {
            logger.warn("Failed debugging command " + e.getMessage().charAt(0));
            throw new BrainfuckIncompleteCommandsInputException(e.getMessage(), e);
        }
    }

    public String execute(String data) throws BrainfuckRuntimeException {
        context.setInputData(data);
        context.resetToStartCommand();
        int c;
        try {
            while ((c = context.readCommand()) != -1) {
                cManager.run(c, context);
            }
        } catch (CommandRuntimeException e) {
            logger.warn(e.getMessage());
            throw new BrainfuckRuntimeException(e.getMessage(), e);
        }
        return context.getOutput();
    }
    public void reset() {
        context.reset();
    }

    public int getValue() {
        return context.readFromCeil();
    }

    public class BrainfuckCreatingException extends Exception {
        public BrainfuckCreatingException() {
        }

        public BrainfuckCreatingException(String message) {
            super(message);
        }

        public BrainfuckCreatingException(Throwable cause) {
            super(cause);
        }

        public BrainfuckCreatingException(String message, Throwable cause) {
            super(message, cause);
        }

        public BrainfuckCreatingException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

    }

    public class BrainfuckDebugException extends Exception {

        public BrainfuckDebugException() {
        }

        public BrainfuckDebugException(String message) {
            super(message);
        }

        public BrainfuckDebugException(Throwable cause) {
            super(cause);
        }

        public BrainfuckDebugException(String message, Throwable cause) {
            super(message, cause);
        }

        public BrainfuckDebugException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
        
    }

    public class BrainfuckRuntimeException extends Exception {

        public BrainfuckRuntimeException() {
        }

        public BrainfuckRuntimeException(String message) {
            super(message);
        }

        public BrainfuckRuntimeException(Throwable cause) {
            super(cause);
        }

        public BrainfuckRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }

        public BrainfuckRuntimeException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
        
    }

    public class BrainfuckIncompleteCommandsInputException extends Exception {

        public BrainfuckIncompleteCommandsInputException() {
        }

        public BrainfuckIncompleteCommandsInputException(String message) {
            super(message);
        }

        public BrainfuckIncompleteCommandsInputException(Throwable cause) {
            super(cause);
        }

        public BrainfuckIncompleteCommandsInputException(String message, Throwable cause) {
            super(message, cause);
        }

        public BrainfuckIncompleteCommandsInputException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
        
    }
}
