package com.zzz.puke.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

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

    @Column(name = "interval_time")
    private int intervalTime;

    @Column(name = "last_send_time")
    private Date lastSendTime;


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

    public int getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }

    public Date getLastSendTime() {
        return lastSendTime;
    }

    public void setLastSendTime(Date lastSendTime) {
        this.lastSendTime = lastSendTime;
    }
}
