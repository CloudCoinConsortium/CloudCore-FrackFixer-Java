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
import java.util.Random;

public class Utils {


    private static Random random = new Random();
    private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";


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
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static String RandomString(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
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
