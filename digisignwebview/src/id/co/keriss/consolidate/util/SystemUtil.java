package id.co.keriss.consolidate.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Terminal;
import id.co.keriss.consolidate.ee.Transaction;

public class SystemUtil {
    static SimpleDateFormat formatDate=new SimpleDateFormat("dd/MM/yy HH:mm");
    static SimpleDateFormat formatDate2=new SimpleDateFormat("dd/MM/yy HH:mm:ss");

	public static String findField(String message, String find, String splitakhir){
		try{
			message=message.toUpperCase();
			String[] dataSplit=message.split(find);
			String[] data= dataSplit[1].split(splitakhir);
			return data[0];
		}
		catch (Exception e) {
			return "";
		}
		
	}
	
	public static String cekMerchantTerminalType(Terminal t){
		return cekMerchantTerminalType(t.getMcode());
	}

	public static String cekMerchantTerminalType(String t){
		String type="";
		
		if(t.equals("6010")){
			type="TELLER";
		}if(t.equals("6012")){
			type="POS";
		}if(t.equals("6018")){
			type="EDC";
		}if(t.equals("6021")){
			type="WEB";
		}if(t.equals("7001")){
			type="SMS";
		}if(t.equals("8000")){
			type="XML";
		}if(t.equals("8001")){
			type="YM";
		}if(t.equals("8002")){
			type="SMS";
		}else{
			type="UNKNOWN";
		}
		
		return type;
		
	}
	
	public static String getPinYM(String pin){
    	int length=Integer.parseInt(pin.substring(0,2));
		return pin.substring(2,2+length);
	}
	
	
	/*
	 * ISO -3
	 */
	public String getPinBlock(String pin, String pan){
		String pinblock="";
		String pinblk= "";
        
        try {
			pinblk="3"+pin.length()+pin;
			pinblk=ISOUtil.padright(pinblk, 16, 'F');
			pinblock=ISOUtil.hexor(pinblk, "0000"+pan.substring(3,15));
        } catch (ISOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pinblock;
	}
	
	/*
	 * pin xml
	 */
	public String getPinBiller(String pinblk, String mid){
		String pin= ISOUtil.hexor(pinblk, "0000"+mid+"FFFF");
		int lenPin= Integer.parseInt(pin.substring(1,2));
		return pin.substring(2,2+lenPin);
	}
	

	/*DES/CBC/Nopadding
	 * mode=0 encrypt
	 * mode=1 decrypt
	 * else encrypt
	 */
	public static String doDesCBC(String hexData, String hexKey, int mode){
		String res="";

       	byte [] keyByte = ISOUtil.hex2byte(hexKey);
       	byte [] data= ISOUtil.hex2byte(hexData);
        SecretKeySpec key = new SecretKeySpec(keyByte, "DES");
        byte[] ivBytes = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        IvParameterSpec ivectorSpecv = new IvParameterSpec(ivBytes);
        byte[] plainText = new byte[data.length];
        int ptLength = 0;
		try {
	        Cipher cipher = Cipher.getInstance("DES/CBC/Nopadding");
			if(mode==1)cipher.init(Cipher.DECRYPT_MODE, key,ivectorSpecv);
			else cipher.init(Cipher.ENCRYPT_MODE, key, ivectorSpecv);
			ptLength = cipher.update(data, 0, data.length, plainText, 0);
	        res=ISOUtil.hexString(plainText);

		} catch (ShortBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;
	}
	
	/*
	 * DES/ECB/Nopadding
	 * mode=0 encrypt
	 * mode=1 decrypt
	 * else encrypt
	 */
	public String doDesECB(String hexData, String hexKey, int mode){
		String res="";

       	byte [] keyByte = ISOUtil.hex2byte(hexKey);
       	byte [] data= ISOUtil.hex2byte(hexData);
        SecretKeySpec key = new SecretKeySpec(keyByte, "DES");

        byte[] plainText = new byte[data.length];
        int ptLength = 0;
		try {
	        Cipher cipher = Cipher.getInstance("DES/ECB/Nopadding");
			if(mode==1)cipher.init(Cipher.DECRYPT_MODE, key);
			else cipher.init(Cipher.ENCRYPT_MODE, key);
			ptLength = cipher.update(data, 0, data.length, plainText, 0);
			res=ISOUtil.hexString(plainText);

		} catch (ShortBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;
	}
	
	public static boolean checkExpDate(String trk2){
		boolean ret=false;
		String data[]=trk2.split("=");
		SimpleDateFormat format=new SimpleDateFormat("yyMM");
		int time=Integer.parseInt(format.format(new Date()));
		int exp=Integer.parseInt(data[1].substring(0,4));
		if(time<exp) ret=true;		
		return ret;
		
	}
	
	/* realmn: keterangan pada log
	 * logname : default "Q2"
	 */
	public static Log getLog(String realm, String logname){
        new Log();
		return Log.getLog(logname, realm); 

	}
	
	public static String amountFormatStr(BigDecimal amt){
		if(amt==null){
			return "";
		}
 		DecimalFormat myFormatter = new DecimalFormat("###,###");
 		return myFormatter.format(amt.longValue()).replace(',', '.');
	}
	
	public static String amountDecFormatStr(BigDecimal amt){
 		DecimalFormat myFormatter = new DecimalFormat("###,###.##");
 		return myFormatter.format(amt).replace(',', '*').replace('.', ',').replace('*', '.');
	}
	
	/**
	 * 
	 * @return String[]= [0]: ip , [1] port
	 */
	public static String[] getYMRMI() {
		Properties props = new Properties();
		FileInputStream fis = null;
		Connection con = null;
			try {
				fis = new FileInputStream("cfg/ymin.properties");
				props.load(fis);
				return new String[]{props.getProperty("YM_IP"),props.getProperty("YM_PORT")};
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

	}
	
	/**
	 * 
	 * @return String[]= [0]: ip , [1] port
	 */
	public static String[] getSMSRMI() {
		Properties props = new Properties();
		FileInputStream fis = null;
		Connection con = null;
			try {
				fis = new FileInputStream("cfg/smsin.properties");
				props.load(fis);
				return new String[]{props.getProperty("SMS_IP"),props.getProperty("SMS_PORT"),props.getProperty("SMS_USER"),props.getProperty("SMS_PASS")};
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

	}
	
	/**
	 * 
	 * @return String[]= [0]: ip , [1] port
	 */
	public static String[] getYM_OUTRMI() {
		Properties props = new Properties();
		FileInputStream fis = null;
		Connection con = null;
			try {
				fis = new FileInputStream("cfg/ymout.properties");
				props.load(fis);
				return new String[]{props.getProperty("YM_IP"),props.getProperty("YM_PORT")};
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

	}

	public static String getBit63FromTrx(Transaction trx, BigDecimal saldo) {
		String res="";
		if(trx.getType().equals("SUCCESS")){
			res=formatDate.format(trx.getApprovaltime())+";ISI "+trx.getProductcode()+" KE "+trx.getCardno()+", BERHASIL ;SAL="+SystemUtil.amountFormatStr(saldo)+"/HRG="+SystemUtil.amountFormatStr(trx.getDepositamount())+"/ID="+trx.getReqid()+"/SN="+trx.getSn()+";";
		}else {
			res=RCError.getErrorMsg(trx.getType());
			res=formatDate.format(trx.getApprovaltime())+";ISI "+trx.getProductcode()+" KE "+trx.getCardno()+", "+res+" ;SAL="+SystemUtil.amountFormatStr(saldo)+"/HRG="+SystemUtil.amountFormatStr(trx.getDepositamount())+"/ID="+trx.getReqid()+"/SN="+trx.getSn()+";";
		}
		return res;
	}
	
	public static String changeFormatDate(Date date){
		return formatDate2.format(date);
	}
	
	public static String randomNumber() {
		java.util.Random generator = new java.util.Random();
		generator.setSeed(System.currentTimeMillis());
		int i = generator.nextInt(1000000) % 1000000;
		
		java.text.DecimalFormat f = new java.text.DecimalFormat("000000");
		return f.format(i);
	}
	
	public static String getExp(String docName) throws Exception {
		String data=docName+"|"+(new Date().getTime()+(1000*60*30));
		String ss=AESEncryption.encryptDoc(data);
		return ss;
		
	}
	
	public static String getExp(String docName, Date date) throws Exception {
		String data=docName+"|"+date.getTime();
		String ss=AESEncryption.encryptDoc(data);
		return ss;
		
	}
	
}
