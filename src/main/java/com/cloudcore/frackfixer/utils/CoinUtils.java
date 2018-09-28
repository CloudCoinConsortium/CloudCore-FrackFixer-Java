package com.cloudcore.frackfixer.utils;

import com.cloudcore.frackfixer.core.CloudCoin;
import com.cloudcore.frackfixer.core.Config;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

public class CoinUtils {


    /* Methods */

    public static void calcExpirationDate(CloudCoin coin) {
        coin.setEd(calcExpirationDate());
    }
    public static String calcExpirationDate() {
        LocalDate expirationDate = LocalDate.now().plusYears(Config.EXPIRATION_YEARS);
        return (expirationDate.getMonthValue() + "-" + expirationDate.getYear());
    }

    public static int getPassCount(CloudCoin coin) {
        return Utils.charCount(coin.getPown(), 'p');
    }
    public static int getFailCount(CloudCoin coin) {
        return Utils.charCount(coin.getPown(), 'f');
    }
    public static String getDetectionResult(CloudCoin coin) {
        return (getPassCount(coin) >= Config.passCount) ? "Pass" : "Fail";
    }

    public static String getPastStatus(CloudCoin coin, int raida_id) {
        String returnString = "";
        char[] pownArray = coin.getPown().toCharArray();
        switch (pownArray[raida_id]) {
            case 'e':
                returnString = "error";
                break;
            case 'f':
                returnString = "fail";
                break;
            case 'p':
                returnString = "pass";
                break;
            case 'u':
                returnString = "undetected";
                break;
            case 'n':
                returnString = "noresponse";
                break;
        }
        return returnString;
    }

    public static boolean setPastStatus(CloudCoin coin, String status, int raida_id) {
        char[] pownArray = coin.getPown().toCharArray();
        switch (status) {
            case "error":
                pownArray[raida_id] = 'e';
                break;
            case "fail":
                pownArray[raida_id] = 'f';
                break;
            case "pass":
                pownArray[raida_id] = 'p';
                break;
            case "undetected":
                pownArray[raida_id] = 'u';
                break;
            case "noresponse":
                pownArray[raida_id] = 'n';
                break;
        }
        coin.setPown(new String(pownArray));
        return true;
    }

    /**
     * Returns a denomination describing the currency value of the CloudCoin.
     *
     * @param coin CloudCoin
     * @return 1, 5, 25, 100, 250, or 0 if the CloudCoin's serial number is invalid.
     */
    public static int getDenomination(CloudCoin coin) {
        int sn = coin.getSn();
        int nom;
        if (sn < 1)
            nom = 0;
        else if ((sn < 2097153))
            nom = 1;
        else if ((sn < 4194305))
            nom = 5;
        else if ((sn < 6291457))
            nom = 25;
        else if ((sn < 14680065))
            nom = 100;
        else if ((sn < 16777217))
            nom = 250;
        else
            nom = 0;

        return nom;
    }

    /**
     * Generates a name for the CloudCoin based on the denomination, Network Number, and Serial Number.
     * <br>
     * <br>Example: 25.1.6123456
     *
     * @return String a filename
     */
    public static String generateFilename(CloudCoin coin) {
        return getDenomination(coin) + ".CloudCoin." + coin.getNn() + "." + coin.getSn();
    }

    /**
     * Generates secure random GUIDs for pans. An example:
     * <ul>
     * <li>8d3eb063937164c789474f2a82c146d3</li>
     * </ul>
     * These Strings are hexadecimal and have a length of 32.
     */
    public static void generatePAN(CloudCoin coin) {
        coin.pan = new String[Config.nodeCount];
        for (int i = 0; i < Config.nodeCount; i++) {
            SecureRandom random = new SecureRandom();
            byte[] cryptoRandomBuffer = random.generateSeed(16);

            UUID uuid = UUID.nameUUIDFromBytes(cryptoRandomBuffer);
            coin.pan[i] = uuid.toString().replace("-", "");
        }
    }

    /**
     * Updates the Authenticity Numbers to the new Proposed Authenticity Numbers.
     */
    public static void setAnsToPans(CloudCoin coin) {
        for (int i = 0; (i < Config.nodeCount); i++) {
            coin.getAn().set(i, coin.pan[i]);
        }
    }

    public static char[] consoleReport(CloudCoin cc) {
        // Used only for console apps
        //  System.out.println("Finished detecting coin index " + j);
        // PRINT OUT ALL COIN'S RAIDA STATUS AND SET AN TO NEW PAN
        char[] pownArray = cc.getPown().toCharArray();
        String report = "   Authenticity Report SN #" + String.format("{0,8}", cc.getSn()) + ", Denomination: " + String.format("{0,3}", getDenomination(cc)) + "  ";

        return pownArray;
    }
}
