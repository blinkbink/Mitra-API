package id.co.spin.test;

import id.co.keriss.consolidate.DS.DigiSign;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.dao.BankDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.KeyDao;
import id.co.keriss.consolidate.dao.MerchantDao;
import id.co.keriss.consolidate.dao.MitraDao;
import id.co.keriss.consolidate.dao.PartnerDao;
import id.co.keriss.consolidate.dao.StoreDao;
import id.co.keriss.consolidate.dao.TerminalDao;
import id.co.keriss.consolidate.dao.TrxDSDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.ee.Bank;
import id.co.keriss.consolidate.ee.CardSummaryVO;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.JenisKey;
import id.co.keriss.consolidate.ee.Key;
import id.co.keriss.consolidate.ee.Merchant;
import id.co.keriss.consolidate.ee.MerchantVO;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.Partner;
import id.co.keriss.consolidate.ee.StatusKey;
import id.co.keriss.consolidate.ee.StatusTrxLog;
import id.co.keriss.consolidate.ee.Store;
import id.co.keriss.consolidate.ee.Terminal;
import id.co.keriss.consolidate.ee.TrxDs;

import org.hibernate.Session;
import org.hibernate.Transaction;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.FileProcessor;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.ReportUtil;
import id.co.keriss.consolidate.util.SaldoTransaction;
import id.co.keriss.consolidate.util.SignerModalku;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jcajce.provider.symmetric.ARC4.Base;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.EEUtil;
import org.jpos.ee.User;
import org.jpos.ee.UserManager;
import org.jpos.ee.action.Login;
import org.jpos.iso.ISOUtil;
import org.jpublish.JPublishContext;
import org.mortbay.log.Log;

import com.anthonyeden.lib.config.Configuration;

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

	    	LogManager.getLogger(getClass()).error(e);
		}
	}
	

}
