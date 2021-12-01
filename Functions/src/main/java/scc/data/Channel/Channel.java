package scc.data.Channel;

import java.util.ArrayList;

public class Channel {
    private String id;
    private String name;
    private String owner;
    private boolean publicChannel;
    private ArrayList<String> members;

    public Channel(String id, String name, String owner, boolean publicChannel, ArrayList<String> members) {
        super();
        this.id = id;
        this.name = name;
        this.owner=owner;
        this.publicChannel = publicChannel;
        this.members = members;
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
        return "Channel{" +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", isPublic=" + publicChannel +
                ", memberIds=" + members.toString() +
                '}';
    }
}
