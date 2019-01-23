package com.cloudcore.frackfixer.raida;

import com.cloudcore.frackfixer.core.CloudCoin;
import com.cloudcore.frackfixer.core.Config;
import com.cloudcore.frackfixer.core.FileSystem;
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

    public static RAIDA activeRAIDA;
    public static ArrayList<RAIDA> networks = new ArrayList<>();

    public Node[] nodes = new Node[Config.nodeCount];

    public MultiDetectRequest multiRequest;
    public ArrayList<CloudCoin> coins;
    public Response[] responseArray = new Response[Config.nodeCount];

    public int networkNumber = 1;


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
    // Return Main RAIDA Network populated with default Nodes Addresses(Network 1)
    public static RAIDA getInstance() {
        return new RAIDA();
    }

    public static void resetInstance() {
        activeRAIDA = null;
        networks.clear();
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
}
