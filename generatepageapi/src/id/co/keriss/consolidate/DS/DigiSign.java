package id.co.keriss.consolidate.DS;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.util.Date;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import org.bouncycastle.util.encoders.Base64;

import id.co.keriss.consolidate.ee.JenisKey;

public class DigiSign {
	public static byte[] pubCA=Base64.decode("mE0EWL5gDwECALlGEVOV3sGwLtB6RS6g67zSmA6nD7brE4g963C0njEE3+1jGfWoBpevFO47FEI9tUBR5tbpriUWBb3hfuAfkP0AEQEAAbQNZGV2ZWxvcG1lbnQxOYhuBBMDAgAYBQJYvmAPAhsDBAsJCAcGFQgCCQoLAh4BAAoJEHtGx5ngAuDYbesCAIJ0gj/ZGWMiHGFHUZNAj3Dbp2CK6fJFrQJZ8RO8sEHCaEWbEbPPKSR721VpGI/AgNyaS2+59IOP2GftcXqBYHw=");
	byte[] secretCA = Base64.decode("lQEmBFi+YA8BAgC5RhFTld7BsC7QekUuoOu80pgOpw+26xOIPetwtJ4xBN/tYxn1qAaXrxTuOxRCPbVAUebW6a4lFgW94X7gH5D9ABEBAAH+CQMCjIVu+Un0E6pggDynSV7/loC95dDMKAKr2qa8Onq4jUaJEv69o0+MXFxcw1efSIs1lSeF/ZmNP5Z7VBkQL6AGhZ6JRQzfPW4t3bqA5SnACnuBgMuM86Z75/eRTTcnqzxL9FgfnqvO1FeeC9dM8cS8WClfPMrmnRIh5zFsYYhVYY8jRFdz0EN4TiYeX4ux+vqomaK+k2hRMwzMtEoCE4C+0VnxYCRR9AhmwldScBG5b4PbuJpoJAp1KXYLTqBsdTIsL+LdXCsTuQPK0qHlfNazTZZiWevAtA1kZXZlbG9wbWVudDE5iG4EEwMCABgFAli+YA8CGwMECwkIBwYVCAIJCgsCHgEACgkQe0bHmeAC4Nht6wIAgnSCP9kZYyIcYUdRk0CPcNunYIrp8kWtAlnxE7ywQcJoRZsRs88pJHvbVWkYj8CA3JpLb7n0g4/YZ+1xeoFgfA==");
    char pass[] = {'d', 'v', '1', '9', 'd', 's', 'i', 'g', 'n'};
	
    String filename;
	PGPPublicKey publicSign;
	PGPSecretKey secKey;
	String notif="";
	
	public DigiSign() {
		// TODO Auto-generated constructor stub
        Security.addProvider(new BouncyCastleProvider());
	}
	
	
	
	public String getNotif() {
		return notif;
	}



	public void setNotif(String notif) {
		this.notif = notif;
	}



	public PGPPublicKey getPublicSign() {
		return publicSign;
	}

	
	public PGPSecretKey getSecKey() {
		return secKey;
	}
	
	public String getFilename() {
		return filename;
	}


	public void setFilename(String filename) {
		this.filename = filename;
	}


	public boolean verifyPublicKey(String pk){
		
		PGPPublicKeyRing pgpRings;
		PGPPublicKeyRing pgpRings2;
		try {
			pgpRings = new PGPPublicKeyRing(Base64.decode(pk), new JcaKeyFingerprintCalculator());
			pgpRings2 = new PGPPublicKeyRing(pubCA, new JcaKeyFingerprintCalculator());
	        return verify(pgpRings.getPublicKey(), pgpRings2.getPublicKey().getUserIDs().next().toString(), pgpRings2.getPublicKey());
	         
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PGPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
    private static boolean isWhiteSpace(byte b)
    {
//        return b == '\r' || b == '\n' || b == '\t' || b == ' ';
        return false;
    }
    
    
	private static int getLengthWithoutWhiteSpace(byte[] line)
	    {
	        int    end = line.length - 1;

	        while (end >= 0 && isWhiteSpace(line[end]))
	        {
	            end--;
	        }

	        return end + 1;
	    }

	private static void processLine(PGPSignature sig, byte[] line)
	        throws SignatureException, IOException
    {
		System.out.println(new String(line));
        int length = getLengthWithoutWhiteSpace(line);
        if (length > 0)
        {
            sig.update(line, 0, length);
        }
    }
	
	 private static void processLine(OutputStream aOut, PGPSignatureGenerator sGen, byte[] line)

    {
        int length = getLengthWithoutWhiteSpace(line);
        if (length > 0)
        {
            sGen.update(line, 0, length);
        }

        try {
			aOut.write(line, 0, line.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	private static int readPassedEOL(ByteArrayOutputStream bOut, int lastCh, InputStream fIn)
	        throws IOException
	    {
	        int lookAhead = fIn.read();

	        if (lastCh == '\r' && lookAhead == '\n')
	        {
	            bOut.write(lookAhead);
	            lookAhead = fIn.read();
	        }

	        return lookAhead;
	    }
	
     private static int readInputLine(ByteArrayOutputStream bOut, int lookAhead, InputStream fIn)
	        throws IOException
	    {
	        bOut.reset();

	        int ch = lookAhead;

	        do
	        {
	            bOut.write(ch);
	            if (ch == '\r' || ch == '\n')
	            {
	                lookAhead = readPassedEOL(bOut, ch, fIn);
	                break;
	            }
	        }
	        while ((ch = fIn.read()) >= 0);

	        return lookAhead;
	    }
     
     private static int readInputLine(ByteArrayOutputStream bOut, InputStream fIn)
    	        throws IOException
    	    {
    	        bOut.reset();

    	        int lookAhead = -1;
    	        int ch;

    	        while ((ch = fIn.read()) >= 0)
    	        {
    	            bOut.write(ch);
    	            if (ch == '\r' || ch == '\n')
    	            {
    	                lookAhead = readPassedEOL(bOut, ch, fIn);
    	                break;
    	            }
    	        }

        return lookAhead;
    }

	public boolean checkSign(
	        String message,
	        String sign, String pk)
	        throws Exception
	    {
	        try{
	        

		        PGPPublicKeyRingCollection pgpRings = new PGPPublicKeyRingCollection(Base64.decode(pk), new JcaKeyFingerprintCalculator());
	
		        
		        JcaPGPObjectFactory           pgpFact2 = new JcaPGPObjectFactory(Base64.decode(sign));
		        PGPSignatureList           p3 = (PGPSignatureList)pgpFact2.nextObject();
		        PGPSignature               sig = p3.get(0);
	
		        sig.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), pgpRings.getPublicKey(sig.getKeyID()));
	
		        System.out.println("Signature : " + Base64.toBase64String(sig.getEncoded()));
	
		        ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
		        InputStream           sigIn = new ByteArrayInputStream(Base64.decode(message));
		        int lookAhead = readInputLine(lineOut, sigIn);
		        
		        processLine(sig, lineOut.toByteArray());
	
		        if (lookAhead != -1)
		        {
		            do
		            {
		                lookAhead = readInputLine(lineOut, lookAhead, sigIn);
	
		                sig.update((byte)'\r');
		                sig.update((byte)'\n');
		                processLine(sig, lineOut.toByteArray());
		            }
		            while (lookAhead != -1);
		        }
	
		        if (sig.verify())
		        {
		        	System.out.println("SIGN OK");
		        	return true;
	
		        }
	        }
	        catch (Exception e) {
				// TODO: handle exception
	        	e.printStackTrace();
			}
	        
	        return false;
	    }
	

    /**
     * Generate an encapsulated signed file.
     * 
     * @param fileName
     * @param keyIn
     * @param out
     * @param pass
     * @param armor
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws PGPException
     * @throws SignatureException
     */
    public ByteArrayOutputStream signFile(
    		 byte[] message,String fileName,
		        String scKey, String pwd)
        throws IOException, NoSuchAlgorithmException, NoSuchProviderException, PGPException, SignatureException
    {
       ByteArrayOutputStream fileOut=new ByteArrayOutputStream();
	   OutputStream out=new ArmoredOutputStream(fileOut);
       
    	PGPSecretKey                    pgpSecKey = readSecretKey(new ByteArrayInputStream(Base64.decode(scKey)));
        
        PGPPrivateKey pgpPrivKey;
		
		pgpPrivKey = pgpSecKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder(new JcaPGPDigestCalculatorProviderBuilder().setProvider("BC").build()).setProvider("BC").build(pwd.toCharArray()));
        PGPSignatureGenerator           sGen = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(pgpSecKey.getPublicKey().getAlgorithm(), PGPUtil.SHA256).setProvider("BC"));

        
        
        sGen.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);
        
        Iterator    it = pgpSecKey.getPublicKey().getUserIDs();
        if (it.hasNext())
        {
            PGPSignatureSubpacketGenerator  spGen = new PGPSignatureSubpacketGenerator();
            
            spGen.setSignerUserID(false, (String)it.next());
            sGen.setHashedSubpackets(spGen.generate());
        }
        
        PGPCompressedDataGenerator  cGen = new PGPCompressedDataGenerator(
                                                                PGPCompressedData.ZLIB);
        
        BCPGOutputStream            bOut = new BCPGOutputStream(cGen.open(out));
        
        sGen.generateOnePassVersion(false).encode(bOut);
        
        File                        file = new File("/home/development19/Documents/wom.csv");
        PGPLiteralDataGenerator     lGen = new PGPLiteralDataGenerator();
        OutputStream                lOut = lGen.open(bOut, PGPLiteralData.BINARY, file);
          FileInputStream             fIn = new FileInputStream(file);
        //        InputStream             fIn = new ByteArrayInputStream(message);
        int                         ch = 0;
        
        while ((ch = fIn.read()) >= 0)
        {
            lOut.write(ch);
            sGen.update((byte)ch);
        }

//        lGen.close();

        sGen.generate().encode(bOut);

        cGen.close();

        out.close();
        
        return fileOut;
        
    }
    
	 public String generateSignature(
		        byte[] message,
		        String scKey, String pwd)
		    {
		 		notif="";
//		    	paramDAO = GlobalVarDb.parameterDAO;
//		    	ParameterList pList = new ParameterList();
//		    	pList = paramDAO.getAllParameter();
		    	
		 		try {
		    		PGPSecretKey                    pgpSecKey = readSecretKey(new ByteArrayInputStream(Base64.decode(scKey)));
		        
			        PGPPrivateKey pgpPrivKey;
					
					pgpPrivKey = pgpSecKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder(new JcaPGPDigestCalculatorProviderBuilder().setProvider("BC").build()).setProvider("BC").build(pwd.toCharArray()));
			        PGPSignatureGenerator           sGen = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(pgpSecKey.getPublicKey().getAlgorithm(), PGPUtil.SHA256).setProvider("BC"));
			        PGPSignatureSubpacketGenerator  spGen = new PGPSignatureSubpacketGenerator();
	
			        sGen.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);
			        
			        Iterator    it = pgpSecKey.getPublicKey().getUserIDs();
			        if (it.hasNext())
			        {
			            spGen.setSignerUserID(false, (String)it.next());
			            sGen.setHashedSubpackets(spGen.generate());
			        }
			        
			        ByteArrayOutputStream  bOut = new ByteArrayOutputStream();
			        ArmoredOutputStream    aOut = new ArmoredOutputStream(bOut);
			        ByteArrayInputStream   bIn = new ByteArrayInputStream(message);
	
			        aOut.beginClearText(PGPUtil.SHA256);
	
			        //
			        // note the last \n in the file is ignored
			        //
			        /*ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
			        int lookAhead = readInputLine(lineOut, bIn);
	
			        processLine(aOut, sGen, lineOut.toByteArray());
	
			        if (lookAhead != -1)
			        {
			            do
			            {
			                lookAhead = readInputLine(lineOut, lookAhead, bIn);
	
			                sGen.update((byte)'\r');
			                sGen.update((byte)'\n');
	
			                processLine(aOut, sGen, lineOut.toByteArray());
			            }
			            while (lookAhead != -1);
			        }
	
			        aOut.endClearText();
	
			        BCPGOutputStream            bcpgOut = new BCPGOutputStream(aOut);
			        
			        PGPSignature pSg = sGen.generate();
			        pSg.getEncoded();
			        
			        sGen.generate().encode(bcpgOut);
	
			        aOut.close();*/
			        
			        System.out.println("lenth : "+message.length);

			        sGen.update(message, 0, message.length);
			        PGPSignature pSg = sGen.generate();
			        
	//		        Log.v("hasilnya :", ISOUtil.hexString(pSg.getEncoded()));
	//		        Log.v("hasilnya base64 :", Base64.toBase64String(pSg.getEncoded()));
			        
	//		        applicationctx.callerDS(Base64.toBase64String(pSg.getEncoded()));
			        return Base64.toBase64String(pSg.getEncoded());
//			        System.out.println("sign : "+Base64.toBase64String(pSg.getEncoded()));
		} catch (PGPException e) {
			// TODO Auto-generated catch block
	        notif="Invalid Password";
			e.printStackTrace();
		}       
//		        messageTest(new String(bOut.toByteArray()), type);
		 catch (IOException e) {
		     		notif="Error";
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return null;
	    }

	/**
	 * Verifies that a public key is signed with another public key
	 *
	 * @param keyToVerify the public key to verify
	 * @param id the id we are verifying against the public key
	 * @param keyToVerifyWith the key to verify with
	 *
	 * @return true if verified, false otherwise
	 */
	boolean verify( PGPPublicKey keyToVerify, String id, PGPPublicKey keyToVerifyWith )
	        throws PGPException
	{

		try
	    {
	    	System.out.println("KEY = "+Base64.toBase64String(keyToVerify.getEncoded()));
	        Iterator<PGPSignature> signIterator = keyToVerify.getSignatures();
	        while ( signIterator.hasNext() )
	        {

	            PGPSignature signature = signIterator.next();
	            signature.init( new JcaPGPContentVerifierBuilderProvider().setProvider( "BC" ), keyToVerifyWith );
	            if ( signature.verifyCertification( id.getBytes(), keyToVerify ) )
	            {
	            	System.out.println("VERIFY KEY = TRUE");
	                return true;
	            }
	        }
	        return false;
	    }
	    catch ( Exception e )
	    {
	        //throw custom  exception
	    throw new PGPException( "Error verifying public key", e );
	    }
	}
	
	public static String keyTxt(String dataKey){
		String data="";
		for(int i=0; i<dataKey.length(); i++){
        	if(i>0 && i%64==0){
            	data+="\n";
        		
        	}
        	data+=(dataKey.charAt(i));

        }
		return data;
	}
	
	public static String keyTxtArmor(String dataKey, JenisKey j){
		String key="";
       try {
    	   OutputStream o=new ByteArrayOutputStream();
           OutputStream outputStream = new ArmoredOutputStream(o);
           if(j.getJenis_key().equals("PS")){
        	   PGPPublicKeyRing pgpRings2 = new PGPPublicKeyRing(Base64.decode(dataKey), new JcaKeyFingerprintCalculator());
        	   pgpRings2.getPublicKey().encode(outputStream);
           }
           else {
               PGPSecretKeyRingCollection        pgpSec = new PGPSecretKeyRingCollection(Base64.decode(dataKey), new JcaKeyFingerprintCalculator());
               pgpSec.encode(outputStream);
           }
           outputStream.close();
	       key=o.toString();
	       o.close();
       
       } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PGPException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
       return key;

	}
	

    /**
     * Generate an encapsulated signed file.
     * 
     * @param fileName
     * @param keyIn
     * @param out
     * @param pass
     * @param armor
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws PGPException
     * @throws SignatureException
     */
    private void signFile(
        String          fileName,
        String 			scKey,
        InputStream     keyIn,
        OutputStream    out,
        char[]          pass,
        boolean         armor)
        throws IOException, NoSuchAlgorithmException, NoSuchProviderException, PGPException, SignatureException
    {
        if (armor)
        {
            out = new ArmoredOutputStream(out);
        }
			PGPSecretKey                    pgpSec = readSecretKey(new ByteArrayInputStream(Base64.decode(scKey)));
	        PGPPrivateKey               pgpPrivKey = pgpSec.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(pass));
	        PGPSignatureGenerator       sGen = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(pgpSec.getPublicKey().getAlgorithm(), PGPUtil.SHA1).setProvider("BC"));
	        
	        sGen.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);
	        
	        Iterator    it = pgpSec.getPublicKey().getUserIDs();
	        if (it.hasNext())
	        {
	            PGPSignatureSubpacketGenerator  spGen = new PGPSignatureSubpacketGenerator();
	            
	            spGen.setSignerUserID(false, (String)it.next());
	            sGen.setHashedSubpackets(spGen.generate());
	        }
	        
	        PGPCompressedDataGenerator  cGen = new PGPCompressedDataGenerator(
	                                                                PGPCompressedData.ZLIB);
	        
	        BCPGOutputStream            bOut = new BCPGOutputStream(cGen.open(out));
	        
	        sGen.generateOnePassVersion(false).encode(bOut);
	        
	        File                        file = new File(fileName);
	        PGPLiteralDataGenerator     lGen = new PGPLiteralDataGenerator();
	        OutputStream                lOut = lGen.open(bOut, PGPLiteralData.BINARY, file);
	        FileInputStream             fIn = new FileInputStream(file);
	        int                         ch;
	        
	        while ((ch = fIn.read()) >= 0)
	        {
	            lOut.write(ch);
	            sGen.update((byte)ch);
	        }
	
	        lGen.close();
	
	        sGen.generate().encode(bOut);
	
	        cGen.close();

	        if (armor)
	        {
	            out.close();
	        }
	}
    
    
	public String signKey(String pub){
		PGPPublicKeyRing pgpRings;
		try {
			pgpRings = new PGPPublicKeyRing(Base64.decode(pub), new JcaKeyFingerprintCalculator());
	    	return sign(pgpRings.getPublicKey());
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PGPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}

    private PGPSecretKey readSecretKey(
        InputStream    in)
        throws IOException, PGPException
    {
        PGPSecretKeyRingCollection        pgpSec = new PGPSecretKeyRingCollection(in, new JcaKeyFingerprintCalculator());

        PGPSecretKey    key = null;

        //
        // iterate through the key rings.
        //
        Iterator rIt = pgpSec.getKeyRings();

        while (key == null && rIt.hasNext())
        {
            PGPSecretKeyRing    kRing = (PGPSecretKeyRing)rIt.next();
            Iterator            kIt = kRing.getSecretKeys();
    
            while (key == null && kIt.hasNext())
            {
                PGPSecretKey    k = (PGPSecretKey)kIt.next();
    
                if (k.isSigningKey())
                {
                    key = k;
                }
            }
        }
    
        if (key == null)
        {
            throw new IllegalArgumentException("Can't find signing key in key ring.");
        }
    
        return key;
    }
    
    private String sign(PGPPublicKey publicKeyToBeSigned) throws IOException, PGPException{
    	
    	PGPSecretKey                    pgpSecKey = readSecretKey(new ByteArrayInputStream(secretCA));
        PGPPrivateKey                   pgpPrivKey = pgpSecKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder(new JcaPGPDigestCalculatorProviderBuilder().setProvider("BC").build()).setProvider("BC").build(pass));
//    	PGPPrivateKey                   pgpPrivKey=pgpSecKey.extractPrivateKey(new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build("hello".toCharArray()));
        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder(pgpSecKey.getPublicKey()
                        .getAlgorithm(), PGPUtil.SHA1));
        signatureGenerator.init(PGPSignature.DIRECT_KEY, pgpPrivKey);

        PGPSignature signature = signatureGenerator.generateCertification(pgpSecKey.getPublicKey().getUserIDs().next().toString(), publicKeyToBeSigned);
        publicSign=PGPPublicKey.addCertification(publicKeyToBeSigned, signature);
        return Base64.toBase64String(publicSign.getEncoded());

    }
    
    private PGPKeyRingGenerator generateKeyRingGenerator(String id, char[] pass) throws Exception
    	{ return generateKeyRingGenerator(id, pass, 0xc0); }

// Note: s2kcount is a number between 0 and 0xff that controls the
// number of times to iterate the password hash before use. More
// iterations are useful against offline attacks, as it takes more
// time to check each password. The actual number of iterations is
// rather complex, and also depends on the hash function in use.
// Refer to Section 3.7.1.3 in rfc4880.txt. Bigger numbers give
// you more iterations.  As a rough rule of thumb, when using
// SHA256 as the hashing function, 0x10 gives you about 64
// iterations, 0x20 about 128, 0x30 about 256 and so on till 0xf0,
// or about 1 million iterations. The maximum you can go to is
// 0xff, or about 2 million iterations.  I'll use 0xc0 as a
// default -- about 130,000 iterations.

private PGPKeyRingGenerator generateKeyRingGenerator
    (String id, char[] pass, int s2kcount)
    throws Exception
{
    Security.addProvider(new BouncyCastleProvider());

    // Add a self-signature on the id
    PGPSignatureSubpacketGenerator signhashgen =
        new PGPSignatureSubpacketGenerator();
    
    // Add signed metadata on the signature.
    // 1) Declare its purpose
    signhashgen.setKeyFlags
        (false, KeyFlags.SIGN_DATA|KeyFlags.CERTIFY_OTHER);
    // 2) Set preferences for secondary crypto algorithms to use
    //    when sending messages to this key.
    signhashgen.setPreferredSymmetricAlgorithms
        (false, new int[] {
            SymmetricKeyAlgorithmTags.AES_256,
            SymmetricKeyAlgorithmTags.AES_192,
            SymmetricKeyAlgorithmTags.AES_128
        });
    signhashgen.setPreferredHashAlgorithms
        (false, new int[] {
            HashAlgorithmTags.SHA256,
            HashAlgorithmTags.SHA1,
            HashAlgorithmTags.SHA384,
            HashAlgorithmTags.SHA512,
            HashAlgorithmTags.SHA224,
        });
    // 3) Request senders add additional checksums to the
    //    message (useful when verifying unsigned messages.)
    signhashgen.setFeature
        (false, Features.FEATURE_MODIFICATION_DETECTION);

    // Create a signature on the encryption subkey.
    PGPSignatureSubpacketGenerator enchashgen =
        new PGPSignatureSubpacketGenerator();
    // Add metadata to declare its purpose
    enchashgen.setKeyFlags
        (false, KeyFlags.ENCRYPT_COMMS|KeyFlags.ENCRYPT_STORAGE);

    
    
    KeyPairGenerator    rsaKpg = KeyPairGenerator.getInstance("RSA", "BC");

    rsaKpg.initialize(512);

    //
    // this is quicker because we are using pregenerated parameters.
    //
    KeyPair           rsaKp = rsaKpg.generateKeyPair();
    PGPKeyPair        rsaKeyPair1 = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, rsaKp, new Date());
                      rsaKp = rsaKpg.generateKeyPair();
    PGPKeyPair        rsaKeyPair2 = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, rsaKp, new Date());
    PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
    PGPKeyRingGenerator    keyRingGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, rsaKeyPair1,
                        id, sha1Calc, signhashgen.generate(), null, new JcaPGPContentSignerBuilder(PGPPublicKey.RSA_SIGN, HashAlgorithmTags.SHA1), new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256).setProvider("BC").build(pass));
//    PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, rsakp_sign,
//            id, sha1Calc, signhashgen.generate(), null, new JcaPGPContentSignerBuilder(PGPPublicKey.RSA_SIGN, HashAlgorithmTags.SHA1), pske);
    keyRingGen.addSubKey
    	(rsaKeyPair2, enchashgen.generate(), null);
    
    return keyRingGen;
}



public boolean generateKey(String userid, String password)
    {
		boolean res=false;
		
        char pass[] = password.toCharArray();
        PGPKeyRingGenerator krgen;
        PGPPublicKeyRing pkr;
        PGPSecretKeyRing skr;
        try {
			krgen = generateKeyRingGenerator
			    (userid, pass);
			// Generate public key ring, dump to file.
	        pkr = krgen.generatePublicKeyRing();

	        sign(pkr.getPublicKey());

	        // Generate private key, dump to file.
	        skr = krgen.generateSecretKeyRing();
	        secKey=skr.getSecretKey();
	        res=true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return res;
        
    }

/*
 * verify the passed in file as being correctly signed.
 */
public boolean verifyFile(
    byte[]        in,
    String        pk,
    String path)
    throws Exception
{
    
    JcaPGPObjectFactory            pgpFact = new JcaPGPObjectFactory(in);

    PGPCompressedData           c1 = (PGPCompressedData)pgpFact.nextObject();

    pgpFact = new JcaPGPObjectFactory(c1.getDataStream());
        
    PGPOnePassSignatureList     p1 = (PGPOnePassSignatureList)pgpFact.nextObject();
        
    PGPOnePassSignature         ops = p1.get(0);
        
    PGPLiteralData              p2 = (PGPLiteralData)pgpFact.nextObject();

    InputStream                 dIn = p2.getInputStream();
    int                         ch;
    
    PGPPublicKeyRingCollection  pgpRing = new PGPPublicKeyRingCollection(Base64.decode(pk), new JcaKeyFingerprintCalculator());

    PGPPublicKey                key = pgpRing.getPublicKey(ops.getKeyID());
    
    
    Path dir = Paths.get(path);
    if(!Files.exists(dir)) {
        try {
          Files.createDirectories(dir);
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
    
    FileOutputStream            out = new FileOutputStream(path+p2.getFileName());
    filename=path+p2.getFileName();
    
    
    ops.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), key);
        
    while ((ch = dIn.read()) >= 0)
    {
        ops.update((byte)ch);
        out.write(ch);
    }

    out.close();
    
    PGPSignatureList            p3 = (PGPSignatureList)pgpFact.nextObject();
    
    if (ops.verify(p3.get(0)))
    {
        System.out.println("signature verified.");
        return true;
    }
    return false;
}
}
