package interpreter;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import interpreter.Brainfuck.BrainfuckCreatingException;
import interpreter.Brainfuck.BrainfuckDebugException;
import interpreter.Brainfuck.BrainfuckIncompleteCommandsInputException;
import interpreter.Brainfuck.BrainfuckRuntimeException;

public class App {
    //?????????????????????????????????????????????????????????
    private static InputStream in  = System.in;
    private static PrintStream out = System.out; 
    //???????????????????????????????????????????????????????

    public static void main(String[] args) {
        Brainfuck brainfuck;
        String input;
        boolean waiting = false;
        try (Scanner sc = new Scanner(in)) {
            brainfuck = new Brainfuck();
            while (true) {
                out.print("[Code] > ");
                input = sc.nextLine();
                if (input.equals("exit"))  { break; } 
                if (input.equals(""))      { continue; }
                if (input.equals("reset")) { 
                    out.println("[INFO] > Reset");
                    brainfuck.reset();
                    continue;
                }
                if (input.equals("value")) {
                    out.println("[INFO] > " + brainfuck.getValue());
                    continue;
                }
                
                try {
                    if (waiting) {
                        brainfuck.load(input);
                    } else {
                        brainfuck.debug(input);
                    }
                } catch (BrainfuckDebugException e) {
                    out.println("[DERR] > " + e.getMessage());
                    continue;
                } catch (BrainfuckIncompleteCommandsInputException e) {
                    // out.println("[DERR] > Incomplete statement: " + e.getMessage());
                    waiting = true;
                    continue;
                }
                waiting = false;
                out.print("[Data] > ");
                input = sc.nextLine();
                try {
                    out.println("[Out~] > " + brainfuck.execute(input));
                } catch (BrainfuckRuntimeException e) {
                    out.println("[RERR] > " + e.getMessage());
                }
            }
        } catch (BrainfuckCreatingException e) {
            out.println("[CERR] > Error during creating a Brainfuck Interpreter");
            return;
        } catch (Exception e) {
            out.println("\n[FAIL] > UNEXPECTED ERROR " + /* e.getClass().getSimpleName() +*/ ": " + e.getMessage());
            return;
        }
        out.println("[SUCC] > Exited without errors");
    }
}
