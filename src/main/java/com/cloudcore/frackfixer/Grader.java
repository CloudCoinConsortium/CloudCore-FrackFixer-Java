package com.cloudcore.frackfixer;

import com.cloudcore.frackfixer.core.CloudCoin;
import com.cloudcore.frackfixer.core.IFileSystem;
import com.cloudcore.frackfixer.utils.Utils;

public class Grader {

    /**
     * Determines the coin's folder based on a simple grading schematic.
     */
    public static void GradeSimple(CloudCoin coin, IFileSystem FS) {
        if (isPassingSimple(coin.pown)) {
            if (isFrackedSimple(coin.pown))
                coin.folder = FS.FrackedFolder;
            else
                coin.folder = FS.BankFolder;
        }
        else {
            if (isHealthySimple(coin.pown))
                coin.folder = FS.CounterfeitFolder;
            else
                coin.folder = FS.LostFolder;
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
     * @return true if the pown result contains more than 5 fracked grades.
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
