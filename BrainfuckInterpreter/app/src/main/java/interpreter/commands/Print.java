package interpreter.commands;

import interpreter.commands.base.Command;
import interpreter.commands.base.CommandManager;
import interpreter.commands.base.ExecutionContext;

public class Print implements Command {

    @Override
    public void debug(ExecutionContext ec, CommandManager cm) throws CommandDebugException {
    }

    @Override
    public void run(ExecutionContext ec, CommandManager cm) throws CommandRuntimeException {
        ec.writeFromCeilToOutput();
    }
    
}
