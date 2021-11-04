package scc.data;

import java.util.ArrayList;
import java.util.Arrays;

public class ChannelDAO {
    private String _rid;
    private String _ts;
    private String id;
    private String name;
    private String owner;
    private boolean isPublic;
    private ArrayList<String> memberIds;

    public ChannelDAO() {
    }

    public ChannelDAO( Channel c) {
        this(c.getId(), c.getName(), c.getOwner(), c.isPublic(), c.getMemberIds());
    }

    public Channel toChannel(){
        return new Channel(id,name,owner,isPublic,memberIds!=null ?memberIds: new ArrayList<>(0));
    }

    public ChannelDAO(String id, String name,String owner, boolean isPublic, ArrayList<String> memberIds) {
        super();
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.isPublic = isPublic;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void addUserToChannel(String id){
        memberIds.add(id);
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
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
                ", isPublic=" + isPublic +
                ", memberIds=" + memberIds.toString() +
                '}';
    }
}
