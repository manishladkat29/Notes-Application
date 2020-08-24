package com.example.group03_inclass09;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Messages implements Serializable {
    String messageId, userID, text;

    public Messages(String messageId, String userID, String text) {
        this.messageId = messageId;
        this.userID = userID;
        this.text = text;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
