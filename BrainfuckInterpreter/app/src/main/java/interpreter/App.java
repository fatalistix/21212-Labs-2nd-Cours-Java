package interpreter;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import interpreter.commands.base.CommandManagerException;

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
                brainfuck.debug(input);
                
                out.print("[Data] > ");
                input = sc.nextLine();
                out.println("[Out~] > " + brainfuck.execute(input));
            }

        } catch (CommandManagerException e) {
            out.println("Fatal error: " + e.getMessage());
            return;
        } 

        out.println("Exited without errors");
    }
}
