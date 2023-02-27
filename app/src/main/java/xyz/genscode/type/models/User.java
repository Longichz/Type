package xyz.genscode.type.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;


public class User implements Serializable {
    private String id;
    private String phone;
    private String name;
    private String info;
    private String avatarUrl;
    private String avatarColor;
    private ArrayList<String> chats = new ArrayList<>();

    public User() {
    }

    public User(String id, String phone, String name, String info, String avatarUrl, String avatarColor, ArrayList<String> chats) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.info = info;
        this.avatarUrl = avatarUrl;
        this.avatarColor = avatarColor;
        this.chats = chats;
    }

    public ArrayList<String> getChats() {
        return chats;
    }

    public void setChats(ArrayList<String> chats) {
        this.chats = chats;
    }

    public void addChat(String id) {
        if(this.chats != null) chats.add(id);
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
