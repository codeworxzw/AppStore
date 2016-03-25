package com.ricardotrujillo.appstore.viewmodel.event;

public class RequestStoreEvent {

    int position;

    public RequestStoreEvent() {

    }

    public RequestStoreEvent(int position) {

        this.position = position;
    }

    public int getPosition() {

        return this.position;
    }
}
