package interpreter.commands;

import interpreter.commands.base.Command;
import interpreter.commands.base.CommandManager;
import interpreter.commands.base.ExecutionContext;
import interpreter.patterns.factory.FactoryObjectCreatingException;

public class LoopEnd implements Command {
    @Override
    public void debug(ExecutionContext ec, CommandManager cm)
            throws CommandDebugException, FactoryObjectCreatingException {
        throw new FactoryObjectCreatingException("Got \"]\" without \"[\" before");
    }

    @Override
    public void run(ExecutionContext ec, CommandManager cm) throws CommandRuntimeException {
    }

    @Override
    public void skip(ExecutionContext ec, CommandManager cm) {
    }
}