package com.cloudcore.frackfixer.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TicketResponse {


    @Expose
    @SerializedName("server")
    public String server;
    @Expose
    @SerializedName("status")
    public String status;
    @Expose
    @SerializedName("sn")
    public String sn;
    @Expose
    @SerializedName("message")
    public String message;
    @Expose
    @SerializedName("nn")
    public String nn;
    @Expose
    @SerializedName("version")
    public String version;
    @Expose
    @SerializedName("time")
    public String time;
}
