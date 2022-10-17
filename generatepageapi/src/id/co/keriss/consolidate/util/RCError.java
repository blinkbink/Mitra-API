package id.co.keriss.consolidate.util;

public class RCError {
	public static String retRC(String message) {
		if(message.equals("INVALID-WRONG-PIN")) {
			return "11";
		}
		if(message.equals("INVALID-SUBS-NOT-FOUND")) {
			return "12";
		}
		if(message.equals("INVALID-TOKEN")) {
			return "13";
		}
		if(message.equals("INVALID-PARTNER-ID")) {
			return "14";
		}
		if(message.equals("INVALID-MERCHANT-NOT-FOUND")) {
			return "15";
		}
		if(message.equals("INVALID-CUSTOMER-MSISDN-EXIST")) {
			return "16";
		}
		if(message.equals("INVALID-BALANCE-OVERLIMIT")) {
			return "17";
		}
		if(message.equals("INVALID-TRX-OVERLIMIT")) {
			return "18";
		}

		return "00";
	}
	
	
	public static String getErrorMsg(String message) {
		String	msg=message;
		String res="";
		if(msg.contains("RC")) msg=msg.substring(2);
		switch (msg) {
		case "PENDING":
			res="Transaksi sedang di PROSES, Mohon JANGAN DIULANG.";
			break;

		case "46":
			res="Saldo tidak mencukupi";
			break;

			
		default:
			res="Transaksi tidak dapat dilakukan";
		}

		return res;
	}
}
