package xyz.genscode.type.models;

public class MessageUnread {
    String messageId;
    String userId;

    public MessageUnread(String messageId, String userId) {
        this.messageId = messageId;
        this.userId = userId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
