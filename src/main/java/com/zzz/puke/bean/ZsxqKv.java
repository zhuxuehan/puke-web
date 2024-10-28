package com.zzz.puke.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "zsxq_kv")
public class ZsxqKv {

    @Id
    private long id;

    @Column(name = ("zsxq_group"))
    private String zsxqGroup;

    @Column(name = ("zsxq_cookie"), columnDefinition = "TEXT")
    private String zsxqCookie;

    @Column(name = ("zf_user"))
    private String zfUser;

    @Column(name = "zsxq_last_time")
    private String zsxqLastTime;

    @Column(name = "zsxq_webhook")
    private String zsxqWebhook;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getZsxqGroup() {
        return zsxqGroup;
    }

    public void setZsxqGroup(String zsxqGroup) {
        this.zsxqGroup = zsxqGroup;
    }

    public String getZsxqCookie() {
        return zsxqCookie;
    }

    public void setZsxqCookie(String zsxqCookie) {
        this.zsxqCookie = zsxqCookie;
    }

    public String getZfUser() {
        return zfUser;
    }

    public void setZfUser(String zfUser) {
        this.zfUser = zfUser;
    }

    public String getZsxqLastTime() {
        return zsxqLastTime;
    }

    public void setZsxqLastTime(String zsxqLastTime) {
        this.zsxqLastTime = zsxqLastTime;
    }

    public String getZsxqWebhook() {
        return zsxqWebhook;
    }

    public void setZsxqWebhook(String zsxqWebhook) {
        this.zsxqWebhook = zsxqWebhook;
    }
}
