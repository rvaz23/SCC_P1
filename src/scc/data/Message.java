package scc.data;

import java.util.UUID;

public class Message {
    private String id;
    private String user;
    private String channel;
    private String text;
    private String imageId;
    private String replyTo;

    public Message(String id, String senderId, String channelId, String text, String mediaId, String repliesToId) {
        super();
        this.id = id;
        this.user = senderId;
        this.channel = channelId;
        this.text = text;
        this.imageId = mediaId;
        this.replyTo = repliesToId;
    }

    public Message(String senderId, String channelId, String text, String mediaId) {
        super();
        String newId = UUID.randomUUID().toString();

        this.id = newId;
        this.user = senderId;
        this.channel = channelId;
        this.text = text;
        this.imageId = mediaId;
        this.replyTo = null;
    }
    public Message() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", senderId='" + user + '\'' +
                ", channelId='" + channel + '\'' +
                ", text='" + text + '\'' +
                ", mediaId='" + imageId + '\'' +
                ", repliesToId='" + replyTo + '\'' +
                '}';
    }
}

