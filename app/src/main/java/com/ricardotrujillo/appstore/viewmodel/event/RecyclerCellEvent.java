package com.ricardotrujillo.appstore.viewmodel.event;

public class RecyclerCellEvent {

    String string;
    String field;

    public RecyclerCellEvent(String string, String field) {

        this.string = string;
        this.field = field;
    }

    public String getString() {

        return string;
    }

    public String getField() {

        return field;
    }
}
