package com.ricardotrujillo.appstore.viewmodel.event;

public class ConnectivityStatusRequest {

    int classType = -1;

    public ConnectivityStatusRequest() {

    }

    public ConnectivityStatusRequest(int classType) {

        this.classType = classType;
    }

    public int getClassType() {

        return this.classType;
    }
}
