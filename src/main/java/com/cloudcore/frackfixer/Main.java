package com.cloudcore.frackfixer;

import com.cloudcore.frackfixer.core.FileSystem;
import com.cloudcore.frackfixer.raida.RAIDA;
import com.cloudcore.frackfixer.utils.SimpleLogger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {


    /* Fields */

    public static SimpleLogger logger;


    /* Methods */

    /**
     * Creates a FrackFixer instance and runs it.
     */
    public static void main(String[] args) {
        try {
            setup();
            FrackFix();
        } catch (Exception e) {
            System.out.println("Uncaught exception - " + e.getLocalizedMessage());
            logger.appendLog(e.toString(), e.getStackTrace());
            e.printStackTrace();
        }

        logger.writeLogToFile();
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
