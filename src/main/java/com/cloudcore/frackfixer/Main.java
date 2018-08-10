package com.cloudcore.frackfixer;

import com.cloudcore.frackfixer.core.Config;
import com.cloudcore.frackfixer.core.FileSystem;
import com.cloudcore.frackfixer.utils.SimpleLogger;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {


    /* Constants */

    public static final String rootFolder = Paths.get("C:/CloudCoins-FrackFixer").toAbsolutePath().toString();


    /* Fields */

    static FileSystem fs;
    public static SimpleLogger logger;

    /* Methods */

    /**
     * Creates an Exporter instance and runs it.
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
    }

    /**
     * Sets up the FileSystem instance in the defined rootFolder.
     */
    private static void setup() {
        fs = new FileSystem(rootFolder);
        fs.createDirectories();
        fs.loadFileSystem();

        logger = new SimpleLogger(fs.LogsFolder + "logs" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")).toLowerCase() + ".log");
    }

    private static void FrackFix() {
        FrackFixer frackFixer = new FrackFixer(fs, Config.milliSecondsToTimeOut);
        FrackFixer.logger = logger;
        frackFixer.continueExecution = true;
        frackFixer.IsFixing = true;
        frackFixer.FixAll();
        frackFixer.IsFixing = false;
    }
}
