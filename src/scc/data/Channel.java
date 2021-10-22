package scc.data;

import java.util.Arrays;

public class Channel {
    private String id;
    private String name;
    private boolean status;
    private String[] memberIds;

    public Channel(String id, String name, boolean status, String[] memberIds) {
        super();
        this.id = id;
        this.name = name;
        this.status = status;
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

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String[] getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(String[] memberIds) {
        this.memberIds = memberIds;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", memberIds=" + Arrays.toString(memberIds) +
                '}';
    }
}
