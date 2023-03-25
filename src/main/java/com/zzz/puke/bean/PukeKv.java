package com.zzz.puke.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "puke_kv")
public class PukeKv {

    @Id
    private long id;

    @Column(name = ("puke_k"))
    private String pukeK;

    @Column(name = ("puke_v"), columnDefinition = "TEXT")
    private String pukeV;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getPukeK() {
        return pukeK;
    }

    public void setPukeK(String pukeK) {
        this.pukeK = pukeK;
    }


    public String getPukeV() {
        return pukeV;
    }

    public void setPukeV(String pukeV) {
        this.pukeV = pukeV;
    }

    @Override
    public String toString() {
        return "PukeKv{" +
                "id=" + id +
                ", pukeK='" + pukeK + '\'' +
                ", pukeV='" + pukeV + '\'' +
                '}';
    }
}
