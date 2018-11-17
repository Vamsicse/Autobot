package util;

import java.util.HashMap;
import java.util.Map;

import artifacts.RESTTestArtifact;

import env.TestEnvironment;

import org.json.JSONException;
import org.json.JSONObject;

public class RESTUtil {


    /**
     * @param url "/salesApi/resources/{latest}/opportunities/{OptyNumber}"
     * @param response REST response payload
     * @return Map <String, String> {"OptyNumber":"CDRM_417598"}
     */
    @SuppressWarnings("oracle.jdeveloper.java.comment-html-is-unterminated")
    public static Map<String, String> fetchDependentRESTResponseValues(String url, String response) {
        Map<String, String> valuesMap = null;
        if (url != null && url.indexOf("{") > -1 && response != null && !response.isEmpty()) {
            valuesMap = new HashMap<String, String>();
            JSONObject json = null;
            try {
                json = new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
                System.out.println("   -- [DEBUG] Exception occurred in method [fetchDependentRESTResponseValues] while parsing JSONObject" +
                                   e.getMessage());

            }
            int pos = -1;
            int endPos = pos;
            String placeHolderStr = null;
            pos = url.indexOf("{", 0);

            while (pos != -1 && pos < url.length()) {
                endPos = url.indexOf("}", pos);
                if (TestEnvironment.DEBUG2) {
                    System.out.println("   -- [DEBUG] char at index[" + pos + "] -> " + url.charAt(pos));
                    System.out.println("   -- [DEBUG] char at index[" + endPos + "] -> " + url.charAt(endPos));
                }
                placeHolderStr = url.substring(pos + 1, endPos);
                try {
                    // Retrieve JSON object and push it into map.
                    if (TestEnvironment.DEBUG)
                        System.out.println("   -- [DEBUG] JSON Key: " + json.get(placeHolderStr));
                    valuesMap.put(placeHolderStr, String.valueOf(json.get(placeHolderStr)));
                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println("   -- [DEBUG] Could not fetch JSONObject [" + placeHolderStr + "] error -> " +
                                       e.getMessage());
                }
                pos = url.indexOf("{", endPos);
            }
        }
        return valuesMap;
    }


    public String makeJSONRequestPayloadUnique(String requestPayload, RESTTestArtifact testcase) {
        String newRequestPayload = null;
        if(requestPayload.equals(""))
         System.out.println("\n\n!!! Error: Input Payload is absent for " + testcase.getTestId() + "\n\n");
       
        try {
            JSONObject json = new JSONObject(requestPayload);
            long time = System.currentTimeMillis();
            String value = null;
            if (TestEnvironment.DEBUG) {
                System.out.println("   -- [DEBUG] Started replacing the old values to make it unique...");
            }

            String prop = testcase.getKeyAttribute();

            if (prop == null) {
                System.out.println("!! Warning Failed in fetching Key Attribute");
            }
            try{
            value = (String) json.get(prop) + "_UN_" + time;
            }
            catch(JSONException e){
                System.out.println("!! Warning: Key Attribute is not present for this object");
            }

            if (TestEnvironment.DEBUG) {
                System.out.println("   -- [DEBUG] new value[" + value + "]");
                System.out.println("   -- [DEBUG] Property[" + prop + "]");
            }
            try{
            json.put(prop, value);
            }
            catch(JSONException e){
                System.out.println("!!Warning: There was an exception when pushing record into Payload");
            }
            value=null;
            String prop2=testcase.getKeyAttribute2();
            if(!(prop2==null)) {
                value = (String) json.get(prop2) + "_UN_" + time;
                json.put(prop2, value);
            }
            newRequestPayload = json.toString();
            
            if (TestEnvironment.DEBUG) {
                System.out.println("   -- [DEBUG] Current/NewRequestPayload [" + newRequestPayload + "]");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("   -- [DEBUG] Could not fetch JSONObject [" + requestPayload + "] error -> " +
                               e.getMessage());
        }

        return newRequestPayload;
    }
    
    // This method is invoked only for PUT
    public String addDependentKeys(String requestPayload, RESTTestArtifact testcase,
                                   Map<String, String> completedTests) throws JSONException {
        JSONObject json = new JSONObject(requestPayload);
        JSONObject resp = new JSONObject(completedTests.get(testcase.getDependentOnTest()));
        try{
        if (testcase.getJSONAttribute2() != null)
            json.put(testcase.getJSONAttribute2(), resp.get(testcase.getJSONAttribute2()));
        if (testcase.getJSONAttribute3() != null)
            json.put(testcase.getJSONAttribute3(), resp.get(testcase.getJSONAttribute3()));
        if (testcase.getJSONAttribute4() != null)
            json.put(testcase.getJSONAttribute4(), resp.get(testcase.getJSONAttribute4()));
        if (testcase.getJSONAttribute5() != null)
            json.put(testcase.getJSONAttribute5(), resp.get(testcase.getJSONAttribute5()));
        }
        catch (JSONException e) {
            if (TestEnvironment.DEBUG)
                System.out.println("!! Warning: During PUT Operation: " + e.getMessage());
        }
        return json.toString();
    }
}
