package scc.data.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonProperty;
import scc.data.User.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;


/**
 * Represents a User, as stored in the database
 */
public class UserDAO {
	private String _rid;
	private String _ts;
	@BsonProperty(value = "id")
	private String id;
	@BsonProperty(value = "name")
	private String name;
	@BsonProperty(value = "pwd")
	private String pwd;
	@BsonProperty(value = "photoId")
	private String photoId;
	@BsonProperty(value = "channelIds")
	private ArrayList<String> channelIds;

	public static Document toDBObject(UserDAO user) {
		return new Document("id", user.getId())
				.append("name", user.getName())
				.append("pwd", user.getPwd())
				.append("photoId", user.getPhotoId())
				.append("channelIds", asList(user.getChannelIds()));
	}

	public UserDAO() {
	}

	public UserDAO( User u) {

		this(u.getId(), u.getName(), u.getPwd(), u.getPhotoId(), u.getChannelIds());
	}
	public UserDAO(String id, String name, String pwd, String photoId, ArrayList<String> channelIds) {
		super();
		this.id = id;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
		this.channelIds = channelIds;
	}

	public User toUser() {
		return new User( id, name, pwd, photoId, channelIds!=null ?channelIds: new ArrayList<>(0));
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
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getPhotoId() {
		return photoId;
	}
	public void setPhotoId(String photoId) {this.photoId = photoId;}


	public void addChannel(String idChannel){
		channelIds.add(idChannel);
	}

	public void removeChannel(String idChannel){
		channelIds.remove(idChannel);
	}

	public ArrayList<String> getChannelIds() {return channelIds;}

	public void setChannelIds(ArrayList<String> channelIds) {
		this.channelIds = channelIds;
	}


	@Override
	public String toString() {
		return "UserDAO [_rid=" + _rid + ", _ts=" + _ts + ", id=" + id + ", name=" + name + ", pwd=" + pwd
				+ ", photoId=" + photoId + ", channelIds=" + channelIds.toString() + "]";
	}

}
