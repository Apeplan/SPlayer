package com.apeplan.splayer.domain;

import java.io.Serializable;

/**
 * describe:
 *
 * @author Apeplan
 * @date 2017/3/12
 * @email hanzx1024@gmail.com
 */

public class MediaEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 多媒体文件名称
     */
    private String title;
    /**
     * 多媒体文件时长
     */
    private String duration;
    /**
     * 多媒体文件大小
     */
    private String size;
    /**
     * 多媒体文件长创建时间
     */
    private String data;
    /**
     * 艺术家
     */
    private String artist;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
