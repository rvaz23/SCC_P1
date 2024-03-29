package scc.data.Channel;

import java.util.ArrayList;
import java.util.UUID;

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
        this(c.getId(), c.getName(), c.getOwner(), c.isPublicChannel(), c.getMembers());
    }

    public ChannelDAO(ChannelCreation c) {
        this(UUID.randomUUID().toString(), c.getName(), c.getOwner(), c.isPublicChannel(), c.getMembers());
    }

    public Channel toChannel(){
        return new Channel(id,name,owner, publicChannel,members!=null ?members: new ArrayList<>(0));
    }

    public ChannelDAO(String id, String name, String owner, boolean publicChannel, ArrayList<String> members) {
        super();
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.publicChannel = publicChannel;
        this.members = members;
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

    public boolean isPublicChannel() {
        return publicChannel;
    }

    public void setPublicChannel(boolean publicChannel) {
        this.publicChannel = publicChannel;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
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
