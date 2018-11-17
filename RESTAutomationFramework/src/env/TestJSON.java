package env;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONObject;

import sun.misc.BASE64Encoder;

import util.IOUtil;
import util.TrustUtil;

public class TestJSON {
    public static void main(String[] args) {
        
        String restURL = "https://slc03vhf.dev.oraclecorp.com:10616/crmCommonApi/resources/latest/accounts/";
        // String restPayload = "{\"OrganizationName\": \"AutoOrg\"}";
        int statusCode;
        HttpGet httpGet = new HttpGet(restURL);
        httpGet.addHeader("Authorization", "Basic " + getEncodedString());
        httpGet.addHeader("Content-Type", Constants.ADF_JSON_MEDIA_TYPE);
        /*
        StringEntity se = null;
        try {
            se = new StringEntity(restPayload);
        } catch (UnsupportedEncodingException e) {
            System.out.println("!! Error: There was an issue with input Payload");
        }
*/
       // httpGet.setEntity(se);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
        } catch (SSLPeerUnverifiedException e) {
            System.out.println("!! Error: Peer not Authenticated");
           // updateStatus(-1, "Peer Not Authenticated", 0, testcase.getTestId(), testcase.isAssertCheck());
           
            //return "Peer Not Authenticated";
        } catch (ClientProtocolException e) {
            statusCode = httpResponse.getStatusLine().getStatusCode();
            System.out.println("!! Warning " + e.getMessage());
            System.out.println("!!! Error when invoking PATCH");
        } catch (IOException e) {
            System.out.println("!! Warning " + e.getMessage());
            e.printStackTrace();
            System.out.println();
            statusCode = httpResponse.getStatusLine().getStatusCode();
            System.out.println("!!! Error when invoking PATCH");
        }
    
        statusCode = httpResponse.getStatusLine().getStatusCode();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            System.out.println("!! Warning " + e.getMessage());
        } catch (IOException e) {
            System.out.println("!! Warning " + e.getMessage());
        }
        
        StringBuilder response = new StringBuilder();
        String line = null;
        boolean assertStatus=true;
        try {
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            System.out.println("Response:\n" + response);
        } catch (IOException e) {
            System.out.println("!! Warning " + e.getMessage());
        }
        
    }
    
    public static String getEncodedString() {
        String encoded = new BASE64Encoder().encode(("extn_am1" + ":" + "Welcome1").getBytes());
        return encoded;
    }
}
