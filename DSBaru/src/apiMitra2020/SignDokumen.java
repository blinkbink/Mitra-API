package apiMitra2020;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;

import id.co.keriss.consolidate.ee.DocumentAccess;
import id.co.keriss.consolidate.ee.Documents;

public class SignDokumen {
	JSONObject sign(List<DocumentAccess> lda, Documents doc, DB db, HttpServletRequest request, String refTrx, char payment, boolean sequence) {
		JSONObject resp=null;
		
		if(payment=='2') {
			
		} else if(payment=='3') {
			
		}
		return resp;
	}
}
