package com.zzz.puke.bean;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tokens")
public class Tokens {

    @Id
    private long id;

    @Column(name = ("x_client_token"))
    private String xClientToken;


    public void setId(long id) {
        this.id = id;
    }


    public String getXClientToken() {
        return xClientToken;
    }

    public void setXClientToken(String xClientToken) {
        this.xClientToken = xClientToken;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
