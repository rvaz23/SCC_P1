package scc.data;

public class Login {

    private String user;
    private String pwd;

    public Login(String user, String pwd) {
        this.user = user;
        this.pwd = pwd;
    }
    
    public Login() {
    	
    }


    public String getUser() {
        return user;
    }

    public void setuser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
