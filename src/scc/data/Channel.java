package scc.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Channel {
    private String id;
    private String name;
    private String owner;
    private boolean publicChannel;
    private ArrayList<String> members;

    public Channel(String id, String name, String owner, boolean publicChannel, ArrayList<String> memberIds) {
        super();
        this.id = id;
        this.name = name;
        this.owner=owner;
        this.publicChannel = publicChannel;
        this.members = memberIds;
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
        return publicChannel;
    }

    public void setIsPublic(boolean isPublic) {
        this.publicChannel = isPublic;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ArrayList<String> getMemberIds() {
        return members;
    }

    public void setMemberIds(ArrayList<String> memberIds) {
        this.members = memberIds;
    }

    @Override
    public String toString() {
        return "Channel{" +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", isPublic=" + publicChannel +
                ", memberIds=" + members.toString() +
                '}';
    }
}
