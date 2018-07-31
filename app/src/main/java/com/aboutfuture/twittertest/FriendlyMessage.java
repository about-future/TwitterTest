package com.aboutfuture.twittertest;

public class FriendlyMessage {
    private String name;
    private String text;

    public FriendlyMessage() {
    }

    public FriendlyMessage(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
