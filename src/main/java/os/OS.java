package os;

import assembler.Assembler;
import filesystem.Directories;
import processess.PCBList;
import shell.Shell;
import utils.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Collects together all modules and run system
 */
public class OS {

    /** code of DUMMY process */
    private static final String DUMMY = "loop: JMP loop";

    private static final String LOGO =  "    ____  __  ________   _________ __           ____  _____\n" +
                                        "   / __ \\/ / / /_  __/  / ____/ (_) /_____ _   / __ \\/ ___/\n" +
                                        "  / /_/ / / / / / /    / __/ / / / __/ __ `/  / / / /\\__ \\ \n" +
                                        " / ____/ /_/ / / /    / /___/ / / /_/ /_/ /  / /_/ /___/ / \n" +
                                        "/_/    \\____/ /_/    /_____/_/_/\\__/\\__,_/   \\____//____/  \n" +
                                        "                                                           ";

    public OS() {
        updateInitialFiles();

        createAndCompile(DUMMY, "dummy", false);
        final byte[] dummyExec = Directories.getTargetDir().getFiles().getFileClean("dummy.exe");
        PCBList.list.addDummy(dummyExec);
    }

    /**
     * starting the system
     */
    public void run() {
        Utils.log("system started");
        Shell.echoOn();
        Shell.println(LOGO);
        boolean closing = false;
        while(!closing) {
            Utils.log("os step");
            PCBList.list.processor.run();
            try {
                closing = Shell.interpret();
            } catch (IOException ignored) {
            }
        }
    }

    private static final String PATH_TO_FILES = "src/main/resources/assembler/";
    private static final String[] INITIAL_PROGRAMS = new String[] { "f16", "f4", "files", "prc", "get" };
    private static final String INITIAL_PROGRAMS_DIR = "sample";

    public static void updateInitialFiles() {
        Utils.log("updating initial programs");

        Directories.setCurrentDir(Directories.getDir().getName());
        Directories.setCurrentDir(INITIAL_PROGRAMS_DIR);
        for (final String filename : INITIAL_PROGRAMS) {
            Directories.getCurrentDir().getFiles().deleteFile(filename + ".asm");
            Directories.getCurrentDir().getFiles().deleteFile(filename + ".exe");
        }

        Directories.setCurrentDir(Directories.getDir().getName());
        Directories.getDir().removeDir(INITIAL_PROGRAMS_DIR);
        Directories.getDir().addDirectory(INITIAL_PROGRAMS_DIR);
        Directories.setCurrentDir(INITIAL_PROGRAMS_DIR);

        for (final String filename : INITIAL_PROGRAMS) {
            try {
                createAndCompile(getFileContent(filename), filename, false);
//                if (filename.equals(INITIAL_PROGRAMS[0])) // save space on disk
//                    Directories.getCurrentDir().getFiles().deleteFile(filename + ".asm");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Directories.setCurrentDir(Directories.getDir().getName());
    }

    private static String getFileContent(final String filename) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(PATH_TO_FILES + filename + ".asm"));
        final StringBuilder fileBuilder = new StringBuilder();
        while (true) {
            final String currentLine = reader.readLine();
            if (currentLine != null) fileBuilder.append(currentLine).append("\n");
            else break;
        }
        final String file = fileBuilder.toString();
        reader.close();
        return file;
    }

    /**
     * Creates file with given code and second with its executable
     * @param codeText assembler code
     * @param fileName name of file to create
     */
    private static void createAndCompile(final String codeText, final String fileName, final boolean withSource) {
        final byte[] code = codeText.getBytes();
        if (withSource)  Directories.getCurrentDir().getFiles().createFile(fileName + ".asm", code);

        Assembler assembler = new Assembler();
        final byte[] exec = assembler.compile(code);
        Directories.getCurrentDir().getFiles().createFile(fileName + ".exe", exec); //error, non-static method in static context
    }

}
