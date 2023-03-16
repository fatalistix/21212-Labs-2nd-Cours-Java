package interpreter;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import interpreter.Brainfuck.BrainfuckCreatingException;
import interpreter.Brainfuck.BrainfuckDebugException;
import interpreter.Brainfuck.BrainfuckIncompleteCommandsInputException;
import interpreter.Brainfuck.BrainfuckRuntimeException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This is the main application class, which, as default, works with streams
 * and provides basic user terminal based GUI like:
 * [Code] >
 * [Data] >
 * [Out~] >
 * [INFO] >
 * etc.
 */
public class App {
    private static final InputStream in  = System.in;
    private static final PrintStream out = System.out;

    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        Brainfuck brainfuck;
        String input;
        boolean waiting = false;
        try (Scanner sc = new Scanner(in)) {
            brainfuck = new Brainfuck();

            logger.info("Brainfuck instance created successfully");
            logger.info("Ready for reading and executing commands");

            while (true) {
                out.print("[Code] > ");
                input = sc.nextLine();
                if (input.equals("exit"))  {
                    logger.info("Got special keyword exit, exiting...");
                    break;
                }
                if (input.equals(""))      { continue; }
                if (input.equals("reset")) { 
                    out.println("[INFO] > Reset");
                    brainfuck.reset();
                    logger.info("Brainfuck reset successfully");
                    continue;
                }
                if (input.equals("value")) {
                    out.println("[INFO] > " + brainfuck.getValue());
                    logger.info("Printed current ceil's value");
                    continue;
                }
                
                try {
                    if (waiting) {
                        brainfuck.load(input);
                    } else {
                        brainfuck.debug(input);
                    }
                } catch (BrainfuckDebugException e) {
                    logger.warn("Got unknown command, message: "+ e.getMessage());
                    out.println("[DERR] > " + e.getMessage());
                    continue;
                } catch (BrainfuckIncompleteCommandsInputException e) {
                    // out.println("[DERR] > Incomplete statement: " + e.getMessage());
                    logger.warn("Got incomplete set of commands, waiting for user's input, message: " + e.getMessage());
                    waiting = true;
                    continue;
                }
                waiting = false;
                logger.info("Debug successfully finished");
                out.print("[Data] > ");
                input = sc.nextLine();
                try {
                    out.println("[Out~] > " + brainfuck.execute(input));
                } catch (BrainfuckRuntimeException e) {
                    out.println("[RERR] > " + e.getMessage());
                    logger.warn("Got a runtime exception, message: " + e.getMessage());
                }

                logger.info("Code executed without errors");
            }
        } catch (BrainfuckCreatingException e) {
            out.println("[CERR] > Error during creating a Brainfuck Interpreter");
            logger.error("Error during creating a Brainfuck Interpreter");
            return;
        } catch (Exception e) {
            out.println("\n[FAIL] > UNEXPECTED ERROR " + /* e.getClass().getSimpleName() +*/ ": " + e.getMessage());
            logger.error("FATAL ERROR, CAUGHT UNEXPECTED EXCEPTION " + e.getClass().getSimpleName() + ": message: " + e.getMessage());
            return;
        }
        out.println("[SUCC] > Exited without errors");
        logger.info("Program finished with success");
    }
}
