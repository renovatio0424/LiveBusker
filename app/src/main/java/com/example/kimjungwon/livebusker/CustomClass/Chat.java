package com.example.kimjungwon.livebusker.CustomClass;

/**
 * Created by kimjungwon on 2017-11-10.
 */

public class Chat {
    private String User_id;
    private String User_chat;

    public Chat(String id, String chat){
        this.User_id = id;
        this.User_chat = chat;
    }

    public String getUser_chat() {
        return User_chat;
    }

    public String getUser_id() {
        return User_id;
    }

    public void setUser_chat(String user_chat) {
        User_chat = user_chat;
    }

    public void setUser_id(String user_id) {
        User_id = user_id;
    }
}
