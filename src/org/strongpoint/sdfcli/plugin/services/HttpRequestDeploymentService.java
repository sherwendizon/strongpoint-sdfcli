package org.strongpoint.sdfcli.plugin.services;

import java.io.ObjectOutputStream.PutField;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class HttpRequestDeploymentService {
	
	public static HttpRequestDeploymentService newInstance() {
		return new HttpRequestDeploymentService();
	}
	
	public JSONObject requestDeployment(JSONObject parameters) {
		JSONObject results = new JSONObject();
		
		String strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=3754&deploy=1";
		
		HttpPost httpPost = null;
		int statusCode;
		String responseBodyStr;
		
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			httpPost = new HttpPost(strongpointURL);
			httpPost.addHeader("Authorization", "NLAuth nlauth_account=TSTDRV1267181, nlauth_email=joanna.paclibar@strongpoint.io, nlauth_signature=FLODocs1234!, nlauth_role=3");
			System.out.println(parameters.toJSONString());
			httpPost.addHeader("Content-type", "application/json");
			StringEntity stringEntity = new StringEntity(parameters.toJSONString(), ContentType.APPLICATION_JSON);
			httpPost.setEntity(stringEntity);
			CloseableHttpResponse httpResponse = client.execute(httpPost);
			HttpEntity entity = httpResponse.getEntity();
			statusCode = httpResponse.getStatusLine().getStatusCode();
			responseBodyStr = EntityUtils.toString(entity);
			
			if(statusCode >= 400) {
				throw new RuntimeException("HTTP Request returns a " +statusCode);
			}
			results = (JSONObject) JSONValue.parse(responseBodyStr);
		} catch (Exception exception) {
			System.out.println("Request Deployment call error: " +exception.getMessage());
			throw new RuntimeException("Request Deployment call error: " +exception.getMessage());
		} finally {
			if (httpPost != null) {
				httpPost.reset();
			}
		}
		
		return results;
	}
	
	public JSONArray getEmployees() {
		JSONArray results;
        String urlString = "Strongpoint getEmployees URL here";
        int statusCode;
        String strRespBody;
        HttpGet httpGet = null;
        CloseableHttpResponse response = null;
        try {
        	CloseableHttpClient client = HttpClients.createDefault();
            httpGet = new HttpGet(urlString);
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            statusCode = response.getStatusLine().getStatusCode();
            strRespBody = EntityUtils.toString(entity);
            if (statusCode >= 400) {
            	throw new RuntimeException("HTTP Request returns a " +statusCode);
            } else {
                results = (JSONArray) JSONValue.parse(strRespBody);
            }
        } catch (Exception exception) {
			System.out.println("Request for all Requestors call error: " + exception.getMessage());
			throw new RuntimeException("Request for all Requestors call error: " + exception.getMessage());
        } finally {
			if (httpGet != null) {
				httpGet.reset();
			}
        }
        return results;
	}
	
	public JSONArray getChangeTypes() {
		JSONArray results;
        String urlString = "Strongpoint getChangeTypes URL here";
        int statusCode;
        String strRespBody;
        HttpGet httpGet = null;
        CloseableHttpResponse response = null;
        try {
        	CloseableHttpClient client = HttpClients.createDefault();
            httpGet = new HttpGet(urlString);
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            statusCode = response.getStatusLine().getStatusCode();
            strRespBody = EntityUtils.toString(entity);
            if (statusCode >= 400) {
            	throw new RuntimeException("HTTP Request returns a " +statusCode);
            } else {
                results = (JSONArray) JSONValue.parse(strRespBody);
            }
        } catch (Exception exception) {
			System.out.println("Request for all Requestors call error: " + exception.getMessage());
			throw new RuntimeException("Request for all Requestors call error: " + exception.getMessage());
        } finally {
			if (httpGet != null) {
				httpGet.reset();
			}
        }
        return results;
	}
	
	public JSONArray changeTypeTest() {
		JSONArray jsonArray = new JSONArray();
		
		JSONObject obj1 = new JSONObject();
		obj1.put("text", "Minor");
		obj1.put("value", 1);
		jsonArray.add(obj1);
		
		JSONObject obj2 = new JSONObject();
		obj2.put("text", "Major");
		obj2.put("value", 2);
		jsonArray.add(obj2);
		
		JSONObject obj3 = new JSONObject();
		obj3.put("text", "New Process");
		obj3.put("value", 3);
		jsonArray.add(obj3);
		
		JSONObject obj4 = new JSONObject();
		obj4.put("text", "New Feature");
		obj4.put("value", 4);
		jsonArray.add(obj4);
		
		JSONObject obj5 = new JSONObject();
		obj5.put("text", "Clean-up");
		obj5.put("value", 5);
		jsonArray.add(obj5);
		
		return jsonArray;
	}
	
	public JSONArray employeeTest() {
		JSONArray jsonArray = new JSONArray();
		
		JSONObject obj1 = new JSONObject();
		obj1.put("id", 123);
		obj1.put("name", "Joanna Paclibar");
		jsonArray.add(obj1);
		
		JSONObject obj2 = new JSONObject();
		obj2.put("id", 456);
		obj2.put("name", "Beverlyn Ucab");
		jsonArray.add(obj2);
		
		JSONObject obj3 = new JSONObject();
		obj3.put("id", 789);
		obj3.put("name", "John Albura");
		jsonArray.add(obj3);
		
		return jsonArray;
	}
}
