package interpreter.commands.base;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import interpreter.patterns.factory.Factory;
import interpreter.patterns.factory.FactoryCreatingFailureException;

public class CommandManager {
    public CommandManager() throws CommandManagerException {
        try {
            commandFactory = new Factory<>(PATH);
        } catch (FactoryCreatingFailureException e) {
            throw new CommandManagerException(e);
        }
    }

    public void debug(ExecutionContext context) throws CommandManagerException {
        int c;
        Command command;
        try {
            while ((c = context.readCommand()) != -1) {
                command = cachedCommands.get(c);
                if (command == null) {
                    command = commandFactory.createObject(String.valueOf((char) c));
                    cachedCommands.put(c, command);
                }
                command.debug(context, this);
            }
        } catch (Exception e) {
            throw new CommandManagerException(e);
        }
    }

    public void execute(ExecutionContext context) throws CommandManagerException {
        Command command;
        int c;
        try {
            context.resetInputCommands();
            while ((c = context.readCommand()) != -1) {
                command = cachedCommands.get(c);
                command.run(context, this);
            }
        } catch (IOException e) {
            throw new CommandManagerException(e);
        } catch (Exception e) {
            throw new CommandManagerException(e);
        }
    }

    private Map <Integer, Command> cachedCommands = new HashMap<>();
    private Factory <Command> commandFactory;
    private final String      PATH = "Classes.cfg";
}
