package interpreter.commands.base;

import java.util.HashMap;
import java.util.Map;

import interpreter.commands.base.Command.CommandDebugException;
import interpreter.commands.base.Command.CommandRuntimeException;
import interpreter.patterns.factory.Factory;
import interpreter.patterns.factory.FactoryCreatingFailureException;
import interpreter.patterns.factory.FactoryObjectCreatingException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class CommandManager {

    private static Logger logger = LogManager.getLogger(CommandManager.class);
    private Map <Integer, Command> cachedCommands = new HashMap<>();

    private Factory <Command> commandFactory;

    private final String PATH = "Classes.cfg";

    public CommandManager() throws FactoryCreatingFailureException {
        commandFactory = new Factory<>(PATH);
    }

    public void debug(int commandCode, ExecutionContext context) throws FactoryObjectCreatingException, CommandDebugException {
        if (commandCode > Character.MAX_VALUE || commandCode < Character.MIN_VALUE) {
            logger.warn("Got command code out of char range, did nothing");
            return;
        }
        Command command = cachedCommands.get(commandCode);
        if (command == null) {
            logger.info("Command with code " + commandCode + "not found");
            command = commandFactory.createObject(String.valueOf((char) commandCode));
            logger.info("Command created successfully");
            cachedCommands.put(commandCode, command);
        }
        command.debug(context, this);
    }
    public void run(int commandCode, ExecutionContext context) throws CommandRuntimeException {
        Command command = cachedCommands.get(commandCode);
        command.run(context, this);
    }

    public void skip(int commandCode, ExecutionContext context) {
        Command command = cachedCommands.get(commandCode);
        command.skip(context, this);
    }
}
