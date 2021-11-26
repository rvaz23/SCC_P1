package scc.data;

import java.util.ArrayList;

public class ChannelDAO {
    private String _rid;
    private String _ts;
    private String id;
    private String name;
    private String owner;
    private boolean channelPublic;
    private ArrayList<String> memberIds;

    public ChannelDAO() {
    }

    public ChannelDAO( Channel c) {
        this(c.getId(), c.getName(), c.getOwner(), c.isChannelPublic(), c.getMemberIds());
    }

    public ChannelDAO(String id, ChannelCreation c) {
        this(id, c.getName(), c.getOwner(), c.isChannelPublic(), c.getMemberIds());
    }

    public Channel toChannel(){
        return new Channel(id,name,owner, channelPublic,memberIds!=null ?memberIds: new ArrayList<>(0));
    }

    public ChannelDAO(String id, String name, String owner, boolean channelPublic, ArrayList<String> memberIds) {
        super();
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.channelPublic = channelPublic;
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

    public boolean isChannelPublic() {
        return channelPublic;
    }

    public void addUserToChannel(String id){
        memberIds.add(id);
    }

    public void setIsPublic(boolean isPublic) {
        this.channelPublic = isPublic;
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
                ", isPublic=" + channelPublic +
                ", memberIds=" + memberIds.toString() +
                '}';
    }
}
