package interpreter.commands;

import interpreter.commands.base.Command;
import interpreter.commands.base.CommandManager;
import interpreter.commands.base.ExecutionContext;
import interpreter.patterns.factory.FactoryObjectCreatingException;



public class LoopStart implements Command {
    @Override
    public void debug(ExecutionContext ec, CommandManager cm) throws CommandDebugException, FactoryObjectCreatingException {
        int commandCode;
        while ((commandCode = ec.readCommand()) != -1 && commandCode != ']') {
            cm.debug(commandCode, ec);
        }
        if (commandCode == -1) {
            throw new CommandDebugException("Got \"[\", but \"]\" not found");
        }
    }

    @Override
    public void run(ExecutionContext ec, CommandManager cm) throws CommandRuntimeException {
        
    }

}