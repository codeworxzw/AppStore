package com.ricardotrujillo.appstore.viewmodel.event;

public class ConnectivityStatusResponse {

    boolean status;
    int postion = -1;

    public ConnectivityStatusResponse() {

    }

    public ConnectivityStatusResponse(boolean status, int position) {

        this.status = status;
        this.postion = position;
    }

    public boolean isConnected() {

        return status;
    }

    public int getPostion() {

        return postion;
    }
}
