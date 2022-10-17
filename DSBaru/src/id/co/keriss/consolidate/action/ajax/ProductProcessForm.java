package id.co.keriss.consolidate.action.ajax;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;

import id.co.keriss.consolidate.dao.FeatureDao;
import id.co.keriss.consolidate.dao.NominalAmountDao;
import id.co.keriss.consolidate.dao.ProductBillerDao;
import id.co.keriss.consolidate.dao.ProductBillerMerchantDao;
import id.co.keriss.consolidate.dao.SupplierDao;
import id.co.keriss.consolidate.ee.Feature;
import id.co.keriss.consolidate.ee.NominalAmount;
import id.co.keriss.consolidate.ee.Productbiller;
import id.co.keriss.consolidate.ee.Supplier;

public class ProductProcessForm {
	DecimalFormat myFormatter = new DecimalFormat("###,###.##");
	private List <Productbiller> listDenomExist;
	private List <Productbiller> listPBNew;
	private HttpServletRequest req;
	DB db;

	public ProductProcessForm(HttpServletRequest req,DB db) {
		this.req=req;
		this.db=db;
	}
	
	String getProductbyCode() throws JSONException{
		String result="";
		ProductBillerDao pbDao=new ProductBillerDao(db);
		listDenomExist=pbDao.findPBExist(req.getParameter("pcode1").toUpperCase(), req.getParameter("pcode_back").toUpperCase() );
		if(listDenomExist==null){
			JSONObject obj=new JSONObject();
			obj.put("status","OK");
			result=obj.toString();
		}else{
			JSONObject objJson=new JSONObject();

			JSONArray jsonArray=new JSONArray();
			for (Productbiller pb : listDenomExist) {
				System.out.println(pb);
				JSONObject obj=new JSONObject();
				obj.put("pid", pb.getId());
				obj.put("name", pb.getName());
				obj.put("code", pb.getProductbillercode());
				if(pb.getProductsuppcode()!=null)obj.put("scode", pb.getProductsuppcode());
				else obj.put("scode", "");
				if(pb.getSupplier()!=null)obj.put("supp", pb.getSupplier().getName());
				else obj.put("supp", "");
				obj.put("denom", myFormatter.format(pb.getNominal()*1000));
				obj.put("admin", myFormatter.format(pb.getAdmin()));
				obj.put("mitra", myFormatter.format(pb.getMitra()));
				jsonArray.put(obj);

			}
			objJson.put("data",jsonArray);
			objJson.put("status","OK");
			result=objJson.toString();
		}
		return result;

	}
	
	String processSaveProduct() throws JSONException{
		Feature ftr = null;
		int admin=Integer.parseInt(req.getParameter("admin-amt"));
		int mitra=Integer.parseInt(req.getParameter("mitra-amt"));
		String name=req.getParameter("name");
		String code_first=req.getParameter("pcode1").toUpperCase();
		String code_last=req.getParameter("pcode_back").toUpperCase();
		String suppcode_first=req.getParameter("scode1").toUpperCase();
		String suppcode_last=req.getParameter("scode_back").toUpperCase();
		String supplierId=req.getParameter("supplier").toUpperCase();
		String result="";
		ProductBillerDao pbDao=new ProductBillerDao(db);
		FeatureDao fdao=new FeatureDao(db);
				
		ftr=fdao.findById(Long.valueOf(req.getParameter("ftr")));
		listPBNew=new ArrayList<Productbiller>();
		listDenomExist=pbDao.findPBExist(code_first, code_last);
		Supplier supplier=new SupplierDao(db).findById(Long.valueOf(supplierId));
		NominalAmountDao nomDao=new NominalAmountDao(db);
		List <NominalAmount> listDenom=nomDao.getAll();
		for (NominalAmount na : listDenom) {
			String postNom=null;
			try{
				postNom=req.getParameter(String.valueOf(na.getDenom()));
			}catch(Exception e){}
			
			if(postNom!=null){
				Productbiller pb=new Productbiller();
				pb.setFeature(ftr);
				pb.setAdmin(admin);
				pb.setMitra(mitra);
				pb.setHarga(0);
				pb.setTotal(0);
				pb.setProductbillercode(code_first+na.getDenom()+code_last);
				pb.setProductsuppcode(suppcode_first+na.getDenom()+suppcode_last);
				pb.setName(name);
				pb.setNominal(na.getDenom());
				pb.setExist(adaDenom(String.valueOf(na.getDenom())));
				pb.setSupplier(supplier);
				listPBNew.add(pb);
				
			}
		}
		
		if(listPBNew.size()>0){
			try {
				pbDao.saveOrUpdatePB(listPBNew);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONObject obj=new JSONObject();
			obj.put("status","OK");
			result=obj.toString();
		}else{
			JSONObject obj=new JSONObject();
			obj.put("status","FAIL");
			result=obj.toString();
		}
		
		return result;
		
	}
	
	boolean adaDenom(String dnm){
		Iterator<Productbiller> nm=listDenomExist.iterator();
		boolean res=false;
		while (nm.hasNext()) {
			Productbiller na=nm.next();
			String cekString=String.valueOf(na.getNominal());
			if(cekString.equals(dnm)){
				return true;
			}
		}
		
		return res;
	}
	
	String deleteProductById() throws JSONException{
		String result="";
		ProductBillerDao pbDao=new ProductBillerDao(db);
		ProductBillerMerchantDao pbmDao=new ProductBillerMerchantDao(db);
		Productbiller pb=pbDao.findById(Long.valueOf(req.getParameter("del-id")));
		if(pb==null){
			JSONObject obj=new JSONObject();
			obj.put("status","FAIL");
			result=obj.toString();
		}else{
			pbmDao.deleteProductBillerFromMerchant(pb);
			pbDao.deleteTransactionCommit(pb);
			JSONObject objJson=new JSONObject();
			objJson.put("status","OK");
			result=objJson.toString();
		}
		return result;

	}
	
}
