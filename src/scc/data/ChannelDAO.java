package scc.data;

import java.util.ArrayList;

public class ChannelDAO {
    private String _rid;
    private String _ts;
    private String id;
    private String name;
    private String owner;
    private boolean publicChannel;
    private ArrayList<String> members;

    public ChannelDAO() {
    }

    public ChannelDAO( Channel c) {
        this(c.getId(), c.getName(), c.getOwner(), c.isChannelPublic(), c.getMemberIds());
    }

    public ChannelDAO(String id, ChannelCreation c) {
        this(id, c.getName(), c.getOwner(), c.isChannelPublic(), c.getMemberIds());
    }

    public Channel toChannel(){
        return new Channel(id,name,owner, publicChannel,members!=null ?members: new ArrayList<>(0));
    }

    public ChannelDAO(String id, String name, String owner, boolean channelPublic, ArrayList<String> memberIds) {
        super();
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.publicChannel = channelPublic;
        this.members = memberIds;
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
        return publicChannel;
    }

    public void setIsPublic(boolean isPublic) {
        this.publicChannel = isPublic;
    }

    public ArrayList<String> getMemberIds() {
        return members;
    }

    public void setMemberIds(ArrayList<String> memberIds) {
        this.members = memberIds;
    }

    @Override
    public String toString() {
        return "ChannelDAO{" +
                "_rid='" + _rid + '\'' +
                ", _ts='" + _ts + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", isPublic=" + publicChannel +
                ", memberIds=" + members.toString() +
                '}';
    }
}
