package org.strongpoint.sdfcli.plugin.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.internal.preferences.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.strongpoint.sdfcli.plugin.utils.Accounts;
import org.strongpoint.sdfcli.plugin.utils.Credentials;
import org.strongpoint.sdfcli.plugin.utils.StrongpointDirectoryGeneralUtility;
import org.strongpoint.sdfcli.plugin.utils.jobs.StrongpointJobManager;

public class AddEditCredentialsService {
	
	private final static String userHomePath = System.getProperty("user.home");
	
	private final static String osName = System.getProperty("os.name").toLowerCase();
	
	private static final String AES_CIPHER = "AES";
	
	private static final byte[] keyValue = new byte[] { 'A', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k','l', 'm', 'n', 'o', 'p'};	
	
	private String emailStr;
	
	private String passwordStr;
	
	public String getEmailStr() {
		return emailStr;
	}

	public void setEmailStr(String emailStr) {
		this.emailStr = emailStr;
	}

	public String getPasswordStr() {
		return passwordStr;
	}

	public void setPasswordStr(String passwordStr) {
		this.passwordStr = passwordStr;
	}

	public void writeToJSONFile() {
		String pass = "";
		String passKey = "";
		StrongpointDirectoryGeneralUtility.newInstance().createSdfcliDirectory();
		JSONObject obj = new JSONObject();
		Key key = generateKey();
//	    String encryptStrPass = encryptPasswd(this.passwordStr, key);
		if(Credentials.isCredentialsFileExists()) {
			JSONObject cred = Credentials.getCredentialsFromFile();
			pass = cred.get("password").toString();
			passKey = cred.get("key").toString();
		} else {
			pass = encryptPasswd(this.passwordStr, key);
			passKey = new String(new Base64().encode(key.getEncoded()));
		}
		JSONArray roles = accountsAndRoles(getAccountRoles(this.emailStr, pass, passKey));
		System.out.println("Roles: " +roles);
	    obj.put("email", this.emailStr);
	    obj.put("password", pass);
	    obj.put("roles", roles);
	    obj.put("key", passKey);
//	    encryptPassword(this.passwordStr);
	    // decipher key/value
//		Cipher decryptCipher;
//		try {
//			decryptCipher = Cipher.getInstance(ALGO);
//			decryptCipher.init(Cipher.DECRYPT_MODE, generateKey());
//			System.out.println("Key: " +new String(new Base64().encode(key.getEncoded())));
//			System.out.println("Decrypted Password: " +Credentials.decryptPass(encryptStrPass.getBytes(), decryptCipher));
//		} catch (NoSuchAlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchPaddingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidKeyException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	    // decipher key/value
//	    obj.put("sandboxRoles", sandboxRoles);
//	    String path = userHomePath.replace("\\", "") + "/sdfcli/";
	    String path = "";
	    if(osName.indexOf("win") >= 0) {
	    	path = userHomePath.replace("\\", "/");
	    	obj.put("path", path + "/sdfcli/");
	    } else {    	
	    	System.out.println("NON-WINDOWS: " +userHomePath + "\\sdfcli\\");
	    	path = userHomePath.replace("\\", "") + "/sdfcli/";
	    	obj.put("path", path.replace("\\", ""));
	    }
//	    obj.put("path", path.replace("\\", ""));
	
	    try {
	        File file = new File( userHomePath + "/sdfcli/" + "credentials.json");
	        if(file.exists() && !file.isDirectory()) {
	            FileWriter writer = new FileWriter(file);
	            System.out.println("Writing JSON object to file...");
	            System.out.println(obj);
	            System.out.println("After adding new account: " +obj.toJSONString());
	            writer.write(obj.toJSONString().replace("\\", ""));
	            writer.flush();
	            writer.close();	
	        } else {
	            boolean isCreated = file.createNewFile();
	            if(isCreated) {
	                System.out.println("File successfully created!");
	            } else {
	                System.out.println("File not created!");
	            }
	
	            FileWriter writer = new FileWriter(file);
	            System.out.println("Writing JSON object to file...");
	            System.out.println(obj);
	            writer.write(obj.toJSONString().replace("\\", ""));
	            writer.flush();
	            writer.close();	
	        }
	    } catch (IOException exception) {
	        exception.printStackTrace();
	    }		
	}
	
	private JSONObject getAccountRoles(String email, String password, String key) {
		String passString = Credentials.decryptPass(password.getBytes(), key);
		String errorMessage = "Error during getting user accounts and roles: ";
		JSONObject results = new JSONObject();
//		String strongpointURL = "https://forms.netsuite.com/app/site/hosting/scriptlet.nl?script=1145&deploy=1&compid=TSTDRV1049933&h=9b265f0bc6e8cc5f673e&email="+email+"&pass="+password;
 		String strongpointURL = "https://rest.netsuite.com/rest/roles";
		System.out.println(strongpointURL);
		HttpGet httpGet = null;
		int statusCode;
		String responseBodyStr;
		CloseableHttpResponse response = null;
		try {
        	CloseableHttpClient client = HttpClients.createDefault();
            httpGet = new HttpGet(strongpointURL);
            httpGet.addHeader("Authorization", "NLAuth nlauth_email=" + email + ", nlauth_signature=" + passString);
            httpGet.addHeader("Content-type", "application/json");
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            statusCode = response.getStatusLine().getStatusCode();
            responseBodyStr = EntityUtils.toString(entity);
            
			if(statusCode >= 400) {
				results = new JSONObject();
				results.put("message", errorMessage);
				results.put("data", null);
				results.put("code", statusCode);
			} else {
				JSONArray arrayResults = (JSONArray) JSONValue.parse(responseBodyStr); 
				results.put("results", arrayResults);	
			}
		} catch (Exception exception) {
			results = new JSONObject();
			results.put("message", exception.getMessage());
			results.put("data", null);
			results.put("code", 400);
		} finally {
			if (httpGet != null) {
				httpGet.reset();
			}
		}
				
		return results;
	}
	
	private JSONArray accountsAndRoles(JSONObject results) {
		JSONArray accountsAndRolesArray = new JSONArray();
		JSONArray dataCenterResults = (JSONArray) results.get("results");
		
		if(dataCenterResults != null && !dataCenterResults.isEmpty()) {
			for (int i = 0; i < dataCenterResults.size(); i++) {
				JSONObject obj = new JSONObject();
				JSONObject dataObject = (JSONObject) dataCenterResults.get(i);
				JSONObject roleObject = (JSONObject) dataObject.get("role");
				obj.put("roleId", roleObject.get("internalId").toString());
				obj.put("roleName", roleObject.get("name").toString());
				JSONObject accountObject = (JSONObject) dataObject.get("account");
				obj.put("accountId", accountObject.get("internalId").toString());
				obj.put("accountName", accountObject.get("name").toString());
				obj.put("accountType", accountObject.get("type").toString());
				JSONObject dataCenterUrlObject = (JSONObject) dataObject.get("dataCenterURLs");
				obj.put("accountDomain", dataCenterUrlObject.get("restDomain").toString());
				
				accountsAndRolesArray.add(obj);
			}			
		}
		
		return accountsAndRolesArray;
	}
	
//	private Map<SecretKey, String> encryptPassword(String password) {
//		Map<SecretKey, String> encrytedPassword = new HashMap<>();
//		SecretKey key;
//		Cipher encryptCipher;
//		String encrytedStr;
//		try {
//			key = KeyGenerator.getInstance("DES").generateKey();
//			encryptCipher = Cipher.getInstance("DES");
//			encryptCipher.init(Cipher.ENCRYPT_MODE, key);
//			byte[] encrypt = encryptPass(password, encryptCipher);
//			encrytedStr = new String(encrypt);
//			System.out.println("Key: " +new String(new Base64().encode(key.getEncoded())));
//			System.out.println("Encrypted Password: " +encrytedStr);
//			
////			Cipher decryptCipher = Cipher.getInstance("DES");
////			decryptCipher.init(Cipher.DECRYPT_MODE, key);
////			System.out.println("Key: " +new String(new Base64().encode(key.getEncoded())));
////			System.out.println("Decrypted Password: " +Credentials.decryptPass(encrypt, decryptCipher));
//			encrytedPassword.put(key, encrytedStr);
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} catch (NoSuchPaddingException e) {
//			e.printStackTrace();
//		} catch (InvalidKeyException e) {
//			e.printStackTrace();
//		}
//		
//		return encrytedPassword;
//	}
	
	private static Key generateKey() {
		Key key = new SecretKeySpec(keyValue, AES_CIPHER);
		return key;
	}
	
	private String encryptPasswd(String pass, Key keyGen) {
	    Key key = generateKey();
	    Cipher cipher;
	    byte[] encryptedValue = null;
		try {
			cipher = Cipher.getInstance(AES_CIPHER);
			cipher.init(Cipher.ENCRYPT_MODE, key);
		    byte[] encryptedByteArr = cipher.doFinal(pass.getBytes());
		    encryptedValue = new Base64().encode(encryptedByteArr);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
	    return new String(encryptedValue);		
	}
	
//	private static byte[] encryptPass(String pass, Cipher encryptCipher) {
//		try {
//			byte[] utf8 = pass.getBytes("UTF8");
//			byte[] enc = encryptCipher.doFinal(utf8);
//			return new Base64().encode(enc);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;		
//	}
	
//	private static String decryptPass(byte[] pass, Cipher decryptCipher) {
//		try {
//			byte[] dec = new Base64().decode(pass);
//			byte[] utf8 = decryptCipher.doFinal(dec);
//			return new String(utf8, "UTF8");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;		
//	}
	
	// Sandbox Account
//	private JSONObject getSandboxAccountRoles(String email, String password) {
//		String errorMessage = "Error during getting user sandbox accounts and roles: ";
//		System.out.println("Get sandbox account roles - Email: " +email+ " Password: " +password);
//		JSONObject results = new JSONObject();
////		String strongpointURL = "https://forms.netsuite.com/app/site/hosting/scriptlet.nl?script=1145&deploy=1&compid=TSTDRV1049933&h=9b265f0bc6e8cc5f673e&email="+email+"&pass="+password;
// 		String strongpointURL = "https://rest.sandbox.netsuite.com/rest/roles";
//		System.out.println(strongpointURL);
//		HttpGet httpGet = null;
//		int statusCode;
//		String responseBodyStr;
//		CloseableHttpResponse response = null;
//		try {
//        	CloseableHttpClient client = HttpClients.createDefault();
//            httpGet = new HttpGet(strongpointURL);
//            httpGet.addHeader("Authorization", "NLAuth nlauth_email=" + email + ", nlauth_signature=" + password);
//            httpGet.addHeader("Content-type", "application/json");
//            response = client.execute(httpGet);
//            HttpEntity entity = response.getEntity();
//            statusCode = response.getStatusLine().getStatusCode();
//            responseBodyStr = EntityUtils.toString(entity);
//            
//			if(statusCode >= 400) {
//				results = new JSONObject();
//				results.put("message", errorMessage);
//				results.put("results", null);
//				results.put("code", statusCode);
//			} else {
//				JSONArray arrayResults = (JSONArray) JSONValue.parse(responseBodyStr); 
//				results.put("results", arrayResults);	
//			}
//		} catch (Exception exception) {
//			results = new JSONObject();
//			results.put("message", exception.getMessage());
//			results.put("results", null);
//			results.put("code", 400);
//		} finally {
//			if (httpGet != null) {
//				httpGet.reset();
//			}
//		}
//		
//		return results;
//	}

}
