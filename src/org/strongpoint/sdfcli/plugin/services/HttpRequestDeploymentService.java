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
}
