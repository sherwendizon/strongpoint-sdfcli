package org.strongpoint.sdfcli.plugin.services;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class HttpRequestDeployment {
	
	public static HttpRequestDeployment newInstance() {
		return new HttpRequestDeployment();
	}
	
	public JSONObject requestDeployment(JSONObject parameters) {
		JSONObject results = new JSONObject();
		
		// This is a test URL, replace this when Stronpoint URL is provided.
		String strongpointURL = "http://echo.jsontest.com/key/value/one/two";
		
		HttpPost httpPost = null;
		int statusCode;
		String responseBodyStr;
		
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			httpPost = new HttpPost(strongpointURL);
//			httpPost.addHeader("Authorization", "authorization_here");
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
}
