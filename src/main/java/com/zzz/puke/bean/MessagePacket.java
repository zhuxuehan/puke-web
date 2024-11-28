package com.zzz.puke.bean;

public class MessagePacket {
    String localUrl;
    String webhook;
    String secret;
    ContentPacket contentPacket;

    public String getLocalUrl() {
        return localUrl;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public ContentPacket getContentPacket() {
        return contentPacket;
    }

    public void setContentPacket(ContentPacket contentPacket) {
        this.contentPacket = contentPacket;
    }


}
