import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class VMTranslator {
    public static void main(String[] args) {
        if(args.length > 0) {
            try {
                File input = new File(args[0]);
                ArrayList<File> files = getFiles(input);

                if(!files.isEmpty()) {
                    String outputName = input.getName().replaceAll("[./].*", "");
                    File output = input.isFile()
                            ? new File(input.getParent(), outputName + ".asm")
                            : new File(input, "main.asm");

                    CodeWriter cw = new CodeWriter(output);

                    cw.writeInit();

                    for(File f : files) {
                        String name = f.getName();
                        name = name.substring(0, name.indexOf('.'));

                        cw.setFileName(name);

                        Parser p = new Parser(f);
                        while(p.hasMoreCommands()) {
                            p.advance();

                            switch (p.commandType()) {
                                case C_ARITHMETIC:
                                    cw.writeArithmetic(p.arg1());
                                    break;
                                case C_PUSH:
                                case C_POP:
                                    cw.writePushPop(p.commandType(), p.arg1(), p.arg2());
                                    break;
                                case C_LABEL:
                                    cw.writeLabel(p.arg1());
                                    break;
                                case C_GOTO:
                                    cw.writeGoto(p.arg1());
                                    break;
                                case C_IF:
                                    cw.writeIf(p.arg1());
                                    break;
                                case C_FUNCTION:
                                    cw.writeFunction(p.arg1(), p.arg2());
                                    break;
                                case C_CALL:
                                    cw.writeCall(p.arg1(), p.arg2());
                                    break;
                                case C_RETURN:
                                    cw.writeReturn();
                                    break;
                                default:
                                    throw new RuntimeException(f + " contains an invalid instruction.");
                            }

                        }
                    }

                    cw.close();
                } else {
                    System.out.println("No .vm files found.");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No source entered.");
        }
    }

    private static ArrayList<File> getFiles(File input) throws FileNotFoundException {
        ArrayList<File> files = new ArrayList<File>();
        if(input.isFile()) {
            // check for .vm extension before adding to list of files
            String filename = input.getName();
            if(filename.endsWith(".vm")) {
                files.add(input);
            }
        } else if(input.isDirectory()) {
            File[] innerFiles = input.listFiles((dir, name) -> name.endsWith(".vm"));
            for(File f : innerFiles) {
                files.add(f);
            }
        } else {
            throw new FileNotFoundException("Could not find file or directory.");
        }
        return files;
    }
}
