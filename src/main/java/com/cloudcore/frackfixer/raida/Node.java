package com.cloudcore.frackfixer.raida;

import com.cloudcore.frackfixer.utils.Utils;

import java.util.concurrent.CompletableFuture;

/**
 * This Class Contains the properties of a RAIDA node.
 */
public class Node {


    /* Fields */

    public enum TicketHistory {Untried, Failed, Success}

    public int nodeNumber;
    public String fullUrl;

    public boolean failsDetect;
    public boolean failsFix;

    public String ticket = "";
    public boolean hasTicket;


    /* Constructors */

    public Node(int nodeNumber) {
        this.nodeNumber = nodeNumber;
        fullUrl = getFullURL();
        System.out.println(fullUrl);
    }

    public Node(int nodeNumber, RAIDANode node) {
        this.nodeNumber = nodeNumber;
        fullUrl = "https://" + node.urls[0].url + "/service/";
    }


    /* Methods */

    public String getFullURL() {
        return "https://raida" + (nodeNumber - 1) + ".cloudcoin.global/service/";
    }

    public void resetTicket() {
        hasTicket = false;
        ticket = "";
    }

    public void newCoin() {
        hasTicket = false;
        ticket = "";
        failsDetect = false;
    }

    public boolean isFailed() {
        return failsDetect;
    }

    /**
     * Method FIX
     * Repairs a fracked RAIDA
     *
     * @param triad three ints trusted server RAIDA numbers
     * @param m1 String ticket from the first trusted server
     * @param m2 String ticket from the second trusted server
     * @param m3 String ticket from the third trusted server
     * @param pan String proposed authenticity number (to replace the wrong AN the RAIDA has)
     * @return String status sent back from the server: sucess, fail or error.
     */
    public Response fix(int[] triad, String m1, String m2, String m3, String pan) {
        Response fixResponse = new Response();
        long before = System.currentTimeMillis();
        fixResponse.fullRequest = fullUrl + "fix?fromserver1=" + triad[0] + "&message1=" + m1 + "&fromserver2=" + triad[1] + "&message2=" + m2 + "&fromserver3=" + triad[2] + "&message3=" + m3 + "&pan=" + pan;

        try {
            fixResponse.fullResponse = Utils.getHtmlFromURL(fixResponse.fullRequest);
            long after = System.currentTimeMillis();
            long ts = after - before;
            fixResponse.milliseconds = (int) ts;

            if (fixResponse.fullResponse.contains("success")) {
                fixResponse.outcome = "success";
                fixResponse.success = true;
            } else {
                fixResponse.outcome = "fail";
                fixResponse.success = false;
            }
        } catch (Exception ex) {//quit
            fixResponse.outcome = "error";
            fixResponse.fullResponse = ex.getMessage();
            fixResponse.success = false;
        }
        return fixResponse;
    }

    /**
     * Returns an ticket from a trusted server
     *
     * @param nn  int that is the coin's Network Number
     * @param sn  int that is the coin's Serial Number
     * @param an String that is the coin's Authenticity Number (GUID)
     * @param d int that is the Denomination of the Coin
     * @return Response Object.
     */
    public CompletableFuture<Response> getTicket(int nn, int sn, String an, int d) {
        return CompletableFuture.supplyAsync(() -> {
            Response get_ticketResponse = new Response();
            get_ticketResponse.fullRequest = fullUrl + "get_ticket?nn=" + nn + "&sn=" + sn + "&an=" + an + "&pan=" + an + "&denomination=" + d;
            long before = System.currentTimeMillis();

            try {
                get_ticketResponse.fullResponse = Utils.getHtmlFromURL(get_ticketResponse.fullRequest);
                long after = System.currentTimeMillis();
                long ts = after - before;
                get_ticketResponse.milliseconds = (int) ts;

                TicketResponse response = Utils.createGson().fromJson(get_ticketResponse.fullResponse, TicketResponse.class);

                if (get_ticketResponse.fullResponse.contains("ticket")) {
                    ticket = get_ticketResponse.outcome = response.message;
                    get_ticketResponse.success = true;
                    hasTicket = true;
                } else {
                    get_ticketResponse.success = false;
                    hasTicket = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                get_ticketResponse.outcome = "error";
                get_ticketResponse.fullResponse = e.getMessage();
                get_ticketResponse.success = false;
                hasTicket = false;
            }
            return get_ticketResponse;
        });
    }
}
