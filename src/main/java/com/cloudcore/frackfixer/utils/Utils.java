package com.cloudcore.frackfixer.utils;

import com.cloudcore.frackfixer.core.CloudCoin;
import com.cloudcore.frackfixer.core.Stack;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {


    /* Methods */

    /**
     * Creates a Gson object, a JSON parser for converting JSON Strings and objects.
     *
     * @return a Gson object.
     */
    public static Gson createGson() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
    }

    public static CloudCoin[] LoadJson(String filename) {
        try {
            byte[] json = Files.readAllBytes(Paths.get(filename));
            Gson gson = new Gson();
            Stack coins = gson.fromJson(new String(json), Stack.class);
            return coins.cc;
        } catch (Exception e) {
            return null;
        }
    }

    public static int charCount(String pown, char character) {
        return pown.length() - pown.replace(Character.toString(character), "").length();
    }

    /**
     * Pads a String with characters appended in the beginning.
     * This is primarily used to pad 0's to hexadecimal Strings.
     *
     * @param string  the String to pad.
     * @param length  the length of the output String.
     * @param padding the character to pad the String with.
     * @return a padded String with the specified length.
     */
    public static String padString(String string, int length, char padding) {
        return String.format("%" + length + "s", string).replace(' ', padding);
    }

    /**
     * Method ordinalIndexOf used to parse cloudcoins. Finds the nth number of a character within a String
     *
     * @param str    The String to search in
     * @param substr What to count in the String
     * @param n      The nth number
     * @return The index of the nth number
     */
    public static int ordinalIndexOf(String str, String substr, int n) {
        int pos = str.indexOf(substr);
        while (--n > 0 && pos != -1) {
            pos = str.indexOf(substr, (pos + 1));
        }
        return pos;
    }

    public static String GetHtmlFromURL(String urlAddress) {
        String data = "";

        try {
            URL url = new URL(urlAddress);
            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
            if (200 != connect.getResponseCode())
                return data;

            BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));

            StringBuilder builder = new StringBuilder();
            while ((data = in.readLine()) != null)
                builder.append(data);
            in.close();
            data = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            data = "";
        }

        return data;
    }
}
