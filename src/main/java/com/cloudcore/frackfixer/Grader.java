package com.cloudcore.frackfixer;

import com.cloudcore.frackfixer.core.CloudCoin;
import com.cloudcore.frackfixer.core.FileSystem;
import com.cloudcore.frackfixer.utils.Utils;

public class Grader {


    /**
     * Determines the coin's folder based on a simple grading schematic.
     */
    public static void gradeSimple(CloudCoin coin) {
        if (isPassingSimple(coin.getPown())) {
            if (isFrackedSimple(coin.getPown()))
                coin.folder = FileSystem.FrackedFolder;
            else
                coin.folder = FileSystem.BankFolder;
        } else {
            if (isHealthySimple(coin.getPown()))
                coin.folder = FileSystem.CounterfeitFolder;
            else
                coin.folder = FileSystem.LostFolder;
        }
    }

    /**
     * Checks to see if the pown result is a passing grade.
     *
     * @return true if the pown result contains more than 20 passing grades.
     */
    public static boolean isPassingSimple(String pown) {
        return (Utils.charCount(pown, 'p') >= 20);
    }

    /**
     * Checks to see if the pown result is fracked.
     *
     * @return true if the pown result contains any fracked grades.
     */
    public static boolean isFrackedSimple(String pown) {
        return (pown.indexOf('f') != -1);
    }

    /**
     * Checks to see if the pown result is in good health. Unhealthy grades are errors and no-responses.
     *
     * @return true if the pown result contains more than 20 passing or failing grades.
     */
    public static boolean isHealthySimple(String pown) {
        return (Utils.charCount(pown, 'p') + Utils.charCount(pown, 'f') >= 20);
    }
}
