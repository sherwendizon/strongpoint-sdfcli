package org.strongpoint.sdfcli.plugin.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.core.internal.preferences.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Credentials {

	private static final String userHomePath = System.getProperty("user.home");

	public static final JSONObject getCredentialsFromFile() {
		StringBuilder contents = new StringBuilder();
		String str;
		File file = new File(userHomePath + "/sdfcli/credentials.json");
		JSONObject credentials = null;
		try {
			if (file.exists() && !file.isDirectory()) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while ((str = reader.readLine()) != null) {
					contents.append(str);
				}
//				System.out.println("FILE Contents: " +contents.toString());
				credentials = (JSONObject) new JSONParser().parse(contents.toString());
			}
		} catch (FileNotFoundException e) {
			StrongpointLogger.logger(Credentials.class.getName(), "error", e.getMessage());
		} catch (IOException e) {
			StrongpointLogger.logger(Credentials.class.getName(), "error", e.getMessage());
		} catch (ParseException e) {
			StrongpointLogger.logger(Credentials.class.getName(), "error", e.getMessage());
		}
		return credentials;
	}

	public static final boolean isCredentialsFileExists() {
		File file = new File(userHomePath + "/sdfcli/credentials.json");
		if (file.exists() && !file.isDirectory()) {
			return true;
		} else {
			return false;
		}
	}

	public static final String getSDFRoleIdParam(String accountId, boolean isUsedForParameter) {
		String role = "";
		if(!isUsedForParameter) {
			role = " - User has no Strongpoint SDF Developer Role for this account: " + accountId;
		}
		JSONObject credentials = getCredentialsFromFile();
		JSONArray roles = (JSONArray) credentials.get("roles");
		for (int i = 0; i < roles.size(); i++) {
			JSONObject credRoleObject = (JSONObject) roles.get(i);
			if (credRoleObject.get("accountId").toString().equalsIgnoreCase(accountId)
					&& credRoleObject.get("roleName").toString().equalsIgnoreCase("Strongpoint SDF Developer Role")) {
				role = credRoleObject.get("roleId").toString();
			}
		}

		return role;
	}
	
	public static String decryptPass(byte[] pass, String keyStr) {
		byte[] encodedKey = new Base64().decode(keyStr.getBytes());
		Key key = new SecretKeySpec(encodedKey,0,encodedKey.length, "AES"); 
		Cipher decryptCipher;
		try {
			decryptCipher = Cipher.getInstance("AES");
			decryptCipher.init(Cipher.DECRYPT_MODE, key);
			byte[] dec = new Base64().decode(pass);
			byte[] utf8 = decryptCipher.doFinal(dec);
			return new String(utf8, "UTF8");
		} catch (Exception e) {
			StrongpointLogger.logger(Credentials.class.getName(), "error", e.getMessage());
		}
		return null;		
	}

}
