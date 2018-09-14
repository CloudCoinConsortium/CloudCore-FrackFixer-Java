package com.cloudcore.frackfixer;

import com.cloudcore.frackfixer.core.*;
import com.cloudcore.frackfixer.utils.CoinUtils;
import com.cloudcore.frackfixer.utils.SimpleLogger;

import java.io.File;

public class FrackFixer {


    /* Fields */

    public static SimpleLogger logger;
    
    private RAIDA raida;

    private int totalValueToBank;
    private int totalValueToFractured;
    private int totalValueToCounterfeit;

    public boolean continueExecution = true;
    public boolean isFixing = false;


    /* Constructors */

    public FrackFixer() {
        raida = RAIDA.getInstance();
        totalValueToBank = 0;
        totalValueToCounterfeit = 0;
        totalValueToFractured = 0;
    }


    /* Methods */

    public boolean fixOneGuidCorner(int raida_ID, CloudCoin cc, int corner, int[] triad) {
        if (raida.nodes[raida_ID].isFailed() || raida.nodes[raida_ID].failsFix) {
            String response = "RAIDA Fails Echo or Fix. Try again when RAIDA online.";
            updateLog(response);
            return false;
        } else {
            if (!raida.nodes[triad[0]].isFailed() || !raida.nodes[triad[1]].isFailed() || !raida.nodes[triad[2]].isFailed()) {
                String[] ans = {cc.getAn().get(triad[0]), cc.getAn().get(triad[1]), cc.getAn().get(triad[2])};
                raida.getTickets(triad, ans, cc.getNn(), cc.getSn(), CoinUtils.getDenomination(cc), Config.milliSecondsToTimeOut);

                if (raida.nodes[triad[0]].hasTicket && raida.nodes[triad[1]].hasTicket && raida.nodes[triad[2]].hasTicket) {
                    if (!continueExecution) {
                        updateLog("Aborting Fix for new operation.");
                        return false;
                    }
                    Response fixResponse = RAIDA.getInstance().nodes[raida_ID].fix(triad, raida.nodes[triad[0]].ticket, raida.nodes[triad[1]].ticket, raida.nodes[triad[2]].ticket, cc.getAn().get(raida_ID));
                    if (fixResponse.success) {
                        updateLog("RAIDA" + raida_ID + " unfracked successfully.");
                        return true;
                    } else {
                        updateLog("RAIDA failed to accept tickets on corner " + corner);
                        return false;
                    }
                } else {
                    updateLog("Trusted servers failed to provide tickets for corner " + corner);
                    return false;
                }
            }
            updateLog("One or more of the trusted triad will not echo and detect. So not trying.");
            return false;
        }
    }


    /* PUBLIC METHODS */

    public int[] fixAll() {
        isFixing = continueExecution = true;
        int[] results = new int[3];
        File[] frackedFiles = FileSystem.GetFilesArray(FileSystem.FrackedFolder, Config.ALLOWED_EXTENSIONS);

        CloudCoin coin;

        if (frackedFiles.length < 0)
            updateLog("You have no fracked coins.");

        updateLog("Fixing fracked coins: " + frackedFiles.length);
        for (int i = 0; i < frackedFiles.length; i++) {
            if (!continueExecution) {
                updateLog("Aborting Fix 1");
                break;
            }
            updateLog("Unfracking coin " + (i + 1) + " of " + frackedFiles.length);

            coin = FileSystem.loadCoin(frackedFiles[i].getParent() + File.separator, frackedFiles[i].getName());
            coin.currentFilename = frackedFiles[i].getName();
            if (coin == null) {
                updateLog(frackedFiles[i] + " is null, skipping");
                continue;
            }
            CoinUtils.consoleReport(coin);
            coin = fixCoin(coin);
            if (!continueExecution) {
                updateLog("Aborting Fix 2");
                break;
            }
            CoinUtils.consoleReport(coin);

            if (FileSystem.BankFolder.equals(coin.folder)) {
                this.totalValueToBank++;
                FileSystem.moveCoin(coin, FileSystem.FrackedFolder, coin.folder, false);
                updateLog("CloudCoin was moved to Bank.");
            }
            else if (FileSystem.CounterfeitFolder.equals(coin.folder)) {
                this.totalValueToCounterfeit++;
                FileSystem.moveCoin(coin, FileSystem.FrackedFolder, coin.folder, false);
                updateLog("CloudCoin was moved to Counterfeit.");
            }
            else {
                this.totalValueToFractured++;
                FileSystem.removeFile(FileSystem.FrackedFolder, frackedFiles[i].getName());
                FileSystem.overWrite(FileSystem.FrackedFolder, coin);
                updateLog("CloudCoin was moved back to Fracked folder.");
            }
        }

        results[0] = this.totalValueToBank;
        results[1] = this.totalValueToCounterfeit; // System.out.println("Counterfeit and Moved to trash: "+totalValueToCounterfeit);
        results[2] = this.totalValueToFractured; // System.out.println("Fracked and Moved to Fracked: "+ totalValueToFractured);
        isFixing = false;
        continueExecution = true;
        updateLog("Finished Frack Fixing. Fixed " + totalValueToBank + " CloudCoins and moved them into Bank Folder");
        return results;
    }


    public CloudCoin fixCoin(CloudCoin coin) {
        FixitHelper fixer;

        for (Node node : RAIDA.getInstance().nodes) node.resetTicket();
        for (Node node : RAIDA.getInstance().nodes) node.newCoin();

        long before = System.currentTimeMillis();

        int corner;
        for (int i = 0; i < 25; i++) {
            if (!continueExecution) {
                System.out.println("Stopping Execution");
                return coin;
            }

            if (!"pass".equals(CoinUtils.getPastStatus(coin, i))) {
                updateLog("Attempting to fix RAIDA " + i);

                fixer = new FixitHelper(i, coin.getAn().toArray(new String[0]));

                corner = 1;
                while (!fixer.finished) {
                    if (!continueExecution) {
                        System.out.println("Stopping Execution");
                        return coin;
                    }
                    updateLog("Using corner " + corner + " Pown is " + coin.getPown());
                    if (fixOneGuidCorner(i, coin, corner, fixer.currentTriad)) {
                        CoinUtils.setPastStatus(coin, "pass", i);
                        fixer.finished = true;
                        corner = 1;
                    } else {
                        corner++;
                        fixer.setCornerToCheck(corner);
                    }
                }
            }
        }

        for (int raida_ID = 24; raida_ID > 0; raida_ID--) {
            if (!continueExecution) return coin;

            if (!"pass".equals(CoinUtils.getPastStatus(coin, raida_ID))) {
                updateLog("Attempting to fix RAIDA " + raida_ID);

                fixer = new FixitHelper(raida_ID, coin.getAn().toArray(new String[0]));

                corner = 1;
                while (!fixer.finished) {
                    updateLog("Using corner " + corner);
                    if (fixOneGuidCorner(raida_ID, coin, corner, fixer.currentTriad)) {
                        CoinUtils.setPastStatus(coin, "pass", raida_ID);
                        fixer.finished = true;
                        corner = 1;
                    } else {
                        corner++;
                        fixer.setCornerToCheck(corner);
                    }
                }
            }
        }
        long after = System.currentTimeMillis();
        long ts = after - before;
        updateLog("Time spent fixing RAIDA in milliseconds: " + ts);

        Grader.gradeSimple(coin);
        CoinUtils.calcExpirationDate(coin);
        return coin;
    }

    public void updateLog(String message) {
        System.out.println(message);
        logger.Info(message);
    }

    /**
     * Sends a message to the SimpleLogger.
     *
     * @param message a log message.
     */
    private void updateLogNoPrint(String message) {
        logger.appendLog(message);
    }
}
