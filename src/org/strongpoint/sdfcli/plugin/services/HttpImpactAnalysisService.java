package org.strongpoint.sdfcli.plugin.services;

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

public class HttpImpactAnalysisService {
	
	public static HttpImpactAnalysisService newInstance() {
		return new HttpImpactAnalysisService();
	}
	
	public JSONObject getImpactAnalysis(String changeRequestId) {
		JSONObject results = new JSONObject();
		
		String strongpointURL = "https://rest.netsuite.com/app/site/hosting/restlet.nl?script=3755&deploy=1&crId=" + changeRequestId;
		System.out.println(strongpointURL);
		HttpGet httpGet = null;
		int statusCode;
		String responseBodyStr;
		CloseableHttpResponse response = null;
		try {
        	CloseableHttpClient client = HttpClients.createDefault();
            httpGet = new HttpGet(strongpointURL);
            httpGet.addHeader("Authorization", "NLAuth nlauth_account=TSTDRV1267181, nlauth_email=joanna.paclibar@strongpoint.io, nlauth_signature=FLODocs1234!, nlauth_role=3");
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            statusCode = response.getStatusLine().getStatusCode();
            responseBodyStr = EntityUtils.toString(entity);
			
			if(statusCode >= 400) {
				throw new RuntimeException("HTTP Request returns a " +statusCode);
			}
			results = (JSONObject) JSONValue.parse(responseBodyStr);
		} catch (Exception exception) {
			System.out.println("Request Deployment call error: " +exception.getMessage());
			throw new RuntimeException("Request Deployment call error: " +exception.getMessage());
		} finally {
			if (httpGet != null) {
				httpGet.reset();
			}
		}
		
		return results;
	}
	
}
