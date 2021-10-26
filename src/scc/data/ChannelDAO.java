package scc.data;

import java.util.ArrayList;
import java.util.Arrays;

public class ChannelDAO {
    private String _rid;
    private String _ts;
    private String id;
    private String name;
    private boolean status;
    private ArrayList<String> memberIds;

    public ChannelDAO() {
    }

    public ChannelDAO( Channel c) {
        this(c.getId(), c.getName(), c.getStatus(), c.getMemberIds());
    }

    public Channel toChannel(){
        return new Channel(id,name,status,memberIds!=null ?memberIds: new ArrayList<>(0));
    }

    public ChannelDAO(String id, String name, boolean status, ArrayList<String> memberIds) {
        super();
        this.id = id;
        this.name = name;
        this.status = status;
        this.memberIds = memberIds;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStatus() {
        return status;
    }

    public void addUserToChannel(String id){
        memberIds.add(id);
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public ArrayList<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(ArrayList<String> memberIds) {
        this.memberIds = memberIds;
    }

    @Override
    public String toString() {
        return "ChannelDAO{" +
                "_rid='" + _rid + '\'' +
                ", _ts='" + _ts + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", memberIds=" + memberIds.toString() +
                '}';
    }
}
