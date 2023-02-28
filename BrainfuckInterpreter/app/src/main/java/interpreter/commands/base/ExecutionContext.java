package interpreter.commands.base;

import java.io.IOException;
import java.io.StringWriter;

import interpreter.patterns.readers.StringReaderWithLoading;

public class ExecutionContext {
    // Constants
    private final int POINTER_INIT_POS = 0;

    private final int MEMORY_SIZE      = 300_000;

    // Private objects
    private char[] memory = new char[MEMORY_SIZE];

    private int memoryPointer = POINTER_INIT_POS;

    private StringReaderWithLoading inputCommands;

    private StringReaderWithLoading inputData;

    private StringWriter output = new StringWriter();

    public ExecutionContext(int memorySize, int pointerInitPos) {
        this.memoryPointer = pointerInitPos;
        this.memory  = new char[memorySize];
    }

    public ExecutionContext() {
    }

    public void resetInputCommands() {
        try {
            inputCommands.reset();
        } catch (IOException e) {
            //? It should never throw it, but if it will we will catch it and do nothing
        }
    }

    public void readFromDataToCeil() throws IllegalArgumentException {
        int buf;
        try {
            buf = inputData.read();
        } catch (IOException e) {
            buf = -1;
        }
        if (buf == -1) {
            throw new IllegalArgumentException("Not enough input values for execute");
        }
        memory[memoryPointer] = (char) buf; //! ????
    }

    public void writeFromCeilToOutput() {
        output.write(memory[memoryPointer]);
    }

    public void setInputCommands(String s) {
        inputCommands = new StringReaderWithLoading(s);
        inputData = null;
    }

    public void loadInputCommands(String s) {
        try {
            inputData.toLoad(s);
        } catch (IOException e) {
            //? It should never throw it, but if it will we will catch it and do nothing
        }
    }

    public void setInputData(String s) {
        inputData = new StringReaderWithLoading(s);
    }

    public String getOutput() {
        String forRet = output.toString();
        output = new StringWriter();
        return forRet;
    }
    public void reset() {
        this.memoryPointer = POINTER_INIT_POS;
        this.memory = new char[MEMORY_SIZE];
        inputData = null;
        inputCommands = null;
        output = null;
    }

    public int readCommand() {
        try {
            return inputCommands.read();
        } catch (IOException e) {
            return -1;
        }
	}
    public void leftShift() throws IllegalArgumentException {
        if (memoryPointer == 0) {
            throw new IllegalArgumentException("You wanted to go lefter then first ceil");
        }
        --memoryPointer;
    }
    public void rightShift() throws IllegalArgumentException {
        if (memoryPointer == memory.length - 1) {
            throw new IllegalArgumentException("You wanted to go righter then last ceil");
        }
        ++memoryPointer;
    }
    public void incrementAtPointer() {
        ++memory[memoryPointer];
    }
    public void decrementAtPointer() {
        --memory[memoryPointer];
    }
}
