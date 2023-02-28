package interpreter.commands;

import java.io.IOException;

import interpreter.commands.base.Command;
import interpreter.commands.base.CommandManager;
import interpreter.commands.base.ExecutionContext;

public class Read implements Command {

    @Override
    public void debug(ExecutionContext ec, CommandManager cm) throws CommandDebugException {
    }

    @Override
    public void run(ExecutionContext ec, CommandManager cm) throws CommandRuntimeException {
        try {
            ec.readFromDataToCeil();
        } catch(IllegalArgumentException | IOException e) {
            throw new CommandRuntimeException(e);
        }
    }
    
}
