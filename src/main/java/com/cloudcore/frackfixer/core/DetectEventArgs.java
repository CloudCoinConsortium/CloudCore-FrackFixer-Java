package com.cloudcore.frackfixer.core;

public class DetectEventArgs {


    private CloudCoin detectedCoin;

    public DetectEventArgs(CloudCoin coin) {
        this.detectedCoin = coin;
    }

    public CloudCoin DetectedCoin() {
        return detectedCoin;
    }
}
