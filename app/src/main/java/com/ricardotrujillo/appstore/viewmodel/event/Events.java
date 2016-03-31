package com.ricardotrujillo.appstore.viewmodel.event;

public class Events {

    public static class RequestStoreEvent {

        int classType;

        public RequestStoreEvent(int classType) {

            this.classType = classType;
        }

        public int getClassType() {

            return this.classType;
        }
    }

    public static class FetchedStoreDataEvent {
    }

    public static class RxFetchedStoreDataEvent {
    }

    public static class ConnectivityStatusResponse {

        boolean status;
        int classType;

        public ConnectivityStatusResponse(boolean status) {

            this.status = status;
        }

        public ConnectivityStatusResponse(int classType, boolean status) {

            this.status = status;
            this.classType = classType;
        }

        public boolean isConnected() {

            return status;
        }

        public int getClassType() {

            return this.classType;
        }
    }

    public static class ConnectivityStatusResponse2 {

        boolean status;
        int classType;

        public ConnectivityStatusResponse2(boolean status) {

            this.status = status;
        }

        public boolean isConnected() {

            return status;
        }
    }

    public static class ConnectivityStatusRequest {

        int classType;

        public ConnectivityStatusRequest(int classType) {

            this.classType = classType;
        }

        public int getClassType() {

            return this.classType;
        }
    }

    public static class ConnectivityStatusRequest2 {
    }
}
