package com.zzz.puke.bean;

import java.util.LinkedList;

public class ContentPacket {
    private String id;
    private String currTime;
    private String type;
    private String group;
    private String text;
    private LinkedList<String> images = new LinkedList<>();
    private LinkedList<String> files = new LinkedList<>();
    private LinkedList<String> audios = new LinkedList<>();
    private LinkedList<String> comments = new LinkedList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurrTime() {
        return currTime;
    }

    public void setCurrTime(String currTime) {
        this.currTime = currTime;
    }

    public LinkedList<String> getImages() {
        return images;
    }

    public void setImages(LinkedList<String> images) {
        this.images = images;
    }

    public LinkedList<String> getFiles() {
        return files;
    }

    public void setFiles(LinkedList<String> files) {
        this.files = files;
    }

    public LinkedList<String> getAudios() {
        return audios;
    }

    public void setAudios(LinkedList<String> audios) {
        this.audios = audios;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LinkedList<String> getComments() {
        return comments;
    }

    public void setComments(LinkedList<String> comments) {
        this.comments = comments;
    }
}
