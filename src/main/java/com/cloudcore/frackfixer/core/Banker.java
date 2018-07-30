package com.cloudcore.frackfixer.core;

import com.cloudcore.frackfixer.coreclasses.FileSystem;

import java.io.File;

public class Banker {

    public IFileSystem fileUtils;

    public Banker(IFileSystem fileUtils) {
        this.fileUtils = fileUtils;
    }

    public int[] countCoins(String directoryPath) {
        int[] returnCounts = new int[6];
        // 0. Total, 1.1s, 2,5s, 3.25s 4.100s, 5.250s
        File[] files = FileSystem.GetFilesArray(directoryPath, Config.allowedExtensions);
        for (int i = 0; (i < files.length); i++) {
            String[] nameParts = files[i].getName().split(".");
            String denomination = nameParts[0];
            switch (denomination) {
                case "1":
                    returnCounts[0]++;
                    returnCounts[1]++;
                    break;
                case "5":
                    returnCounts[0] += 5;
                    returnCounts[2]++;
                    break;
                case "25":
                    returnCounts[0] += 25;
                    returnCounts[3]++;
                    break;
                case "100":
                    returnCounts[0] += 100;
                    returnCounts[4]++;
                    break;
                case "250":
                    returnCounts[0] += 250;
                    returnCounts[5]++;
                    break;
            }

        }


        return returnCounts;
    }
}
