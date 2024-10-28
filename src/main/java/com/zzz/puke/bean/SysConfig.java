package com.zzz.puke.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sys_config")
public class SysConfig {

    @Id
    private long id;

    @Column(name = ("sys_k"))
    private String sysK;

    @Column(name = ("sys_v"))
    private String sysV;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSysK() {
        return sysK;
    }

    public void setSysK(String sysK) {
        this.sysK = sysK;
    }

    public String getSysV() {
        return sysV;
    }

    public void setSysV(String sysV) {
        this.sysV = sysV;
    }
}
