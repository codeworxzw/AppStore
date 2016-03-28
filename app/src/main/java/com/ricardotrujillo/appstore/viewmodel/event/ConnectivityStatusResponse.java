package com.ricardotrujillo.appstore.viewmodel.event;

public class ConnectivityStatusResponse {

    boolean status;

    public ConnectivityStatusResponse() {

    }

    public ConnectivityStatusResponse(boolean status) {

        this.status = status;
    }

    public boolean isConnected() {

        return status;
    }
}
