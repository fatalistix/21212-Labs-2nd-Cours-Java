package interpreter.commands;

import interpreter.commands.base.Command;
import interpreter.commands.base.CommandManager;
import interpreter.commands.base.ExecutionContext;

public class LeftShift implements Command {
    @Override
    public void debug(interpreter.commands.base.ExecutionContext ec, CommandManager cm) throws CommandDebugException {
    }

    @Override
    public void run(interpreter.commands.base.ExecutionContext ec, CommandManager cm) throws CommandRuntimeException {
        try {
            ec.leftShift();
        } catch (IllegalArgumentException e) {
            throw new CommandRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void skip(ExecutionContext ec, CommandManager cm) {
    }
}
