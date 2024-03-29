package scc.data.User;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents a User, as returned to the clients
 */
public class User {
	private String id;
	private String name;
	private String pwd;
	private String photoId;
	private ArrayList<String> channelIds;


	public User(String id, String name, String pwd, String photoId, ArrayList<String> channelIds) {
		super();
		this.id = id;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
		this.channelIds = channelIds;
	}

	public User(){

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
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getPhotoId() {
		return photoId;
	}
	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}
	public ArrayList<String> getChannelIds() {
		return channelIds ;
	}
	public void setChannelIds(ArrayList<String> channelIds) {
		this.channelIds = channelIds;
	}

	public void addChannel(String idChannel){
		channelIds.add(idChannel);
	}

	public void removeChannel(String idChannel){
		channelIds.remove(idChannel);
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", pwd=" + pwd + ", photoId=" + photoId + ", channelIds="
				+ channelIds.toString()+ "]";
	}

}
