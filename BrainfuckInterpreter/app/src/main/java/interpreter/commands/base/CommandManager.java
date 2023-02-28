package interpreter.commands.base;

import java.util.HashMap;
import java.util.Map;

import interpreter.commands.base.Command.CommandDebugException;
import interpreter.commands.base.Command.CommandRuntimeException;
import interpreter.patterns.factory.Factory;
import interpreter.patterns.factory.FactoryCreatingFailureException;
import interpreter.patterns.factory.FactoryObjectCreatingException;

public class CommandManager {
    public CommandManager() throws FactoryCreatingFailureException {
        commandFactory = new Factory<>(PATH);
    }

    public void debug(ExecutionContext context) throws FactoryObjectCreatingException, CommandDebugException {
        int c;
        Command command;
        while ((c = context.readCommand()) != -1) {
            command = cachedCommands.get(c);
            if (command == null) {
                command = commandFactory.createObject(String.valueOf((char) c));
                cachedCommands.put(c, command);
            }
            command.debug(context, this);
        }
    }

    public void execute(ExecutionContext context) throws CommandRuntimeException {
        Command command;
        int c;

        context.resetInputCommands();
        while ((c = context.readCommand()) != -1) {
            command = cachedCommands.get(c);
            command.run(context, this);
        }
    }

    private Map <Integer, Command> cachedCommands = new HashMap<>();
    private Factory <Command> commandFactory;
    private final String PATH = "Classes.cfg";
}
