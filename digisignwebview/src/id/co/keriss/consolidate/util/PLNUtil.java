package id.co.keriss.consolidate.util;

public class PLNUtil {
	public static String getStringTokenEDC(String token){
		String[] tkn=token.split("-");
		
		if(tkn!=null && tkn.length>0){
			token="";
			for(int i=0;i<tkn.length;i++) token+=tkn[i];
		}
		
		
		return token;
		
	}
	
	public static String getStringTokenXML(String token){
		String tkn="";
		int awal=0;
		for(int i=1;i<token.length();i++){
			if(i%4==0){
				tkn+=token.substring(awal,awal+4)+"-";
				awal=i;
			}
		}
		tkn+=token.substring(awal,awal+4);

		
		return tkn;
		
	}
	
	public static String getRCPLN(String rc){
		String resp="";
		if(rc.equals("14")){
			resp="NOMOR METER/IDPEL :idpel YANG ANDA MASUKAN SALAH";
		}else if(rc.equals("16")){
			resp="KONSUMEN :idpel DIBLOKIR HARAP HUBUNGI PLN";
		}else if(rc.equals("33")){
			resp="KODE PRODUK TIDAK DITEMUKAN";
		}else if(rc.equals("46")){
			resp="SALDO ANDA TIDAK CUKUP";
		}else if(rc.equals("EA")){
			resp="NOMINAL TIDAK SAMA, CEK KEMBALI DATA ANDA";
		}else{
			resp="TRANSAKSI GAGAL, RC="+rc;
		}
		return resp;
		
	}
}
