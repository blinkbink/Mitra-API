package id.co.keriss.consolidate.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EntryUtil {
	
	private static EntryUtil instance = null;
	protected EntryUtil(){
		
	}
	public static EntryUtil getInstance() {
	      if(instance == null) {
	         instance = new EntryUtil();
	      }
	      return instance;
	}
	private String formatNumber(Long amount){
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return ""+formatter.format(amount)+"";
	}
	
	private String formatNumber(Double amount){
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return ""+formatter.format(amount)+"";
	}
	  public Date dateStart(String date){
		  Date dateVal = null;
		  SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		  try {
			dateVal = format.parse(date);
		  } catch (ParseException e) {
			e.printStackTrace();
		  }
		  System.out.println("dateVal ="+dateVal);
		  return dateVal;
	  }
	  
	  public Date dateEnd(String date){
		  Date dateVal = null;
		  SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		  try {
			dateVal = format.parse(date);
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateVal);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			dateVal=cal.getTime();
		  } catch (ParseException e) {
			e.printStackTrace();
		  }
		  System.out.println("dateEnd ="+dateVal);
		  return dateVal;
	  }
	  public String dateTitle(String from, String to){
		  String date=now();
		  if(!from.equalsIgnoreCase(to))date = from +" - "+ to;
		  return date;
	  }
	  public String now(){
		  SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		  return sdf.format(new Date());
	  }
	  public String stringDate(Date date){
		return new SimpleDateFormat("dd-MM-yyyy").format(date); 
	  }
}
