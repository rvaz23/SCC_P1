package scc.data.Message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class Message {
    private String id;
    private String user;
    private String channel;
    private String text;
    private String imageId="";
    private String replyTo="";


    @JsonCreator
    public Message(@JsonProperty("id")String id,@JsonProperty("user") String senderId,@JsonProperty("channel") String channelId,@JsonProperty("text") String text
            ,@JsonProperty("imageId") String mediaId,@JsonProperty("replyTo") String repliesToId) {
        super();
        if (id!=null){
            this.id = id;
        }else{
            this.id=UUID.randomUUID().toString();
        }
        this.user = senderId;
        this.channel = channelId;
        this.text = text;
        if (mediaId!=null)
        this.imageId = mediaId;
        if (repliesToId!=null)
        this.replyTo = repliesToId;
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

