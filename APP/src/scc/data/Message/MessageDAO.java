package scc.data.Message;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import scc.data.Message.Message;

public class MessageDAO {
    private String _rid;
    private String _ts;
    private String id;
    private String user;
    private String channel;
    private String text;
    private String imageId;
    private String replyTo;


    public static final DBObject toDBObject(MessageDAO message) {
        return new BasicDBObject("id", message.getId())
                .append("user", message.getUser())
                .append("channel", message.getChannel())
                .append("text", message.getText())
                .append("imageId", message.getImageId())
                .append("replyTo", message.getReplyTo());
    }


    public MessageDAO() {
    }

    public MessageDAO( Message m) {
        this(m.getId(), m.getUser(), m.getChannel(), m.getText(), m.getImageId(), m.getReplyTo());
    }

    public Message toMessage() {
        return new Message(id, user,channel,text, imageId !=null ? imageId :"", replyTo !=null ? replyTo :"");
    }

    public MessageDAO(String id, String senderId, String channelId, String text, String mediaId, String repliesToId) {
        super();
        this.id = id;
        this.user = senderId;
        this.channel = channelId;
        this.text = text;
        this.imageId = mediaId;
        this.replyTo = repliesToId;
    }

    public String get_rid() {
        return _rid;
    }

    public void set_rid(String _rid) {
        this._rid = _rid;
    }

    public String get_ts() {
        return _ts;
    }

    public void set_ts(String _ts) {
        this._ts = _ts;
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

    public String getChannelId() {
        return channel;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }


    public void setChannelId(String channelId) {
        this.channel = channelId;
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
        return "MessageDAO{" +
                "_rid='" + _rid + '\'' +
                ", _ts='" + _ts + '\'' +
                ", id='" + id + '\'' +
                ", senderId='" + user + '\'' +
                ", channelId='" + channel + '\'' +
                ", text='" + text + '\'' +
                ", mediaId='" + imageId + '\'' +
                ", repliesToId='" + replyTo + '\'' +
                '}';
    }
}
