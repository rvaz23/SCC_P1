package scc.data.Channel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ChannelCreation {

    private String name;
    private String owner;
    private boolean publicChannel;
    private ArrayList<String> members;

    public ChannelCreation(String name, String owner, boolean publicChannel, ArrayList<String> members) {
        super();
        this.name = name;
        this.owner=owner;
        this.publicChannel = publicChannel;
        this.members = members;
    }

    public ChannelCreation(){

    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublicChannel() {
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

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "ChannelCreation{" +
                ", name='" + name + '\'' +
                ", isPublic=" + publicChannel +
                ", memberIds=" + members.toString() +
                '}';
    }
}
