package com.example.kimjungwon.livebusker.Data;

/**
 * Created by kimjungwon on 2017-09-07.
 */
//방송 정보
public class Stream {
    private int id;
    private String title;
    private String stream_key;

    public Stream (int id, String title, String stream_key){
        this.id = id;
        this.title = title;
        this.stream_key = stream_key;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStream_key() {
        return stream_key;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStream_key(String stream_key) {
        this.stream_key = stream_key;
    }
}
