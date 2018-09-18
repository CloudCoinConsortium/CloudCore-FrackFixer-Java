package com.cloudcore.frackfixer.core;

import com.cloudcore.frackfixer.utils.CoinUtils;
import com.cloudcore.frackfixer.utils.FileUtils;
import com.cloudcore.frackfixer.utils.Utils;
import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;

public class FileSystem {


    /* Fields */

    public static String RootPath = "C:" + File.separator + "CloudCoins-FrackFixer" + File.separator;

    public static String DetectedFolder = RootPath + Config.TAG_DETECTED + File.separator;
    public static String SuspectFolder = RootPath + Config.TAG_SUSPECT + File.separator;

    public static String BankFolder = RootPath + Config.TAG_BANK + File.separator;
    public static String FrackedFolder = RootPath + Config.TAG_FRACKED + File.separator;
    public static String CounterfeitFolder = RootPath + Config.TAG_COUNTERFEIT + File.separator;
    public static String LostFolder = RootPath + Config.TAG_LOST + File.separator;

    public static String LogsFolder = RootPath + Config.TAG_LOGS + File.separator;

    public static ArrayList<CloudCoin> importCoins;
    public static ArrayList<CloudCoin> predetectCoins;


    /* Methods */

    /**
     * Creates directories in the location defined by RootPath.
     *
     * @return true if all folders were created or already exist, otherwise false.
     */
    public static boolean createDirectories() {
        try {
            Files.createDirectories(Paths.get(RootPath));

            Files.createDirectories(Paths.get(BankFolder));
            Files.createDirectories(Paths.get(FrackedFolder));
            Files.createDirectories(Paths.get(CounterfeitFolder));
            Files.createDirectories(Paths.get(LostFolder));

            Files.createDirectories(Paths.get(DetectedFolder));
            Files.createDirectories(Paths.get(SuspectFolder));

            Files.createDirectories(Paths.get(LogsFolder));
        } catch (Exception e) {
            System.out.println("FS#CD: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }


    /**
     * Loads all CloudCoins from a specific folder.
     *
     * @param folder the folder to search for CloudCoin files.
     * @return an ArrayList of all CloudCoins in the specified folder.
     */
    public static ArrayList<CloudCoin> loadFolderCoins(String folder) {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();

        String[] filenames = FileUtils.selectFileNamesInFolder(folder);
        for (String filename : filenames) {
            int index = filename.lastIndexOf('.');
            if (index == -1) continue;

            String extension = filename.substring(index + 1);

            switch (extension) {
                case "stack":
                    ArrayList<CloudCoin> coins = FileUtils.loadCloudCoinsFromStack(folder, filename);
                    folderCoins.addAll(coins);
                    break;
            }
        }

        return folderCoins;
    }

    /**
     * Deletes CloudCoins files from a specific folder.
     *
     * @param cloudCoins the ArrayList of CloudCoins to delete.
     * @param folder     the folder to delete from.
     */
    public static void removeCoins(ArrayList<CloudCoin> cloudCoins, String folder) {
        for (CloudCoin coin : cloudCoins) {
            removeCoin(coin, folder);
        }
    }
    public static void removeCoin(CloudCoin coin, String folder) {
        try {
            Files.deleteIfExists(Paths.get(folder + coin.currentFilename));
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public static void saveCoin(CloudCoin coin) {
        saveCoin(coin, coin.folder);
    }
    public static void saveCoin(CloudCoin coin, String folder) {
        Gson gson = Utils.createGson();
        try {
            coin.currentFilename = FileUtils.ensureFilenameUnique(CoinUtils.generateFilename(coin), ".stack", coin.folder);
            Stack stack = new Stack(coin);
            Files.write(Paths.get(folder + coin.currentFilename), gson.toJson(stack).getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
    public static void saveCoins(ArrayList<CloudCoin> coins) {
        for (CloudCoin coin : coins) {
            saveCoin(coin);
        }
    }

    public static void detectPreProcessing() {
        for (CloudCoin coin : importCoins) {
            String fileName = coin.getFullFilePath();
            int coinExists = 0;
            for (CloudCoin folderCoin : predetectCoins)
                if (folderCoin.getSn() == coin.getSn())
                    coinExists++;
            //int coinExists = (int) Arrays.stream(predetectCoins.toArray(new CloudCoin[0])).filter(x -> x.getSn() == coin.getSn()).count();

            //if (coinExists > 0)
            //{
            //    String suffix = Utils.randomString(16);
            //    fileName += suffix.toLowerCase();
            //}

            Stack stack = new Stack(coin);
            try {
                Files.write(Paths.get(SuspectFolder + fileName + ".stack"), Utils.createGson().toJson(stack).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("FS#DPP: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }
}

