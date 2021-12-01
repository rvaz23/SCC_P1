package scc.cache;

public class Session {

	private String uid;
	private String user;
	
	

	public Session(String uid, String user) {
		super();
		this.uid = uid;
		this.user = user;
	}
	
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	
	
}
