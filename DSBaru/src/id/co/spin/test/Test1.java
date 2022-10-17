package id.co.spin.test;

import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import org.jpos.ee.DB;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.MitraDao;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.util.LogSystem;

public class Test1 extends ActionSupport {

	SimpleDateFormat sdf=new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		DB db=(org.jpos.ee.DB) context.get(DB);
		HttpServletRequest  request  = context.getRequest();
		
		try{
			MitraDao mDao=new MitraDao(db);
			String data="";
			
			Mitra mitra1=new Mitra();
			mitra1.setName("PT Maju Selalu");
			mitra1.setId(mDao.create(mitra1));
			
			Mitra mitra2=new Mitra();
			mitra2.setName("PT Terang");
			mitra2.setId(mDao.create(mitra2));

			
			data+=mitra1.getName()+":"+mitra1.getId();
			data+=mitra2.getName()+":"+mitra2.getId();

			context.put("trxjson", data );

			

		}catch (Exception e) {

			LogSystem.error(getClass(), e);
		}
	}
	

}
