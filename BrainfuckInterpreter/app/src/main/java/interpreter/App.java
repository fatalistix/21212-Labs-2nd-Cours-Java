package interpreter;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import interpreter.Brainfuck.BrainfuckCreatingException;
import interpreter.Brainfuck.BrainfuckDebugException;
import interpreter.Brainfuck.BrainfuckIncompleteCommandsInputException;
import interpreter.Brainfuck.BrainfuckRuntimeException;

public class App {
    private static InputStream in  = System.in;
    private static PrintStream out = System.out; 

    public static void main(String[] args) {
        Brainfuck brainfuck;
        String input;
        try (Scanner sc = new Scanner(in)) {
            brainfuck = new Brainfuck();
            while (true) {
                out.print("[Code] > ");
                input = sc.nextLine();
                if (input.equals("exit"))  { break; } 
                if (input.equals(""))      { continue; }
                if (input.equals("reset")) { 
                    brainfuck.reset();
                    continue;
                }
                
                try {
                    brainfuck.debug(input);
                } catch (BrainfuckDebugException e) {
                    out.println("[DERR] > " + e.getMessage());
                    continue;
                } catch (BrainfuckIncompleteCommandsInputException e) {
                    // TODO Auto-generated catch block
                    out.println("[NOTH] > Now it do nothing, because never reachable");
                }
                
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
            out.println("[FAIL] > UNEXPECTED ERROR " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return;
        }
        out.println("[SUCC] > Exited without errors");
    }
}
