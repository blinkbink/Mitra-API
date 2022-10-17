package id.co.keriss.consolidate.action.ajax;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.Wl_DistrictsDao;
import id.co.keriss.consolidate.dao.Wl_ProvincesDao;
import id.co.keriss.consolidate.dao.Wl_RegenciesDao;
import id.co.keriss.consolidate.dao.Wl_VillageDao;
import id.co.keriss.consolidate.ee.Wl_Districts;
import id.co.keriss.consolidate.ee.Wl_Provinces;
import id.co.keriss.consolidate.ee.Wl_Regencies;
import id.co.keriss.consolidate.ee.Wl_Villages;
import id.co.keriss.consolidate.util.LogSystem;

public class FrmProcess extends ActionSupport {

	public static final int HTTP_TIMEOUT = 30 * 1000; // milliseconds
	String getreq = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
//        User user = (User) context.getSession().getAttribute (USER);
		int count = 21;
		DB db = getDB(context);
		HttpServletRequest  request  = context.getRequest();
//        User userTrx= user!=null?new UserManager(db).findById(user.getId()):null;
        
        
		try{

			String method = null;
			method = request.getParameter("frmProcess");
			
			String jsonResult = null;
//			if(method.equals("depAjx") && user!=null){
//				DepositProcessForm dp=new DepositProcessForm();
//				String amt=request.getParameter("amt");
//				String mid=request.getParameter("mid");
//				String act=request.getParameter("act");
//				
//				jsonResult=dp.topupProcessing(amt,mid,act,user.getId());
//			}
//			
//			if(method.equals("saveProduct") && user!=null && user.hasPermission("admin")){
//				ProductProcessForm p=new ProductProcessForm(request,db);
//				try{
//					jsonResult=p.processSaveProduct();
//				}catch(Exception e){
//					LogSystem.error(getClass(), e);
//				}
//			}
//			if(method.equals("deleteProduct") && user!=null && user.hasPermission("admin")){
//				ProductProcessForm p=new ProductProcessForm(request,db);
//				try{
//					jsonResult=p.deleteProductById();
//				}catch(Exception e){
//					LogSystem.error(getClass(), e);
//				}
//			}
//			
//			if(method.equals("getProduct") && user!=null && user.hasPermission("admin")){
//				ProductProcessForm p=new ProductProcessForm(request,db);
//				jsonResult=p.getProductbyCode();
//			}
//			
//			if(method.equals("mercAjx") && user!=null && user.hasPermission("admin")){
//				String id=request.getParameter("id");
//				String tid=request.getParameter("tid");
//				String sts=request.getParameter("sts");
//				boolean stsBoolean=false;
//				if(sts.equals("ON")) stsBoolean=true;
//				MerchantProcess mp=new MerchantProcess(db);
//				try{
//					jsonResult=mp.changeStatus(id,tid,stsBoolean);
//				}catch(Exception e){
//					LogSystem.error(getClass(), e);
//				}
//			}
//			
//			if(method.equals("mercDelAjx") && user!=null && user.hasPermission("admin")){
//				String id=request.getParameter("id");
//				
//				MerchantProcess mp=new MerchantProcess(db);
//				try{
//					jsonResult=mp.deleteMerchant(id);
//				}catch(Exception e){
//					LogSystem.error(getClass(), e);
//				}
//			}
//			
//			if(method.equals("getDetAjx") && user!=null){
//				String id=request.getParameter("id");
//				TransactionProcessForm tpf =new TransactionProcessForm(db);
//				jsonResult=tpf.getDetailTrx(id, user);
//			}
//			
//			if(method.equals("chgStatTransAjx") && user!=null && user.hasPermission("admin")){
//				String id=request.getParameter("id");
//				String sts=request.getParameter("sts");
//				String info=request.getParameter("info");
//				String price=request.getParameter("price");
//				String sn=request.getParameter("sn");
//
//				TransactionProcessForm tpf =new TransactionProcessForm(db);
//				StatusTrxLog newStatus=new StatusTrxLog();
//				newStatus.setId(new Long(id));
//				newStatus.setInformation(sts);
//				newStatus.setOldprice(new BigDecimal(price));
//				newStatus.setOldsn(sn);
//				newStatus.setOldstatus(sts);
//				tpf.setNewStat(newStatus);
//				jsonResult=tpf.changeStatTrx(id, sts, user);
//			}
//			
//			if(method.equals("genKey") && user!=null){
//				String pass=request.getParameter("password");
//				String id=userTrx.getUserdata().getNama()+" <"+userTrx.getNick()+">";
//				DigiSign ds=new DigiSign();
//				
//				JSONObject jo =new JSONObject();
//				if(!ds.generateKey(id, pass)){
//					jo.put("status", "Tidak dapat diproses");
//					
//				}else{
//					
//					// Save ke database
//					Date crDate=new Date();
//					Key keyPrivData=new Key();
//					keyPrivData.setJenis_key(new JenisKey("PV"));
//					keyPrivData.setKey(Base64.toBase64String(ds.getSecKey().getEncoded()));
//					keyPrivData.setKey_id(String.valueOf(ds.getSecKey().getKeyID()));
//					keyPrivData.setStatus(new StatusKey("ACT"));
//					keyPrivData.setUserdata(userTrx.getUserdata());
//					keyPrivData.setUser_id((String) ds.getSecKey().getUserIDs().next());
//					keyPrivData.setWaktu_buat(crDate);
//					
//					Key keyPubData=new Key();
//					keyPubData.setJenis_key(new JenisKey("PS"));
//					keyPubData.setKey(Base64.toBase64String(ds.getPublicSign().getEncoded()));
//					keyPubData.setKey_id(String.valueOf(ds.getPublicSign().getKeyID()));
//					keyPubData.setStatus(new StatusKey("ACT"));
//					keyPubData.setUserdata(userTrx.getUserdata());
//					keyPubData.setUser_id((String) ds.getPublicSign().getUserIDs().next());
//					keyPubData.setWaktu_buat(crDate);
//					
//					KeyDao kDao = new KeyDao(db);
//					kDao.create(keyPrivData);
//					kDao.create(keyPubData);
//					
//					jo.put("status", "OK");
//					jo.put("key", ds.keyTxt(Base64.toBase64String(ds.getPublicSign().getEncoded())));
//					
//					System.out.println("PUB : "+Base64.toBase64String(ds.getPublicSign().getEncoded()));
//					System.out.println("PRIV : "+Base64.toBase64String(ds.getSecKey().getEncoded()));
//				}
//				System.out.println(jo.toString());
//				jsonResult=jo.toString();
//			}
//
//			
//			if(method.equals("genCode") && user!=null){
//				
//					JSONObject jo =new JSONObject();
//					Random rand = new Random();
//					String  cd = String.valueOf(rand.nextInt(900000) + 100000);
//					// Save ke database
//					Date crDate=new Date();
//					ConfirmCode cc = new ConfirmCode();
//					ConfirmSms cs = new ConfirmSms();
//					String status = cs.sendingPostRequest(cd, userTrx.getUserdata().getNo_handphone());
//					System.out.println("Status SMS = "+status);
//					if(status.equals("OK")) {
//					cc.setUserdata(userTrx.getUserdata());
//					cc.setWaktu_buat(crDate);
//					cc.setStatus("no");
//					cc.setCode(cd);
//					ConfirmCodeDao ccd = new ConfirmCodeDao(db);
//					ccd.create(cc);
//					jo.put("status", "OK");
//					}else {
//						jo.put("status", "gagal");
//					}
//					
//				System.out.println(jo.toString());
//				jsonResult=jo.toString();
//			}
//			
//			if(method.equals("tes") && user!=null){
//				System.out.println("####TES##");
//			}
//	
//			if(method.equals("verCode") && user!=null){
//				
//				JSONObject jo =new JSONObject();
//				
//				// Save ke database
//				Date crDate=new Date();
//				
//				String res = "Kode verifikasi salah";
//				
//				String code = request.getParameter("code").toString();
//				try {
//					ConfirmCodeDao ccd = new ConfirmCodeDao(db);	
//					ConfirmCode cc = ccd.findByUserID(code,userTrx.getUserdata().getId());
//					if (cc != null) {
//						Date waktubuat = cc.getWaktu_buat();
//						Date date = new Date();
//						long diff = date.getTime() - waktubuat.getTime();
//						long diffHours = diff / (60 * 60 * 1000) % 24;
//						System.out.print(diffHours + " hours, ");
//						if (diffHours < 1) {						
//						res = "OK";
//						cc.setStatus("yes");
//						ccd.update(cc);
//						} else {
//							res = "Kode sudah kadaluarsa, harap kirim kembali kode verifikasi";
//						}
//					}
//				} catch (Exception e) {
//					// TODO: handle exception
//					System.out.println("User "+user);
//					LogSystem.error(getClass(), e);
//				}
//				
//				jo.put("status", res);
//				System.out.println(jo.toString());
//				jsonResult=jo.toString();
//		}
//			
//			if(method.equals("signPub") && user!=null){
//
//				DigiSign ds=new DigiSign();
//				String pb=ds.signKey(request.getParameter("pub"));
//				JSONObject jo =new JSONObject();
//				if(pb==null){
//					jo.put("status", "Tidak dapat diproses");
//					
//				}else{
//					Key keyPubData=new Key();
//					keyPubData.setJenis_key(new JenisKey("PS"));
//					keyPubData.setKey(Base64.toBase64String(ds.getPublicSign().getEncoded()));
//					keyPubData.setKey_id(String.valueOf(ds.getPublicSign().getKeyID()));
//					keyPubData.setStatus(new StatusKey("ACT"));
//					keyPubData.setUserdata(userTrx.getUserdata());
//					keyPubData.setUser_id((String) ds.getPublicSign().getUserIDs().next());
//					keyPubData.setWaktu_buat(new Date());
//					
//					KeyDao kDao = new KeyDao(db);
//					kDao.create(keyPubData);
//					
//					jo.put("status", "OK");
//					jo.put("key", ds.keyTxt(pb));
//					
//				}
//				System.out.println(jo.toString());
//				jsonResult=jo.toString();
//			}

			if (method.equals("getPropinsi")) {
				JSONObject jo = new JSONObject();
				Wl_ProvincesDao wl_propdao = new Wl_ProvincesDao(getDB(context));
				List<Wl_Provinces> str = wl_propdao.findAll();
				// Vector str2 = new Vector();
				JSONArray array = new JSONArray();
				for (Wl_Provinces m : str) {
					JSONObject str3 = new JSONObject();
					str3.put("value", m.getId());
					str3.put("data", m.getName());

					array.put(str3);

					// str2.add(m.getName());
				}
				LogSystem.info(this.getClass(), "getProvinsi");
				jo.put("status", "ok");
				jo.put("props", array);
				jsonResult = jo.toString();
			}

			
			if (method.equals("getRegenci")) {
				String prop = request.getParameter("prop");
				JSONObject jo = new JSONObject();
				Wl_RegenciesDao wl_regdao = new Wl_RegenciesDao(getDB(context));
				List<Wl_Regencies> str = wl_regdao.findRegencies(prop);
				// Vector str2 = new Vector();
				JSONArray array = new JSONArray();
				for (Wl_Regencies m : str) {
					JSONObject str3 = new JSONObject();
					str3.put("value", m.getId());
					str3.put("data", m.getName());

					array.put(str3);

					// str2.add(m.getName());
				}
				LogSystem.info(this.getClass(), "getRegency");

				jo.put("status", "ok");
				jo.put("regencies", array);
				jsonResult = jo.toString();
			}

			if (method.equals("getDistrict")) {
				String reg = request.getParameter("reg");
				JSONObject jo = new JSONObject();
				Wl_DistrictsDao wl_regdao = new Wl_DistrictsDao(getDB(context));
				List<Wl_Districts> str = wl_regdao.findDistricts(reg);
				// Vector str2 = new Vector();
				JSONArray array = new JSONArray();
				for (Wl_Districts m : str) {
					JSONObject str3 = new JSONObject();
					str3.put("value", m.getId());
					str3.put("data", m.getName());

					array.put(str3);

					// str2.add(m.getName());
				}
				LogSystem.info(this.getClass(), "getDistrict");

				jo.put("status", "ok");
				jo.put("districts", array);
				jsonResult = jo.toString();
			}

			if (method.equals("getVillage")) {
				String dis = request.getParameter("dis");
				JSONObject jo = new JSONObject();
				Wl_VillageDao wl_regdao = new Wl_VillageDao(getDB(context));
				List<Wl_Villages> str = wl_regdao.findVillages(dis);
				// Vector str2 = new Vector();
				JSONArray array = new JSONArray();
				for (Wl_Villages m : str) {
					JSONObject str3 = new JSONObject();
					str3.put("value", m.getId());
					str3.put("data", m.getName());

					array.put(str3);

					// str2.add(m.getName());
				}
				LogSystem.info(this.getClass(), "getVillage");

				jo.put("status", "ok");
				jo.put("villages", array);
				jsonResult = jo.toString();
			}

			if (method.equals("getKodepos")) {
				String dis = request.getParameter("dis");
				JSONObject jo = new JSONObject();
				Wl_VillageDao wl_regdao = new Wl_VillageDao(getDB(context));
				Wl_Villages str = wl_regdao.findKodepos(dis);

				LogSystem.info(this.getClass(), "getKodePos");

				jo.put("status", "ok");
				jo.put("kodepos", str.getKodepos());
				jsonResult = jo.toString();
			}
			
//			if(method.equals("register")){
//
//				
//				JSONObject jo=new JSONObject();
//		        String res="05";
//		        String notif="Email sudah terdaftar gunakan email lain";
//				Userdata userdata= new Userdata();
//
//				try{
//			        userdata.setAlamat(request.getParameter("alamat").toString());
//			        if(request.getParameter("jenis_kelamin").toString().equalsIgnoreCase("laki-laki")) userdata.setJk('L');
//			        else  userdata.setJk('P');
//			        userdata.setKecamatan(request.getParameter("kecamatan").toString());
//			        userdata.setKelurahan(request.getParameter("kelurahan").toString());
//			        userdata.setKodepos(request.getParameter("kode_pos").toString());
//			        userdata.setKota(request.getParameter("kota").toString());
//			        userdata.setNama(request.getParameter("nama").toString());
//			        userdata.setNo_handphone(request.getParameter("tlp").toString());
//			        userdata.setTgl_lahir(new SimpleDateFormat("dd-MM-yyyy").parse(request.getParameter("tgl_lahir").toString()));
//			        userdata.setPropinsi(request.getParameter("provinsi").toString());
//			        userdata.setNo_identitas(request.getParameter("idktp").toString());
//			        userdata.setTempat_lahir(request.getParameter("tmp_lahir").toString());
//			        	         
//			        User login =new User();
//			        login.setPassword(EEUtil.getHash(request.getParameter("email").toString(), request.getParameter("password").toString()));
//			        login.setNick(request.getParameter("email").toString());
//			        login.setName(request.getParameter("nama").toString());
//			        login.grant("ds");
//			        login.grant("login");
//			        login.setUserdata(userdata);
//			        login.logRevision("created", new UserManager(db).findById((long) 0));
//			        new UserdataDao(db).create(userdata);
//
//			        System.out.println("PASS:"+ request.getParameter("password").toString());
//					 
////					System.out.println("PB :"+pb); 
////					System.out.println("CEK PB "+pb.equals(Base64.toBase64String(ds.getPublicSign().getEncoded()))); 
////					System.out.println("VERF :"+ds.verifyPublicKey(keyPubData.getKey())); 
//					 
//			        org.hibernate.Transaction tx = db.session().beginTransaction();
//			        db.session().save (login);
//			        tx.commit ();
//			        SendMailSSL sendMailSSL = new SendMailSSL();
//			        sendMailSSL.sendMailRegister(userdata,request.getParameter("email").toString());
//			       
//			        res="OK";
//			        notif="Registrasi berhasil silahkan login menggunakan email : "+ request.getParameter("email").toString();
//
//				}
//				catch (Exception e) {
//					// TODO: handle exception
//					new UserdataDao(db).delete(userdata);
//					LogSystem.error(getClass(), e);
//				}
//		        jo.put("status", res);
//		        jo.put("notif", notif);
//				
//				jsonResult=jo.toString();
//			}
			if(jsonResult!=null && !jsonResult.equals("")){
	            context.put("jsoncontent", jsonResult);
			}
			
			
			
			

		}catch (Exception e) {
            LogSystem.error(getClass(), e);
//			error (context, e.getMessage());
//            context.getSyslog().error (e);
		}
	}

	/*
    private String extractFileName(Part part) {
        // form-data; name="file"; filename="C:\file1.zip"
        // form-data; name="file"; filename="C:\Note\file2.zip"
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                // C:\file1.zip
                // C:\Note\file2.zip
                String clientFileName = s.substring(s.indexOf("=") + 2, s.length() - 1);
                clientFileName = clientFileName.replace("\\", "/");
                int i = clientFileName.lastIndexOf('/');
                // file1.zip
                // file2.zip
                return clientFileName.substring(i + 1);
            }
        }
        return null;
    }
	
	*/
	public static String request(HttpResponse response) {
		String result = "";
		try {
			InputStream in = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder str = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				str.append(line + "\n");
			}
			in.close();
			result = str.toString();
		} catch (Exception ex) {
			result = "Error";
		}
		return result;
	}
	
	
}
