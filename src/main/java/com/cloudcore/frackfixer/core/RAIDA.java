package com.cloudcore.frackfixer.core;

import com.cloudcore.frackfixer.utils.CoinUtils;
import com.cloudcore.frackfixer.utils.SimpleLogger;
import com.cloudcore.frackfixer.utils.Utils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RAIDA {


    /* Fields */

    public static RAIDA mainNetwork;
    public static RAIDA activeRAIDA;

    public static SimpleLogger logger;

    public static ArrayList<RAIDA> networks = new ArrayList<>();
    public Node[] nodes = new Node[Config.nodeCount];
    public Response[] responseArray = new Response[Config.nodeCount];
    public MultiDetectRequest multiRequest;

    public int networkNumber = 1;


    public ArrayList<CloudCoin> coins;


    /* Constructors */

    private RAIDA() {
        for (int i = 0; i < Config.nodeCount; i++) {
            nodes[i] = new Node(i + 1);
        }
    }

    private RAIDA(Network network) {
        nodes = new Node[network.raida.length];
        this.networkNumber = network.nn;
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new Node(i + 1, network.raida[i]);
        }
    }


    /* Methods */

    // RAIDA details are read from Directory URL first.
    // In case of failure, it falls back to a file on the file system
    public static ArrayList<RAIDA> instantiate() {
        String nodesJson = "";
        networks.clear();

        try {
            nodesJson = Utils.getHtmlFromURL(Config.URL_DIRECTORY);
        } catch (Exception e) {
            System.out.println(": " + e.getLocalizedMessage());
            e.printStackTrace();
            if (!Files.exists(Paths.get("directory.json"))) {
                System.out.println("RAIDA instantiation failed. No Directory found on server or local path");
                System.exit(-1);
                return null;
            }
            try {
                nodesJson = new String(Files.readAllBytes(Paths.get(Paths.get("").toAbsolutePath().toString()
                        + File.separator + "directory.json")));
            } catch (IOException e1) {
                System.out.println("| " + e.getLocalizedMessage());
                e1.printStackTrace();
            }
        }

        try {
            Gson gson = Utils.createGson();
            RAIDADirectory dir = gson.fromJson(nodesJson, RAIDADirectory.class);

            for (Network network : dir.networks) {
                System.out.println("Available Networks: " + network.raida[0].urls[0].url + " , " + network.nn);
                networks.add(RAIDA.getInstance(network));
            }
        } catch (Exception e) {
            System.out.println("RAIDA instantiation failed. No Directory found on server or local path");
            e.printStackTrace();
            System.exit(-1);
        }

        if (networks == null || networks.size() == 0) {
            System.out.println("RAIDA instantiation failed. No Directory found on server or local path");
            System.exit(-1);
            return null;
        }
        return networks;
    }

    // Return Main RAIDA Network populated with default Nodes Addresses(Network 1)
    public static RAIDA getInstance() {
        return (mainNetwork != null) ? mainNetwork : new RAIDA();
    }

    public static RAIDA getInstance(Network network) {
        return new RAIDA(network);
    }

    public void getTickets(int[] triad, String[] ans, int nn, int sn, int denomination, int milliSecondsToTimeOut) {
        CompletableFuture task = getTicket(0, triad[0], nn, sn, ans[0], denomination);
        CompletableFuture task2 = getTicket(1, triad[1], nn, sn, ans[1], denomination);
        CompletableFuture task3 = getTicket(2, triad[2], nn, sn, ans[2], denomination);
        CompletableFuture[] taskList = new CompletableFuture[]{task, task2, task3};

        try {
            CompletableFuture.allOf(taskList).get(milliSecondsToTimeOut, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture getTicket(int i, int raidaID, int nn, int sn, String an, int d) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                responseArray[raidaID] = nodes[raidaID].getTicket(nn, sn, an, d).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public static CompletableFuture<Object> processNetworkCoins(int networkNumber) {
        return processNetworkCoins(networkNumber, true);
    }

    public static CompletableFuture<Object> processNetworkCoins(int networkNumber, boolean changeANS) {
        return CompletableFuture.supplyAsync(() -> {
            FileSystem.detectPreProcessing();

            System.out.println("Getting coins...");
            ArrayList<CloudCoin> folderSuspectCoins = FileSystem.loadFolderCoins(FileSystem.SuspectFolder);
            ArrayList<CloudCoin> suspectCoins = new ArrayList<>();
            for (CloudCoin oldPredetectCoin : folderSuspectCoins) {
                if (networkNumber == oldPredetectCoin.getNn()) {
                    suspectCoins.add(oldPredetectCoin);
                }
            }

            FileSystem.predetectCoins = suspectCoins;

            System.out.println("Getting network...");
            RAIDA raida = null;
            for (RAIDA network : RAIDA.networks) {
                if (network != null && networkNumber == network.networkNumber) {
                    raida = network;
                    break;
                }
            }

            if (raida == null)
                return null;

            // Process Coins in Lots of 200. Can be changed from Config File
            int lotCount = suspectCoins.size() / Config.multiDetectLoad;
            if (suspectCoins.size() % Config.multiDetectLoad > 0)
                lotCount++;

            int coinCount = 0;
            int totalCoinCount = suspectCoins.size();
            int progress;
            for (int i = 0; i < lotCount; i++) {
                ArrayList<CloudCoin> coins = new ArrayList<>();
                try { // Pick up to 200 Coins and send them to RAIDA
                    coins = new ArrayList<>(suspectCoins.subList(i * Config.multiDetectLoad, Math.min(suspectCoins.size(), 200)));
                    raida.coins = coins;
                } catch (Exception e) {
                    System.out.println(":" + e.getLocalizedMessage());
                    e.printStackTrace();
                }
                ArrayList<CompletableFuture<Node.MultiDetectResponse>> tasks = raida.getMultiDetectTasks(raida.coins, changeANS);
                try {
                    try {
                        System.out.println("Waiting for futures...");
                        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).get();
                    } catch (Exception e) {
                        System.out.println("RAIDA#PNC:" + e.getLocalizedMessage());
                    }

                    for (int j = 0; j < coins.size(); j++) {
                        CloudCoin coin = coins.get(j);
                        StringBuilder pownString = new StringBuilder();
                        coin.setPown("");
                        for (int k = 0; k < Config.nodeCount; k++) {
                            Response nodeResponse = raida.nodes[k].multiResponse.responses[j];
                            pownString.append(nodeResponse.outcome, 0, 1);
                        }
                        coin.setPown(pownString.toString());
                        coinCount++;

                        updateLog("No. " + coinCount + ". Coin Detected. sn - " + coin.getSn() + ". Pass Count - " + CoinUtils.getPassCount(coin) +
                                ". Fail Count  - " + CoinUtils.getFailCount(coin) + ". Result - " + CoinUtils.getDetectionResult(coin) + "." + coin.getPown());
                        System.out.println("Coin Detected. sn - " + coin.getSn() + ". Pass Count - " + CoinUtils.getPassCount(coin) +
                                ". Fail Count  - " + CoinUtils.getFailCount(coin) + ". Result - " + CoinUtils.getDetectionResult(coin));
                        //coin.sortToFolder();
                        progress = (coinCount) * 100 / totalCoinCount;
                        System.out.println("Minor Progress- " + progress);
                    }
                    progress = (coinCount - 1) * 100 / totalCoinCount;
                    System.out.println("Minor Progress- " + progress);
                    FileSystem.removeCoins(coins, FileSystem.SuspectFolder);
                    FileSystem.saveCoins(coins);

                    updateLog(progress + " % of Coins on Network " + networkNumber + " processed.");
                } catch (Exception e) {
                    System.out.println("RAIDA#PNC: " + e.getLocalizedMessage());
                }
            }

            return null;
        });
    }

    public ArrayList<CompletableFuture<Node.MultiDetectResponse>> getMultiDetectTasks(ArrayList<CloudCoin> coins, boolean changeANs) {
        this.coins = coins;

        int[] nns = new int[coins.size()];
        int[] sns = new int[coins.size()];

        String[][] ans = new String[Config.nodeCount][];
        String[][] pans = new String[Config.nodeCount][];

        int[] dens = new int[coins.size()]; // Denominations
        ArrayList<CompletableFuture<Node.MultiDetectResponse>> detectTasks = new ArrayList<>(); // Stripe the coins

        for (int i = 0; i < coins.size(); i++) {
            if (changeANs)
                CoinUtils.generatePAN(coins.get(i));
            else
                CoinUtils.setAnsToPans(coins.get(i));
            nns[i] = coins.get(i).getNn();
            sns[i] = coins.get(i).getSn();
            dens[i] = CoinUtils.getDenomination(coins.get(i));
            System.out.println(coins.get(i).toString());
        }

        try {
            multiRequest = new MultiDetectRequest();
            multiRequest.timeout = Config.milliSecondsToTimeOut;
            for (int nodeNumber = 0; nodeNumber < Config.nodeCount; nodeNumber++) {
                ans[nodeNumber] = new String[coins.size()];
                pans[nodeNumber] = new String[coins.size()];

                for (int i = 0; i < coins.size(); i++) {
                    ans[nodeNumber][i] = coins.get(i).getAn().get(nodeNumber);
                    pans[nodeNumber][i] = coins.get(i).pan[nodeNumber];
                }
                multiRequest.an[nodeNumber] = ans[nodeNumber];
                multiRequest.pan[nodeNumber] = pans[nodeNumber];
                multiRequest.nn = nns;
                multiRequest.sn = sns;
                multiRequest.d = dens;
            }
        } catch (Exception e) {
            System.out.println("/0" + e.getLocalizedMessage());
            e.printStackTrace();
        }

        try {
            for (int nodeNumber = 0; nodeNumber < Config.nodeCount; nodeNumber++) {
                detectTasks.add(nodes[nodeNumber].MultiDetect());
            }
        } catch (Exception e) {
            System.out.println("/1" + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return detectTasks;
    }

    public static void updateLog(String message) {
        System.out.println(message);
        logger.Info(message);
    }
}
