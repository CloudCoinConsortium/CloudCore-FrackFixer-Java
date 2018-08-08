package com.cloudcore.frackfixer;

import com.cloudcore.frackfixer.core.*;
import com.cloudcore.frackfixer.utils.CoinUtils;
import com.cloudcore.frackfixer.utils.SimpleLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FrackFixer {


    /* INSTANCE VARIABLES */

    public static SimpleLogger logger;

    private IFileSystem fileUtils;
    private RAIDA raida;

    private int totalValueToBank;
    private int totalValueToFractured;
    private int totalValueToCounterfeit;

    public boolean continueExecution = true;
    public boolean IsFixing = false;


    /* CONSTRUCTORS */

    public FrackFixer(IFileSystem fileUtils, int timeout) {
        this.fileUtils = fileUtils;
        raida = RAIDA.GetInstance();
        totalValueToBank = 0;
        totalValueToCounterfeit = 0;
        totalValueToFractured = 0;
    }

    public String fixOneGuidCorner(int raida_ID, CloudCoin cc, int corner, int[] trustedTriad) {
        //RAIDA raida = RAIDA.GetInstance();

        /*1. WILL THE BROKEN RAIDA FIX? check to see if it has problems echo, detect, or fix. */
        if (raida.nodes[raida_ID].FailsFix || raida.nodes[raida_ID].FailsEcho || raida.nodes[raida_ID].FailsEcho) {
            String response = "RAIDA Fails Echo or Fix. Try again when RAIDA online.";
            updateLog(response);
            return response;
        } else {
            /*2. ARE ALL TRUSTED RAIDA IN THE CORNER READY TO HELP?*/

            if (!raida.nodes[trustedTriad[0]].FailsEcho || !raida.nodes[trustedTriad[0]].FailsDetect || !raida.nodes[trustedTriad[1]].FailsEcho || !!raida.nodes[trustedTriad[1]].FailsDetect || !raida.nodes[trustedTriad[2]].FailsEcho || !raida.nodes[trustedTriad[2]].FailsDetect) {
                /*3. GET TICKETS AND UPDATE RAIDA STATUS TICKETS*/
                String[] ans = {cc.an.get(trustedTriad[0]), cc.an.get(trustedTriad[1]), cc.an.get(trustedTriad[2])};
                raida.GetTickets(trustedTriad, ans, cc.nn, cc.getSn(), CoinUtils.getDenomination(cc), 3000);

                /*4. ARE ALL TICKETS GOOD?*/
                if (raida.nodes[trustedTriad[0]].HasTicket && raida.nodes[trustedTriad[1]].HasTicket && raida.nodes[trustedTriad[2]].HasTicket) {
                    /*5.T YES, so REQUEST FIX*/
                    //DetectionAgent da = new DetectionAgent(raida_ID, 5000);
                    if (!continueExecution) {
                        String response = "Aborting Fix for new operation.";
                        updateLog(response);
                        return response;
                    }
                    Response fixResponse = RAIDA.GetInstance().nodes[raida_ID].Fix(trustedTriad, raida.nodes[trustedTriad[0]].Ticket, raida.nodes[trustedTriad[1]].Ticket, raida.nodes[trustedTriad[2]].Ticket, cc.an.get(raida_ID));
                    /*6. DID THE FIX WORK?*/
                    if (fixResponse.success) {
                        String response = "RAIDA" + raida_ID + " unfracked successfully.";
                        updateLog(response);
                        return response;
                    } else {
                        String response = "RAIDA failed to accept tickets on corner " + corner;
                        updateLog(response);
                        return response;
                    }
                } else { //no three good tickets
                    String response = "Trusted servers failed to provide tickets for corner " + corner;
                    updateLog(response);
                    return response;
                }
            }

            String response = "One or more of the trusted triad will not echo and detect. So not trying.";
            updateLog(response);
            return response;
        }
    }


    /* PUBLIC METHODS */

    public int[] FixAll() {
        IsFixing = true;
        continueExecution = true;
        int[] results = new int[3];
        File[] frackedFiles = FileSystem.GetFilesArray(fileUtils.FrackedFolder, Config.allowedExtensions);

        CloudCoin frackedCC;

        if (frackedFiles.length < 0)
            updateLog("You have no fracked coins.");

        for (int i = 0; i < frackedFiles.length; i++) {
            if (!continueExecution) {
                System.out.println("Aborting Fix 1");
                break;
            }
            String response = "Unfracking coin " + (i + 1) + " of " + frackedFiles.length;
            updateLog(response);
            try {
                frackedCC = fileUtils.LoadCoin(this.fileUtils.FrackedFolder + frackedFiles[i]);
                if (frackedCC == null)
                    throw new IOException();
                CoinUtils.consoleReport(frackedCC);

                frackedCC = fixCoin(frackedCC); // Will attempt to unfrack the coin.
                if (!continueExecution) {
                    System.out.println("Aborting Fix 2");
                    break;
                }
                CoinUtils.consoleReport(frackedCC);
                switch (frackedCC.folder.toLowerCase()) {
                    case "bank":
                        this.totalValueToBank++;
                        this.fileUtils.overWrite(this.fileUtils.BankFolder, frackedCC);
                        this.deleteCoin(this.fileUtils.FrackedFolder + frackedFiles[i].getName());
                        updateLog("CloudCoin was moved to Bank.");
                        break;
                    case "counterfeit":
                        this.totalValueToCounterfeit++;
                        this.fileUtils.overWrite(this.fileUtils.CounterfeitFolder, frackedCC);
                        this.deleteCoin(this.fileUtils.FrackedFolder + frackedFiles[i].getName());
                        updateLog("CloudCoin was moved to Trash.");
                        break;
                    default://Move back to fracked folder
                        this.totalValueToFractured++;
                        this.deleteCoin(this.fileUtils.FrackedFolder + frackedFiles[i].getName());
                        this.fileUtils.overWrite(this.fileUtils.FrackedFolder, frackedCC);
                        updateLog("CloudCoin was moved back to Fracked folder.");
                        break;
                }
            } catch (IOException e) {
                updateLog(e.getMessage());
            }
        }

        results[0] = this.totalValueToBank;
        results[1] = this.totalValueToCounterfeit; // System.out.println("Counterfeit and Moved to trash: "+totalValueToCounterfeit);
        results[2] = this.totalValueToFractured; // System.out.println("Fracked and Moved to Fracked: "+ totalValueToFractured);
        IsFixing = false;
        continueExecution = true;
        updateLog("Finished Frack Fixing. Fixed " + totalValueToBank + " CloudCoins and moved them into Bank Folder");
        if (totalValueToBank > 0)
            ;//raida.OnLogRecieved(pge);

        return results;
    }


    public boolean deleteCoin(String path) {
        // System.out.println("Deleteing Coin: "+path + this.fileName + extension);
        try {
            Files.delete(Paths.get(path));
        } catch (IOException e) {
            System.out.println(e);
            return false;
            //  CoreLogger.Log(e.toString());
        }
        return true;
    }


    public CloudCoin fixCoin(CloudCoin brokeCoin) {
        /*0. RESET TICKETS IN RAIDA STATUS TO EMPTY*/
        //RAIDA_Status.resetTickets();
        for (Node node : RAIDA.GetInstance().nodes) node.resetTicket();

        /*0. RESET THE DETECTION to TRUE if it is a new COIN */
        for (Node node : RAIDA.GetInstance().nodes) node.newCoin();

        //RAIDA_Status.newCoin();

        CoinUtils.setAnsToPans(brokeCoin);// Make sure we set the RAIDA to the cc ans and not new pans.
        long before = System.currentTimeMillis();

        String fix_result = "";
        FixitHelper fixer;

        /*START*/
        /*1. PICK THE CORNER TO USE TO TRY TO FIX */
        int corner = 1;
        // For every guid, check to see if it is fractured
        for (int raida_ID = 0; raida_ID < 25; raida_ID++) {
            if (!continueExecution) {
                System.out.println("Stopping Execution");
                return brokeCoin;
            }
            //  System.out.println("Past Status for " + raida_ID + ", " + brokeCoin.pastStatus[raida_ID]);

            if (CoinUtils.getPastStatus(brokeCoin, raida_ID).toLowerCase() != "pass")//will try to fix everything that is not perfect pass.
            {
                updateLog("Attempting to fix RAIDA " + raida_ID);

                fixer = new FixitHelper(raida_ID, brokeCoin.an.toArray(new String[0]));

                //trustedServerAns = new String[] { brokeCoin.ans[fixer.currentTriad[0]], brokeCoin.ans[fixer.currentTriad[1]], brokeCoin.ans[fixer.currentTriad[2]] };
                corner = 1;
                while (!fixer.finished) {
                    if (!continueExecution) {
                        System.out.println("Stopping Execution");
                        return brokeCoin;
                    }
                    updateLog("Using corner " + corner + " Pown is " + brokeCoin.pown);
                    fix_result = fixOneGuidCorner(raida_ID, brokeCoin, corner, fixer.currentTriad);
                    // updateLog(" fix_result: " + fix_result + " for corner " + corner);
                    if (fix_result.contains("success")) {
                        //Fixed. Do the fixed stuff
                        CoinUtils.setPastStatus(brokeCoin, "pass", raida_ID);
                        fixer.finished = true;
                        corner = 1;
                    } else {
                        //Still broken, do the broken stuff.
                        corner++;
                        fixer.setCornerToCheck(corner);
                    }
                }
            }
        }

        for (int raida_ID = 24; raida_ID > 0; raida_ID--) {
            //  System.out.println("Past Status for " + raida_ID + ", " + brokeCoin.pastStatus[raida_ID]);
            if (!continueExecution) {
                return brokeCoin;
            }

            if (CoinUtils.getPastStatus(brokeCoin, raida_ID).toLowerCase() != "pass")//will try to fix everything that is not perfect pass.
            {
                updateLog("Attempting to fix RAIDA " + raida_ID);

                fixer = new FixitHelper(raida_ID, brokeCoin.an.toArray(new String[0]));

                //trustedServerAns = new String[] { brokeCoin.ans[fixer.currentTriad[0]], brokeCoin.ans[fixer.currentTriad[1]], brokeCoin.ans[fixer.currentTriad[2]] };
                corner = 1;
                while (!fixer.finished) {
                    updateLog("Using corner " + corner);

                    fix_result = fixOneGuidCorner(raida_ID, brokeCoin, corner, fixer.currentTriad);
                    // updateLog(" fix_result: " + fix_result + " for corner " + corner);
                    if (fix_result.contains("success")) {
                        //Fixed. Do the fixed stuff
                        CoinUtils.setPastStatus(brokeCoin, "pass", raida_ID);
                        fixer.finished = true;
                        corner = 1;
                    } else {
                        //Still broken, do the broken stuff.
                        corner++;
                        fixer.setCornerToCheck(corner);
                    }
                }
            }
        }
        long after = System.currentTimeMillis();
        long ts = after - before;
        updateLog("Time spent fixing RAIDA in milliseconds: " + ts);

        CoinUtils.calculateHP(brokeCoin);//how many fails did it get
        //  cu.gradeCoin();// sets the grade and figures out what the file extension should be (bank, fracked, counterfeit, lost

        Grader.GradeSimple(brokeCoin, fileUtils);
        CoinUtils.calcExpirationDate();
        return brokeCoin;
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
