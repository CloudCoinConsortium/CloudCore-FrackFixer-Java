package com.cloudcore.frackfixer.core;

public class Config {


    /* Constant Fields */

    public static final String[] ALLOWED_EXTENSIONS = new String[] { ".stack", ".jpeg", ".chest", ".bank", ".jpg",".celebrium",".celeb",".csv" };

    public static final String URL_DIRECTORY = "http://michael.pravoslavnye.ru/";

    public static final int EXPIRATION_YEARS = 2;

    public static final String TAG_DETECTED = "Detected";
    public static final String TAG_SUSPECT = "Suspect";
    public static final String TAG_EXPORT = "Export";

    public static final String TAG_BANK = "Bank";
    public static final String TAG_FRACKED = "Fracked";
    public static final String TAG_COUNTERFEIT = "Counterfeit";
    public static final String TAG_LOST = "Lost";

    public static final String TAG_LOGS = "Logs";


    /* Fields */

    public static int milliSecondsToTimeOut = 20000;
    public static int multiDetectLoad = 200;
    public static int nodeCount = 25;
    public static int passCount = 16;
}
