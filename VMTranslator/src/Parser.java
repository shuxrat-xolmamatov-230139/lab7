import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {

    private int pointer;
    private String[] currentCommand;
    private ArrayList<String> instructions = new ArrayList<String>();



    public Parser(File input) throws FileNotFoundException, Exception {
        try {
            removeWhitespace(input);
            pointer = -1;
        } catch (FileNotFoundException fnf) {
            throw new FileNotFoundException(fnf.getMessage());
        }
    }

    public boolean hasMoreCommands() {
        return (pointer < instructions.size() - 1);
    }

    public void advance() {
        pointer++;
        currentCommand = instructions.get(pointer).split("\\s");
    }

    public CommandType commandType() {
        String type = currentCommand[0].toLowerCase();
        switch (type) {
            case "add":
            case "sub":
            case "neg":
            case "eq":
            case "gt":
            case "lt":
            case "and":
            case "or":
            case "not":
                return CommandType.C_ARITHMETIC;
            case "push":
                return CommandType.C_PUSH;
            case "pop":
                return CommandType.C_POP;
            case "label":
                return CommandType.C_LABEL;
            case "goto":
                return CommandType.C_GOTO;
            case "if-goto":
                return CommandType.C_IF;
            case "function":
                return CommandType.C_FUNCTION;
            case "call":
                return CommandType.C_CALL;
            case "return":
                return CommandType.C_RETURN;
            default:
                throw new RuntimeException("Invalid command type: " + type);
        }
    }

    public String arg1() {
        if(this.commandType() == CommandType.C_ARITHMETIC) {
            return currentCommand[0];
        }
        else {
            return currentCommand[1];
        }
    }

    public int arg2() throws NumberFormatException {
        try {
            return Integer.parseInt(currentCommand[2]);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException("Invalid argument: " + nfe.getMessage());
        }
    }

    public void removeWhitespace(File input) throws FileNotFoundException {
      try (Scanner in = new Scanner(input)) {
          while (in.hasNextLine()) {
              String next = in.nextLine().trim();
              if (next.isEmpty() || next.startsWith("//")) {
                  continue; // skip empty lines and whole-line comments
              }
              String command = "";
              for (String token : next.split("\\s+")) {
                  if (token.startsWith("//")) {
                      break; // skip inline comments
                  }
                  command += token + " ";
              }
              if (!command.trim().isEmpty()) {
                  instructions.add(command.trim());
              }
          }
      } catch (FileNotFoundException fnf) {
          throw new FileNotFoundException("File not found: " + fnf.getMessage());
      }
    }
}

