package com.zzz.puke.bean;

import com.zzz.puke.enums.ContentChannel;

import java.util.LinkedList;
import java.util.List;

public class ContentPacket {
    private String id;
    private String type;
    private String group;
    private String text;
    private String currTime;
    private ContentChannel contentChannel;
    private List<String> images = new LinkedList<>();
    private List<String> files = new LinkedList<>();
    private List<String> audios = new LinkedList<>();
    private List<String> comments = new LinkedList<>();

    public ContentChannel getContentChannel() {
        return contentChannel;
    }

    public void setContentChannel(ContentChannel contentChannel) {
        this.contentChannel = contentChannel;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public List<String> getAudios() {
        return audios;
    }

    public void setAudios(List<String> audios) {
        this.audios = audios;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }
}
