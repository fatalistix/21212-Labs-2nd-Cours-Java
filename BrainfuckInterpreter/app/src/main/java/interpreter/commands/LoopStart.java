package interpreter.commands;

import interpreter.commands.base.Command;
import interpreter.commands.base.CommandManager;
import interpreter.commands.base.ExecutionContext;
import interpreter.patterns.factory.FactoryObjectCreatingException;



public class LoopStart implements Command {
    @Override
    public void debug(ExecutionContext ec, CommandManager cm) throws CommandDebugException, FactoryObjectCreatingException {
        int commandCode;
        // ec.markCurrentCommand();
        while ((commandCode = ec.readCommand()) != -1 && commandCode != ']') {
            cm.debug(commandCode, ec);
        }
        if (commandCode == -1) {
            throw new CommandDebugException("Got \"[\", but \"]\" not found");
        }
        // ec.demarkCommand();
    }

    @Override
    public void run(ExecutionContext ec, CommandManager cm) throws CommandRuntimeException {
        ec.markCommand();
        int commandCode;
        int counter = 0;
        while (ec.readFromCeil() != 0 && ++counter < 1000) {
            while ((commandCode = ec.readCommand()) != ']') {
                cm.run(commandCode, ec);
            }
            ec.resetToLastMarkedCommand();
        }
        if (counter == 1000) {
            throw new CommandRuntimeException("Loop limit reached: 1000 iterations");
        }
        while ((commandCode = ec.readCommand()) != ']') {
            cm.skip(commandCode, ec);
        }
        ec.demarkCommand();
        // ec.readCommand();
    }

    @Override
    public void skip(ExecutionContext ec, CommandManager cm) {
        int commandCode;
        while ((commandCode = ec.readCommand()) != ']') {
            cm.skip(commandCode, ec);
        }
    }
}