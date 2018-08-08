package com.cloudcore.frackfixer.core;

public class Config {


    /* Constant Fields */

    public static String[] allowedExtensions = new String[] { ".stack", ".jpeg", ".chest", ".bank", ".jpg",".celebrium",".celeb",".csv" };

    public static String URL_DIRECTORY = "http://michael.pravoslavnye.ru/";

    public static final int YEARSTILEXPIRE = 2;

    public static String TAG_IMPORT = "Import";
    public static String TAG_DETECTED = "Detected";
    public static String TAG_SUSPECT = "Suspect";
    public static final String TAG_EXPORT = "Export";

    public static final String TAG_BANK = "Bank";
    public static final String TAG_FRACKED = "Fracked";
    public static final String TAG_COUNTERFEIT = "Counterfeit";
    public static final String TAG_LOST = "Lost";

    public static final String TAG_TEMPLATES = "Templates";
    public static final String TAG_LOGS = "Logs";


    /* Fields */

    public static int milliSecondsToTimeOut = 20000;
    public static int MultiDetectLoad = 200;
    public static int NodeCount = 25;
    public static int PassCount = 16;
}
