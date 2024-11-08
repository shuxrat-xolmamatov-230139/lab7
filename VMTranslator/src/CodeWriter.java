import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CodeWriter {
    private String filename;
    private PrintWriter writeOut;
    private int labelNumber;

    private void out(String line) {
        writeOut.println(line);
    }

    private String nextLabel() {
        return String.valueOf(labelNumber++);
    }

    public CodeWriter(File output) throws FileNotFoundException {
        try {
            writeOut = new PrintWriter(output);
            labelNumber = 0;
        } catch (FileNotFoundException fnf) {
            throw new FileNotFoundException("File not found: " + fnf.getMessage());
        }
    }

    public void setFileName(String filename) {
        this.filename = filename;
    }

    public void writeInit() {
        out("// init");
        out("@256");
        out("D=A");
        out("@SP");
        out("M=D");

        // uncomment in project 8
        // writeCall("Sys.init", 0);
    }

    public void writeArithmetic(String operation) {
        out("// " + operation);

        switch (operation.toLowerCase()) {
            case "add":
            case "sub":
            case "and":
            case "or":
                out("@SP");
                out("AM=M-1");
                out("D=M");
                out("A=A-1");
                switch (operation.toLowerCase()) {
                    case "add":
                        out("M=D+M");
                        break;
                    case "sub":
                        out("M=M-D");
                        break;
                    case "and":
                        out("M=D&M");
                        break;
                    case "or":
                        out("M=D|M");
                        break;
                }
                break;
            case "eq":
            case "lt":
            case "gt":
                String label = nextLabel();

                out("@SP");
                out("AM=M-1");
                out("D=M");
                out("A=A-1");
                out("D=M-D");
                out("@COMPARE.TRUE." + label);
                switch (operation.toLowerCase()) {
                    case "eq":
                        out("D;JEQ");
                        break;
                    case "gt":
                        out("D;JGT");
                        break;
                    case "lt":
                        out("D;JLT");
                        break;
                }
                out("@SP");
                out("A=M-1");
                out("M=0");
                out("@COMPARE.END." + label);
                out("0;JMP");
                out("(COMPARE.TRUE." + label + ")");
                out("@SP");
                out("A=M-1");
                out("M=-1");
                out("(COMPARE.END." + label + ")");
                break;
            case "not":
                out("@SP");
                out("A=M-1");
                out("M=!M");
                break;
            case "neg":
                out("@SP");
                out("A=M-1");
                out("M=-M");
                break;
        }
    }

    public void writePushPop(CommandType command, String segment, int index) {
        out("// " + command + " " + segment + " " + index);
        if (command == CommandType.C_PUSH) {
            // save value to D
            switch (segment.toLowerCase()) {
                case "pointer":
                    out(index == 0 ? "@THIS" : "@THAT");
                    out("D=M");
                    break;
                case "static":
                    out("@" + filename + "." + index);
                    out("D=M");
                    break;
                case "constant":
                    out("@" + index);
                    out("D=A");
                    break;
                case "temp":
                    out("@R5"); //
                    out("D=A"); // D=5
                    out("@" + index); // index = i
                    out("A=D+A"); // A = 5 + i
                    out("D=M"); // D =
                    break;
                default:
                    out(getLabel(segment));
                    out("D=M");
                    out("@" + index);
                    out("A=D+A");
                    out("D=M");
                    break;
            }

            // push D to stack
            finishPush();
        } else if (command == CommandType.C_POP) {
            // save address to D
            switch (segment.toLowerCase()) {
                case "pointer":
                    out(index == 0 ? "@THIS" : "@THAT");
                    out("D=A");
                    break;
                case "static":
                    out("@" + filename + "." + index);
                    out("D=A");
                    break;
                case "temp":
                    out("@R5");
                    out("D=A");
                    out("@" + index);
                    out("D=D+A");
                    break;
                default:
                    out(getLabel(segment));
                    out("D=M");
                    out("@" + index);
                    out("D=D+A");
                    break;
            }
            // pop to an address pointed by D
            out("@R13");
            out("M=D");
            out("@SP");
            out("AM=M-1");
            out("D=M");
            out("@R13");
            out("A=M");
            out("M=D");
        }
    }

    public void writeLabel(String label) {
        out("// C_LABEL " + label);
        out("(" + label + ")");
    }

    public void writeGoto(String label) {
        out("// C_GOTO " + label);
        out("@" + label);
        out("0;JMP");
    }

    public void writeIf(String label) {
        out("// C_IF " + label);
        out("@SP");
        out("AM=M-1");
        out("D=M");
        out("@" + label);
        out("D;JNE");
    }

    public void writeCall(String functionName, int numArgs) {
        String label = nextLabel();

        out("// call " + functionName + " " + numArgs);
        // R13 = SP
        out("@SP");
        out("D=M");
        out("@R13");
        out("M=D");

        // push return address
        out("@return." + label);
        out("D=A");
        finishPush();

        // push LCL
        out("@LCL");
        out("D=M");
        finishPush();

        // push ARG
        out("@ARG");
        out("D=M");
        finishPush();

        // push THIS
        out("@THIS");
        out("D=M");
        finishPush();

        // push THAT
        out("@THAT");
        out("D=M");
        finishPush();

        // ARG = R13 - numArgs
        out("@R13");
        out("D=M");
        out("@" + numArgs);
        out("D=D-A");
        out("@ARG");
        out("M=D");

        // LCL = SP
        out("@SP");
        out("D=M");
        out("@LCL");
        out("M=D");

        // goto functionName
        out("@" + functionName);
        out("0;JMP");

        // declare return address label
        out("(return." + label + ")");
    }

    public void writeReturn() {
        out("// return");

        // store return address in R13 = LCL - 5
        out("@LCL");
        out("D=M");
        out("@5");
        out("A=D-A");
        out("D=M");
        out("@R13");
        out("M=D");

        // store return value *(SP-1) in *ARG
        out("@SP");
        out("A=M-1");
        out("D=M");
        out("@ARG");
        out("A=M");
        out("M=D");

        // restore SP = ARG + 1
        out("D=A+1");
        out("@SP");
        out("M=D");

        // restore THAT = *(LCL - 1); LCL--
        out("@LCL");
        out("AM=M-1");
        out("D=M");
        out("@THAT");
        out("M=D");

        // restore THIS = *(LCL - 1); LCL--
        out("@LCL");
        out("AM=M-1");
        out("D=M");
        out("@THIS");
        out("M=D");

        // restore ARG = *(LCL - 1); LCL--
        out("@LCL");
        out("AM=M-1");
        out("D=M");
        out("@ARG");
        out("M=D");

        // restore LCL=*(LCL - 1)
        out("@LCL");
        out("A=M-1");
        out("D=M");
        out("@LCL");
        out("M=D");

        // Jump to return address stored in R13
        out("@R13");
        out("A=M");
        out("0;JMP");
    }

    public void writeFunction(String functionName, int numLocals) {
        writeOut.println("// function " + functionName + numLocals);

        // declare label for function entry
        writeLabel(functionName);

        // initialize local variables to 0
        for (int i = 0; i < numLocals; i++) {
            writePushPop(CommandType.C_PUSH, "constant", 0);
        }
    }

    private String getLabel(String segment) {
        switch (segment.toLowerCase()) {
            case "local":
                return "@LCL";
            case "argument":
                return "@ARG";
            case "this":
                return "@THIS";
            case "that":
                return "@THAT";
            default:
                return null;
        }
    }

    private void finishPush() {
        out("@SP"); // SP=0 M=RAM[SP]=256
        out("A=M"); //A = 256
        out("M=D"); // M = 7
        out("@SP"); // SP=0
        out("M=M+1"); // RAM[0] = 256 + 1 = 257
    }

    public void close() {
        writeOut.close();
    }
}
