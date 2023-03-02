package interpreter.commands;

import interpreter.commands.base.Command;
import interpreter.commands.base.CommandManager;
import interpreter.commands.base.ExecutionContext;

public class RightShift implements Command {
    @Override
    public void debug(ExecutionContext ec, CommandManager cm) throws CommandDebugException {
    }

    @Override
    public void run(ExecutionContext ec, CommandManager cm) throws CommandRuntimeException {
        try {
            ec.rightShift();
        } catch (IllegalArgumentException e) {
            throw new CommandRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void skip(ExecutionContext ec, CommandManager cm) {
    }
}
