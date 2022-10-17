package api;



import java.io.Serializable;
import java.util.Date;

public class MsgYM implements Serializable{
	
	private Long id;
	private String from;
	private String to;
	private String msg;
	private String stn;
	private String nom;
	private String nohp;
	private String msgResp;
	private String msgRespAsli;
	private Date respTime;
	private Date reqTime;
	
	public String getMsgRespAsli() {
		return msgRespAsli;
	}
	public void setMsgRespAsli(String msgRespAsli) {
		this.msgRespAsli = msgRespAsli;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getNohp() {
		return nohp;
	}
	public void setNohp(String nohp) {
		this.nohp = nohp;
	}
	public String getMsgResp() {
		return msgResp;
	}
	public void setMsgResp(String msgResp) {
		this.msgResp = msgResp;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getStn() {
		return stn;
	}
	public void setStn(String stn) {
		this.stn = stn;
	}
	public Date getRespTime() {
		return respTime;
	}
	public void setRespTime(Date respTime) {
		this.respTime = respTime;
	}
	public Date getReqTime() {
		return reqTime;
	}
	public void setReqTime(Date reqTime) {
		this.reqTime = reqTime;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	

}
