package api.response;

import id.co.keriss.consolidate.DS.DigiSign;
import id.co.keriss.consolidate.DS.FaceRecognition;
import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.action.ApiVerification;
import id.co.keriss.consolidate.action.SmartphoneVerification;
import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.dao.BankDao;
import id.co.keriss.consolidate.dao.ConfirmCodeDao;
import id.co.keriss.consolidate.dao.DocumentsAccessDao;
import id.co.keriss.consolidate.dao.DocumentsDao;
import id.co.keriss.consolidate.dao.KeyDao;
import id.co.keriss.consolidate.dao.MerchantDao;
import id.co.keriss.consolidate.dao.MitraDao;
import id.co.keriss.consolidate.dao.PartnerDao;
import id.co.keriss.consolidate.dao.PreRegistrationDao;
import id.co.keriss.consolidate.dao.StoreDao;
import id.co.keriss.consolidate.dao.TerminalDao;
import id.co.keriss.consolidate.dao.TokenMitraDao;
import id.co.keriss.consolidate.dao.TrxDSDao;
import id.co.keriss.consolidate.dao.UserdataDao;
import id.co.keriss.consolidate.dao.UserdataDao2;
import id.co.keriss.consolidate.ee.Bank;
import id.co.keriss.consolidate.ee.CardSummaryVO;
import id.co.keriss.consolidate.ee.ConfirmCode;
import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.JenisKey;
import id.co.keriss.consolidate.ee.Key;
import id.co.keriss.consolidate.ee.Merchant;
import id.co.keriss.consolidate.ee.MerchantVO;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.Partner;
import id.co.keriss.consolidate.ee.PreRegistration;
import id.co.keriss.consolidate.ee.StatusKey;
import id.co.keriss.consolidate.ee.StatusTrxLog;
import id.co.keriss.consolidate.ee.Store;
import id.co.keriss.consolidate.ee.Terminal;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.TrxDs;

import org.hibernate.Transaction;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.ee.Userdata2;
import id.co.keriss.consolidate.util.AESEncryption;
import id.co.keriss.consolidate.util.Encryption;
import id.co.keriss.consolidate.util.FileProcessor;
import id.co.keriss.consolidate.util.LogSystem;
import id.co.keriss.consolidate.util.ReportUtil;
import id.co.keriss.consolidate.util.SaldoTransaction;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
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
import org.mortbay.util.ajax.JSON;

import com.anthonyeden.lib.config.Configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class forbidden extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("digisignlogger");

	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		context.getResponse().setStatus(403);
		
	}
	
		
		
		
}
