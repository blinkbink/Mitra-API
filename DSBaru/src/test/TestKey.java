package test;

import org.codehaus.jettison.json.JSONObject;

import id.co.keriss.consolidate.util.AESEncryption;

public class TestKey
{
	public static void main(String[] args) throws Exception
	{
		JSONObject jo = new JSONObject();
		JSONObject jf = new JSONObject();
		jf.put("eeuser", "1015636");
//		jf.put("level", "C2");
		jo.put("JSONFile", AESEncryption.encryptDocSign(jf.toString()));
//		jo.put("JSONFile", KMSRSAEncryption.encryptWithPub(jf.toString()));
		jo.put("encrypt-mode", "CBC");
		System.out.print(jo);

	}
}