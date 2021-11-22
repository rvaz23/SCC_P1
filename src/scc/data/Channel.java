package scc.data;

import java.util.ArrayList;

public class Channel {
    private String id;
    private String name;
    private String owner;
    private boolean channelPublic;
    private ArrayList<String> memberIds;

    public Channel(String id, String name, String owner, boolean channelPublic, ArrayList<String> memberIds) {
        super();
        this.id = id;
        this.name = name;
        this.owner=owner;
        this.channelPublic = channelPublic;
        this.memberIds = memberIds;
    }

    public Channel(){

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

    public boolean isChannelPublic() {
        return channelPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.channelPublic = isPublic;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ArrayList<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(ArrayList<String> memberIds) {
        this.memberIds = memberIds;
    }

    @Override
    public String toString() {
        return "Channel{" +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", isPublic=" + channelPublic +
                ", memberIds=" + memberIds.toString() +
                '}';
    }
}
