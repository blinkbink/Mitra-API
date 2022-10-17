package id.co.keriss.consolidate.ee;

import java.util.Date;

public class ReRegistration  implements java.io.Serializable {

		private long id;
	    private Userdata userdata;
	    private Date datetime;
	    private String selfie_photo;
     
	    public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public Userdata getUserdata() {
			return userdata;
		}
		public void setUserdata(Userdata userdata) {
			this.userdata = userdata;
		}
		public Date getDatetime() {
			return datetime;
		}
		public void setDatetime(Date datetime) {
			this.datetime = datetime;
		}
		public String getSelfie_photo() {
			return selfie_photo;
		}
		public void setSelfie_photo(String selfie_photo) {
			this.selfie_photo = selfie_photo;
		}

}