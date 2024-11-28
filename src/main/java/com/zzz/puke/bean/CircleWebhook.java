package com.zzz.puke.bean;

import com.zzz.puke.enums.ContentChannel;
import com.zzz.puke.enums.WebhookChannel;

import javax.persistence.*;

@Entity
@Table(name = "circle_webhook")
public class CircleWebhook {

    @Id
    private int id;

    @Column(name = ("zf_user"))
    private String zfUser;

    @Column(name = ("circle_id"))
    private String circleId;

    @Column(name = ("webhook"))
    private String webhook;

    @Enumerated(EnumType.STRING)
    @Column(name = ("content_channel"))
    private ContentChannel contentChannel;

    @Enumerated(EnumType.STRING)
    @Column(name = ("webhook_channel"))
    private WebhookChannel webhookChannel;

    @Column(name = ("secret"))
    private String secret;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getZfUser() {
        return zfUser;
    }

    public void setZfUser(String zfUser) {
        this.zfUser = zfUser;
    }


    public String getCircleId() {
        return circleId;
    }

    public void setCircleId(String circleId) {
        this.circleId = circleId;
    }


    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }


    public WebhookChannel getWebhookChannel() {
        return webhookChannel;
    }

    public void setWebhookChannel(WebhookChannel webhookChannel) {
        this.webhookChannel = webhookChannel;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public ContentChannel getContentChannel() {
        return contentChannel;
    }

    public void setContentChannel(ContentChannel contentChannel) {
        this.contentChannel = contentChannel;
    }
}
