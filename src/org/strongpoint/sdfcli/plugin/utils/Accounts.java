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

public class Accounts {
	
	private static final String userHomePath = System.getProperty("user.home");
	
	public static final JSONArray getAccountsFromFile() {
		StringBuilder contents = new StringBuilder();
		String str;
		File file = new File(userHomePath + "/sdfcli/accounts.json");
		JSONArray accounts = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while((str = reader.readLine())  != null) {
				contents.append(str);
			}
			System.out.println("FILE Contents: " +contents.toString());
			accounts = (JSONArray) new JSONParser().parse(contents.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return accounts;
	}
	
	public static final String[] getAccountsStrFromFile() {
		String[] accounts = {"No account/s available"};		
		if(getAccountsFromFile() != null && !getAccountsFromFile().isEmpty()) {
			accounts = new String[getAccountsFromFile().size()];
			System.out.println("Accounts: " +getAccountsFromFile().toJSONString());
			System.out.println("Accounts Size: " +getAccountsFromFile().size());
			for (int i = 0; i < getAccountsFromFile().size(); i++) {
				JSONObject obj = (JSONObject) getAccountsFromFile().get(i);
				System.out.println("Account: " +obj.toJSONString());
				accounts[i] = obj.get("accountName") + "(" +obj.get("accountId") + ")";
			}	
		}
		return accounts;
	}

}
