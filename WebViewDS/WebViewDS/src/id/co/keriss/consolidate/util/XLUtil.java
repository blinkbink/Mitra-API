package id.co.keriss.consolidate.util;

public class XLUtil {
	
	public static String unpackFromSpace(String var) {
		String tmp = null;
		int index = var.indexOf(' ');
		if(index != -1) {
			System.out.println("index "+index);
			tmp = var.substring(0, index);
			System.out.println("subs_msisdn "+tmp);
		} else {
			tmp = var;
			System.out.println("subs_msisdn "+tmp);
		}
		
		return tmp;
	}
}
