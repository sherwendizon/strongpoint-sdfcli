package org.strongpoint.sdfcli.plugin.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.strongpoint.sdfcli.plugin.services.HttpTestConnectionService;

public class Accounts {

	private static final String userHomePath = System.getProperty("user.home");

	public static final JSONArray getAccountsFromFile() {
		StringBuilder contents = new StringBuilder();
		String str;
		File file = new File(userHomePath + "/sdfcli/accounts.json");
		JSONArray accounts = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((str = reader.readLine()) != null) {
				contents.append(str);
			}
//			System.out.println("FILE Contents: " +contents.toString());
			accounts = (JSONArray) new JSONParser().parse(contents.toString());
		} catch (FileNotFoundException e) {
			StrongpointLogger.logger(Accounts.class.getName(), "error", e.getMessage());
		} catch (IOException e) {
			StrongpointLogger.logger(Accounts.class.getName(), "error", e.getMessage());
		} catch (ParseException e) {
			StrongpointLogger.logger(Accounts.class.getName(), "error", e.getMessage());
		}
		return accounts;
	}

	public static final String[] getAccountsStrFromFile() {
		String[] accounts = { "No account/s available" };
		if (getAccountsFromFile() != null && !getAccountsFromFile().isEmpty()) {
			accounts = new String[getAccountsFromFile().size()];
			StrongpointLogger.logger(Accounts.class.getName(), "info", "Accounts: " + getAccountsFromFile().toJSONString());
			StrongpointLogger.logger(Accounts.class.getName(), "info", "Accounts Size: " + getAccountsFromFile().size());
			for (int i = 0; i < getAccountsFromFile().size(); i++) {
				JSONObject obj = (JSONObject) getAccountsFromFile().get(i);
				StrongpointLogger.logger(Accounts.class.getName(), "info", "Account: " + obj.toJSONString());
				accounts[i] = obj.get("accountName") + " (" + obj.get("accountId") + ")";
			}
		}
		return accounts;
	}

	public static final boolean isSandboxAccount(String accountId) {
		boolean isSandbox = false;
		JSONObject credentials = Credentials.getCredentialsFromFile();
		JSONArray sandBoxRoles = (JSONArray) credentials.get("roles");
		for (int i = 0; i < sandBoxRoles.size(); i++) {
			JSONObject sbRole = (JSONObject) sandBoxRoles.get(i);
			if (sbRole.get("accountId").toString().equalsIgnoreCase(accountId)
					&& sbRole.get("accountType").toString().equalsIgnoreCase("sandbox")) {
				isSandbox = true;
			}
		}

		return isSandbox;
	}
	
	public static final String getSandboxRestDomain(String accountId) {
		String restDomain = "https://rest.sandbox.netsuite.com";
		JSONObject credentials = Credentials.getCredentialsFromFile();
		JSONArray sandBoxRoles = (JSONArray) credentials.get("roles");
		for (int i = 0; i < sandBoxRoles.size(); i++) {
			JSONObject sbRole = (JSONObject) sandBoxRoles.get(i);
			if (sbRole.get("accountId").toString().equalsIgnoreCase(accountId)
					&& sbRole.get("accountType").toString().equalsIgnoreCase("sandbox")
					&& sbRole.get("roleName").toString().equalsIgnoreCase("Strongpoint SDF Developer Role")) {
				restDomain = sbRole.get("accountDomain").toString();
			}
		}		
		
		return restDomain;
	}
	
	public static final String getProductionRestDomain(String accountId) {
		String restDomain = "https://rest.netsuite.com";
		JSONObject credentials = Credentials.getCredentialsFromFile();
		JSONArray sandBoxRoles = (JSONArray) credentials.get("roles");
		for (int i = 0; i < sandBoxRoles.size(); i++) {
			JSONObject sbRole = (JSONObject) sandBoxRoles.get(i);
			if (sbRole.get("accountId").toString().equalsIgnoreCase(accountId)
					/*&& sbRole.get("accountType").toString().equalsIgnoreCase("production")*/
					&& sbRole.get("roleName").toString().equalsIgnoreCase("Strongpoint SDF Developer Role")) {
				restDomain = sbRole.get("accountDomain").toString();
			}
		}		
		
		return restDomain;
	}

}
