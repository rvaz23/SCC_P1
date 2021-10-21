package scc.data;

import java.util.Arrays;

public class MessageDAO {
    private String _rid;
    private String _ts;
    private String id;
    private String senderId;
    private String channelId;
    private String text;
    private String mediaId;
    private String repliesToId;

    public MessageDAO() {
    }

    public MessageDAO( Message m) {
        this(m.getId(), m.getSenderId(), m.getChannelId(), m.getText(), m.getMediaId(), m.getRepliesToId());
    }

    public Message toMessage() {
        return new Message(id,senderId,channelId,text,mediaId!=null ? mediaId :"", repliesToId!=null ? repliesToId :"");
    }

    public MessageDAO(String id, String senderId, String channelId, String text, String mediaId, String repliesToId) {
        this.id = id;
        this.senderId = senderId;
        this.channelId = channelId;
        this.text = text;
        this.mediaId = mediaId;
        this.repliesToId = repliesToId;
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
        return "MessageDAO{" +
                "_rid='" + _rid + '\'' +
                ", _ts='" + _ts + '\'' +
                ", id='" + id + '\'' +
                ", senderId='" + senderId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", text='" + text + '\'' +
                ", mediaId='" + mediaId + '\'' +
                ", repliesToId='" + repliesToId + '\'' +
                '}';
    }
}
