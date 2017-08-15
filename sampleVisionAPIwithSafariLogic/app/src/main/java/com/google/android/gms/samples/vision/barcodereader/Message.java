package com.google.android.gms.samples.vision.barcodereader;

/**
 * Created by Katherine on 29/7/17.
 */

public class Message {

    String messageName;
    String messageContent;

    public Message(String messageName, String messageContent) {
        this.messageName = messageName;
        this.messageContent = messageContent;
    }

    public String getMessageName() {
        return messageName;
    }

    public String getMessageContent() {
        return messageContent;
    }
}
