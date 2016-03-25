package com.ricardotrujillo.appstore.viewmodel.event;

public class ConnectivityStatusRequest {

    int position;

    public ConnectivityStatusRequest() {

        position = -1;
    }

    public ConnectivityStatusRequest(int position) {

        this.position = position;
    }

    public int getPosition() {

        return position;
    }
}
