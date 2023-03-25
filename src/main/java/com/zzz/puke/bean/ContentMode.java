package com.zzz.puke.bean;

import java.util.List;

public class ContentMode {
    private String id;
    private String time;
    private String content;
    private List<String> images;
    private List<String> audios;
    private List<String> comments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
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
