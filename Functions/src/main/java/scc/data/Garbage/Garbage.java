package scc.data.Garbage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Garbage {

    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("internal_id")
    private String internal_id;


/*
    @JsonCreator
    public Garbage(@JsonProperty("id")String id,@JsonProperty("type")String type,@JsonProperty("internal_id")String internal_id) {
        this.id = id;
        this.type = type;
        this.internal_id = internal_id;
    }
*/
    public Garbage(String type, String internal_id) {
        this.id= UUID.randomUUID().toString();
        this.type = type;
        this.internal_id = internal_id;
    }

    public Garbage(){

    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("internal_id")
    public String getInternal_id() {
        return internal_id;
    }

    @JsonProperty("internal_id")
    public void setInternal_id(String internal_id) {
        this.internal_id = internal_id;
    }
}
