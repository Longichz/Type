package xyz.genscode.type.models;

public class Message {
    private String userId;
    private String id;
    private long timestamp;
    private String text;
    private String imageUrl;
    private boolean edited = false;
    private boolean read = false;

    public Message() {
    }

    public Message(String userId, String id, long timestamp, String text, String imageUrl) {
        this.userId = userId;
        this.id = id;
        this.timestamp = timestamp;
        this.text = text;
        this.imageUrl = imageUrl;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean readied) {
        this.read = readied;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
