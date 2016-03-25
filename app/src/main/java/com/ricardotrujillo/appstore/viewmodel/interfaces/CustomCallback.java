package com.ricardotrujillo.appstore.viewmodel.interfaces;

public interface CustomCallback {

    void onSuccess();

    void onError();

    class EmptyCallback implements CustomCallback {

        @Override public void onSuccess() {

        }

        @Override public void onError() {

        }
    }
}
