package id.co.keriss.consolidate.util;

public class SignerModalku {

	public String[] getLocation(String user) {
		String loc[]= new String[]{"0","0","0","0","0"};
		
		if(user.equals("pasangan-penjamin")) {
			loc=new String[]{"3","178.4000244140625","462","308.4000244140625","527"};
		}
		if(user.equals("komisaris-penjamin")) {
			loc=new String[] {"2","181.13330078125","129","311.13330078125","194"};
		}
		if(user.equals("preskom-peminjam")) {
			loc=new String[] {"2","181.13330078125","294","311.13330078125","359"};
		}
		if(user.equals("komisaris-penjamin2")) {
			loc=new String[] {"3","181.4000244140625","640","311.4000244140625","705"};
		}

		if(user.equals("komisaris2-peminjam")) {
			loc=new String[] {"2","182.13330078125","449","312.13330078125","514"};
		}
		if(user.equals("direktur-peminjam")) {
			loc=new String[] {"1","181.13330078125","115","311.13330078125","180"};
		}
		if(user.equals("direktur-modalku")) {
			loc=new String[] {"1","179.13330078125","332","309.13330078125","397"};
		}
		if(user.equals("pemberi-peminjaman")) {
			loc=new String[] {"1","183.13330078125","508","313.13330078125","573"};
		}
		if(user.equals("komisaris1-peminjam")) {
			loc=new String[] {"2","181.13330078125","607","311.13330078125","672"};
		}
			

		return loc;
	}
	
	
	public String[] getLocationIndividu(String user) {
		String loc[]= new String[]{"0","0","0","0","0"};
		
		if(user.equals("direktur-modalku")) {
			loc=new String[]{"1","184.015625","326","314.015625","391"};
		}
		
		if(user.equals("pemberi-peminjaman")) {
			loc=new String[]{"1","186.015625","517","316.015625","582"};
		}
		
		if(user.equals("pasangan-peminjam")) {
			loc=new String[]{"2","184.015625","605","314.015625","670"};
		}
		
		if(user.equals("peminjam")) {
			loc=new String[]{"1","188.015625","126","318.015625","191"};
		}		

		return loc;
	}
}
