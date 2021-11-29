package scc.data.Garbage;

import java.util.UUID;

public class Garbage {

    private String id;
    private String type;
    private String internal_id;

    public Garbage(String type, String internal_id) {
        this.id= UUID.randomUUID().toString();
        this.type = type;
        this.internal_id = internal_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInternal_id() {
        return internal_id;
    }

    public void setInternal_id(String internal_id) {
        this.internal_id = internal_id;
    }
}
