package com.cloudcore.frackfixer;

import com.cloudcore.frackfixer.core.Config;
import com.cloudcore.frackfixer.core.FileSystem;
import com.cloudcore.frackfixer.core.RAIDA;
import com.cloudcore.frackfixer.utils.SimpleLogger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {


    /* Fields */

    public static SimpleLogger logger;

    public static int networkNumber = 1;


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
        FileSystem.loadFileSystem();

        logger = new SimpleLogger(FileSystem.LogsFolder + "logs" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")).toLowerCase() + ".log");

        try {
            //SetupRAIDA();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SetupRAIDA() {
        try {
            RAIDA.instantiate();
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        if (RAIDA.networks.size() == 0) {
            updateLog("No Valid Network found.Quitting!!");
            System.exit(1);
        }
        updateLog(RAIDA.networks.size() + " Networks found.");
        RAIDA raida = RAIDA.networks.get(0);
        for (RAIDA r : RAIDA.networks)
            if (networkNumber == r.networkNumber) {
                raida = r;
                break;
            }

        RAIDA.activeRAIDA = raida;
        if (raida == null) {
            updateLog("Selected Network Number not found. Quitting.");
            System.exit(0);
        }
        else {
            updateLog("Network Number set to " + networkNumber);
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

    public static void updateLog(String message) {
        System.out.println(message);
        logger.Info(message);
    }
}
