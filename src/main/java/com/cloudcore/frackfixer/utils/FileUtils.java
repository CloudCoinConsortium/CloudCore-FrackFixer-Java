package com.cloudcore.frackfixer.utils;

import com.cloudcore.frackfixer.core.CloudCoin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class FileUtils {

    /**
     * Attempts to read a JSON Object from a file.
     *
     * @param jsonFilePath the filepath pointing to the JSON file
     * @return String
     */
    public static String loadJSON(String jsonFilePath) {
        String jsonData = "";
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader(jsonFilePath));
            while ((line = br.readLine()) != null) {
                jsonData += line + System.lineSeparator();
            }
        } catch (IOException e) {
            System.out.println("Failed to open " + jsonFilePath);
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return jsonData;
    }

    /** Attempt to read an array of CloudCoins from a JSON String. */
    public static ArrayList<CloudCoin> loadCloudCoinsFromJSON(String fileName) {
        ArrayList<CloudCoin> cloudCoins = new ArrayList<>();

        String fileJson;
        fileJson = loadJSON(fileName);
        if (fileJson == null) {
            System.out.println("File " + fileName + " was not imported.");
            return cloudCoins;
        }

        JSONArray incomeJsonArray;
        try {
            JSONObject json = new JSONObject(fileJson);
            incomeJsonArray = json.getJSONArray("cloudcoin");
            CloudCoin tempCoin;
            for (int i = 0; i < incomeJsonArray.length(); i++) {
                JSONObject childJSONObject = incomeJsonArray.getJSONObject(i);
                int nn = childJSONObject.getInt("nn");
                int sn = childJSONObject.getInt("sn");
                JSONArray an = childJSONObject.getJSONArray("an");
                String[] ans = toStringArray(an);
                String ed = childJSONObject.getString("ed");

                tempCoin = new CloudCoin(nn, sn, ans);
            }
        } catch (JSONException ex) {
            System.out.println("File " + fileName + " was not imported.");
            ex.printStackTrace();
        }

        return cloudCoins;
    }

    /**
     * Returns an array containing all filenames in a directory.
     *
     * @param directoryPath the directory to check for files
     * @return String[]
     */
    public static String[] selectFileNamesInFolder(String directoryPath) {
        File dir = new File(directoryPath);
        String candidateFileExt = "";
        Collection<String> files = new ArrayList<String>();
        if (dir.isDirectory()) {
            File[] listFiles = dir.listFiles();

            for (File file : listFiles) {
                if (file.isFile()) {//Only add files with the matching file extension
                    files.add(file.getName());
                }
            }
        }
        return files.toArray(new String[]{});
    }

    /**
     * Converts a JSONArray to a String array
     *
     * @param jsonArray a JSONArray Object
     * @return String[]
     */
    public static String[] toStringArray(JSONArray jsonArray) {
        if (jsonArray == null)
            return null;

        String[] arr = new String[jsonArray.length()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = jsonArray.optString(i);
        }
        return arr;
    }
}
