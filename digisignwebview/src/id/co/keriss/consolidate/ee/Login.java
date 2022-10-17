package id.co.keriss.consolidate.ee;

import java.io.Serializable;

public class Login implements Serializable {
    private long id;
    private String username;
    private String password;
    private String imei;
    
    public Login() {
      
    }
  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }
  public String getPassword() {
    return password;
  }
  public void setPassword(String password) {
    this.password = password;
  }
  public String getImei() {
    return imei;
  }
  public void setImei(String imei) {
    this.imei = imei;
  }
    
    
    
    
}
