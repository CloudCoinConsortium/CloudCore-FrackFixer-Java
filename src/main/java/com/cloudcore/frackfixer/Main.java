package com.cloudcore.frackfixer;

import com.cloudcore.frackfixer.core.FileSystem;
import com.cloudcore.frackfixer.raida.RAIDA;
import com.cloudcore.frackfixer.utils.FileUtils;
import com.cloudcore.frackfixer.utils.SimpleLogger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.cloudcore.frackfixer.raida.RAIDA.resetInstance;

public class Main {


    /* Fields */

    public static SimpleLogger logger;


    /* Methods */

    /**
     * Creates a FrackFixer instance and runs it.
     */
    public static void main(String[] args) {
        SimpleLogger.writeLog("ServantAuthenticatorStarted", "");
        singleRun = isSingleRun(args);
        if (args.length != 0 && Files.exists(Paths.get(args[0]))) {
            System.out.println("New root path: " + args[0]);
            FileSystem.changeRootPath(args[0]);
        }

        if (0 != FileUtils.selectFileNamesInFolder(FileSystem.FrackedFolder).length) {
            setup();
            FrackFix();
            exitIfSingleRun();
            resetInstance();
        }

        FolderWatcher watcher = new FolderWatcher(FileSystem.FrackedFolder);
        System.out.println("Watching folders at " + FileSystem.FrackedFolder + "...");
        boolean detectingFiles = false;
        long timeWaitingForFilesToBeWritten = 0;

        while (true) {
            try {
                Thread.sleep(1000);

                // If a change is detected, set the timer.
                if (watcher.newFileDetected()) {
                    detectingFiles = true;
                    timeWaitingForFilesToBeWritten = System.currentTimeMillis() + 1000;
                    System.out.println("found files, waiting a second to authenticate");
                    continue;
                }

                if (!detectingFiles || timeWaitingForFilesToBeWritten > System.currentTimeMillis())
                    continue;

                setup();

                detectingFiles = false;

                System.out.println("Processing Network Coins...");
                FrackFix();
                resetInstance();
                exitIfSingleRun();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Uncaught exception - " + e.getLocalizedMessage());
                resetInstance();
            }
        }
    }

    public static boolean singleRun = false;
    public static boolean isSingleRun(String[] args) {
        for (String arg : args)
            if (arg.equals("singleRun"))
                return true;
        return false;
    }
    public static void exitIfSingleRun() {
        if (singleRun)
            System.exit(0);
    }

    /**
     * Sets up the FileSystem instance in the defined rootFolder.
     */
    private static void setup() {
        FileSystem.createDirectories();

        logger = new SimpleLogger(FileSystem.LogsFolder + "logs" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")).toLowerCase() + ".log");

        try {
            //SetupRAIDA();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void FrackFix() {
        FrackFixer frackFixer = new FrackFixer();
        FrackFixer.logger = logger;
        frackFixer.continueExecution = true;
        frackFixer.isFixing = true;
        frackFixer.fixAll();
        frackFixer.isFixing = false;
    }
}
