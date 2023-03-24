package xyz.genscode.type.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


public class User implements Serializable {
    private String id;
    private String phone;
    private String name;
    private String info;
    private String avatarUrl;
    private String avatarColor;
    private HashMap<String, Long> chats = new HashMap<>();

    public User() {
    }

    public User(String id, String phone, String name, String info, String avatarUrl, String avatarColor, HashMap<String, Long> chats) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.info = info;
        this.avatarUrl = avatarUrl;
        this.avatarColor = avatarColor;
        this.chats = chats;
    }

    public HashMap<String, Long> getChats() {
        return chats;
    }

    public void setChats(HashMap<String, Long> chats) {
        this.chats = chats;
    }

    public void addChat(String id) {
        if(this.chats != null) chats.put(id, System.currentTimeMillis());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

}
