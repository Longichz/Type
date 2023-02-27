package xyz.genscode.type.models;

import java.util.ArrayList;
import java.util.HashMap;

public class Chat {

    private String chatId;
    private Message lastMessage;
    private ArrayList<String> users;
    private HashMap<String, Message> messages;

    public Chat() {
    }

    public Chat(String chatId, String chatName, Message lastMessage, ArrayList<String> users, HashMap<String, Message> messages) {
        this.chatId = chatId;
        this.lastMessage = lastMessage;
        this.users = users;
        this.messages = messages;
    }

    public HashMap<String, Message> getMessages() {
        return messages;
    }

    public void setMessages(HashMap<String, Message> messages) {
        this.messages = messages;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }
}
