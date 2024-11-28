package com.zzz.puke.bean;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;


@Entity
@Table(name = "puke_kv")
public class PukeKv {

    @Id
    private long id;

    @Column(name = ("zf_user"))
    private String zfUser;

    @Column(name = ("circleid"))
    private String circleid;

    @Column(name = ("x_access_token"), columnDefinition = "TEXT")
    private String xAccessToken;

    @Column(name = ("x_client_key"))
    private String xClientKey;

    @Column(name = ("lastid"))
    private String lastid;

    @Column(name = ("last_send_time"))
    private Date lastSendTime;

    @Column(name = ("interval_time"))
    private int intervalTime;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getZfUser() {
        return zfUser;
    }

    public void setZfUser(String zfUser) {
        this.zfUser = zfUser;
    }

    public String getCircleid() {
        return circleid;
    }

    public void setCircleid(String circleid) {
        this.circleid = circleid;
    }

    public String getxAccessToken() {
        return xAccessToken;
    }

    public void setxAccessToken(String xAccessToken) {
        this.xAccessToken = xAccessToken;
    }

    public String getxClientKey() {
        return xClientKey;
    }

    public void setxClientKey(String xClientKey) {
        this.xClientKey = xClientKey;
    }

    public String getLastid() {
        return lastid;
    }

    public void setLastid(String lastid) {
        this.lastid = lastid;
    }

    public Date getLastSendTime() {
        return lastSendTime;
    }

    public void setLastSendTime(Date lastSendTime) {
        this.lastSendTime = lastSendTime;
    }

    public int getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }
}
