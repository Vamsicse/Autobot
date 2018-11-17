package env;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import util.IOUtil;

public class Test2 {
    public static void main(String[] args) throws IOException, ClientProtocolException {
        new Test2().doPatch();


    }

    public void doPatch() throws IOException, ClientProtocolException {
        HttpPatch httpPatch =
            new HttpPatch("https://slcai762.us.oracle.com:10616/crmCommonApi/resources/latest/accounts/CDRM_662103");
        httpPatch.addHeader("Authorization", "Basic bWhvb3BlOldlbGNvbWUx");
        httpPatch.addHeader("Content-Type", "application/vnd.oracle.adf.resourceitem+json");
        String payload="{\"OrganizationName\": \"MyOrg27121991\"}";
        StringEntity se = new StringEntity(payload);
        httpPatch.setEntity(se);
        
        DefaultHttpClient httpClient = new DefaultHttpClient();
        
        HttpResponse httpResponse = httpClient.execute(httpPatch);
        
        System.out.println("Status: " + httpResponse.getStatusLine().getStatusCode());

        BufferedReader br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "utf-8"));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        //System.out.println(sb.toString());
        IOUtil.writeToFile("D:\\Softwares\\MyProject\\RestAutomationApplication\\RESTAutomationFramework\\src\\payloads\\response\\", "PatchResp.txt", sb.toString());
    }


    public void doPost() throws IOException, ClientProtocolException {
        HttpPost httpPost =
            new HttpPost("https://slcai762.us.oracle.com:10616/crmCommonApi/resources/latest/accounts");
        httpPost.addHeader("Authorization", "Basic bWhvb3BlOldlbGNvbWUx");
        httpPost.addHeader("Content-Type", "application/vnd.oracle.adf.resourceitem+json");
        String payload="{\"OrganizationName\": \"MyOrg27\"}";
        StringEntity se = new StringEntity(payload);
        httpPost.setEntity(se);
        
        DefaultHttpClient httpClient = new DefaultHttpClient();
        
        HttpResponse httpResponse = httpClient.execute(httpPost);
        
        System.out.println("Status: " + httpResponse.getStatusLine().getStatusCode());

        BufferedReader br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "utf-8"));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line).append(System.getProperty("line.separator"));
        }
        System.out.println(sb.toString());
        IOUtil.writeToFile("D:\\Softwares\\MyProject\\RestAutomationApplication\\RESTAutomationFramework\\src\\payloads\\response\\", "PostResp.txt", sb.toString());
    }


    public void doGet() throws IOException, ClientProtocolException {
        HttpGet httpGet =
            new HttpGet("https://slcai762.us.oracle.com:10616/crmCommonApi/resources/latest/accounts/describe");
        httpGet.addHeader("Authorization", "Basic bWhvb3BlOldlbGNvbWUx");

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = httpClient.execute(httpGet);
        System.out.println("Status: " + httpResponse.getStatusLine().getStatusCode());

        BufferedReader br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "utf-8"));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line).append(System.getProperty("line.separator"));
        }
        //System.out.println(sb.toString());
        IOUtil.writeToFile("D:\\Softwares\\MyProject\\RestAutomationApplication\\RESTAutomationFramework\\src\\payloads\\response\\", "GetResp.txt", sb.toString());
    }
}
