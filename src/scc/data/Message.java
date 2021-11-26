package scc.data;

import scc.utils.Hash;

import java.util.Random;
import java.util.UUID;

public class Message {
    private String id;
    private String senderId;
    private String channelId;
    private String text;
    private String mediaId;
    private String repliesToId;

    public Message(String id, String senderId, String channelId, String text, String mediaId, String repliesToId) {
        super();
        this.id = id;
        this.senderId = senderId;
        this.channelId = channelId;
        this.text = text;
        this.mediaId = mediaId;
        this.repliesToId = repliesToId;
    }

    public Message(String senderId, String channelId, String text, String mediaId) {
        super();
        String newId = UUID.randomUUID().toString();

        this.id = newId;
        this.senderId = senderId;
        this.channelId = channelId;
        this.text = text;
        this.mediaId = mediaId;
        this.repliesToId = null;
    }
    public Message() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getRepliesToId() {
        return repliesToId;
    }

    public void setRepliesToId(String repliesToId) {
        this.repliesToId = repliesToId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", senderId='" + senderId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", text='" + text + '\'' +
                ", mediaId='" + mediaId + '\'' +
                ", repliesToId='" + repliesToId + '\'' +
                '}';
    }
}

