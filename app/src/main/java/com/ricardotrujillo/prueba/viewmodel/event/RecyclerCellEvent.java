package com.ricardotrujillo.prueba.viewmodel.event;

import android.view.View;

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
