package com.cloudcore.frackfixer;

import com.cloudcore.frackfixer.core.Config;
import com.cloudcore.frackfixer.core.FrackFixer;
import com.cloudcore.frackfixer.coreclasses.FileSystem;
import com.cloudcore.frackfixer.utils.SimpleLogger;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    public static String rootFolder = Paths.get("C:/CloudCoins-FrackFixer").toAbsolutePath().toString();

    static FileSystem FS;
    static FrackFixer fixer;

    public static SimpleLogger logger;

    public static void main(String[] args) {
        try {
            setup();

            fixer = new FrackFixer(FS, Config.milliSecondsToTimeOut);
            FrackFixer.logger = logger;
            fix();
        } catch (Exception e) {
            System.out.println("Uncaught exception - " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private static void setup() {
        FS = new FileSystem(rootFolder);
        FS.CreateDirectories();
        FS.LoadFileSystem();

        logger = new SimpleLogger(FS.LogsFolder + "logs" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")).toLowerCase() + ".log", true);
    }

    private static void fix() {
        fixer.continueExecution = true;
        fixer.IsFixing = true;
        fixer.FixAll();
        fixer.IsFixing = false;
    }
}
