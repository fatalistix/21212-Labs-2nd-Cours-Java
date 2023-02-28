package interpreter;

import interpreter.commands.base.CommandManager;
import interpreter.commands.base.CommandManagerException;
import interpreter.commands.base.ExecutionContext;

public class Brainfuck {
    private CommandManager cManager;

    private ExecutionContext context = new ExecutionContext();

    public Brainfuck() throws CommandManagerException  {
        cManager = new CommandManager();
    }

    public void debug(String commands) throws CommandManagerException {
        context.setInputCommands(commands);
        cManager.debug(context);
    }

    public String execute(String data) {
        context.setInputData(data);
        try {
            cManager.execute(context);
        } catch (CommandManagerException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        return context.getOutput();
    }
    public void reset() {
        context.reset();
    }
}
