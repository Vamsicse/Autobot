package execute;

import artifacts.MainRESTArtifact;
import artifacts.RESTTestArtifact;

import env.Constants;
import env.RunAutomation;
import env.TestEnvironment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.sql.Timestamp;

import java.util.List;

import org.json.JSONObject;

import sun.misc.BASE64Encoder;

import util.IOUtil;
import util.RESTUtil;

public class DataLoadHelper {

    static String user, password, authToken;
    static double completeTime=0;
    
    public static void loadObject(String objectNames, int range, String... details) throws IOException {
        user = details[1];
        password=details[2];
        authToken = "Basic " + new BASE64Encoder().encode((user + ":" + password).getBytes());
        if(range<1){
            System.out.println("!!! No POST operations to invoke. Exiting...");           
            System.exit(1);
        }
        TestEnvironment.getTestEnvironment(RunAutomation.getValidateMap());
        MainRESTArtifact mainArtifact = TestEnvironment.getMainArtifact();
        String[] object = objectNames.split(",");
        int objCount = object.length;
        if(objCount<1){
            System.out.println("!!! No Targets to load. Exiting...");           
            System.exit(1);
        }
        String [] testId = new String[objCount];
        for(int i=0;i<objCount;i++){
            testId[i] = object[i] + "_Post";
            testId[i] = testId[i].substring(0, 1).toUpperCase() + testId[i].substring(1);
        }

        List<RESTTestArtifact> allTestCases = mainArtifact.getArtifact();
        int testCasesSize = allTestCases.size();
        int count = 0, i=0;
        for (RESTTestArtifact testcase : allTestCases) {
            String currObj = testcase.getTestId();
            if (currObj.equals(testId[i])) {
                String restURL = testcase.getRestURL();
                String absoluteURL = details[0] + restURL;
                DataLoadHelper.loader(testId[i], testcase, absoluteURL, range);
                i++;
            } else {
                count++;
            }
            if(i>=objCount)
             return;
        }
        if (testCasesSize == count) {
            System.out.println("!!! Error: No matches found for Object Name");
            System.out.println("Make sure that [" + testId[i] +
                               "] testcase is uncommented and available in TestSuite.xml\n");
        }
    }

    public static void loader(String testId, RESTTestArtifact testcase, String absoluteURL, int range) {
        System.out.println(" ** Info: Request payload will be made unique and new request payload file will be created with name [" +
                           testId + "-unique" + "] **");
        for (int i = 0; i < range; i++) {
            String response = doPost(testId, testcase, absoluteURL);
            String fileSuffix = new Timestamp(System.currentTimeMillis()).toString().replace(":", ".");
            String filename = testId + "_LoadOpr_" + i +"_"+ fileSuffix + ".txt";
            IOUtil.writeToFile(RunAutomation.getOutputLocation(), filename, response);
        }
        System.out.println("\n*** Info: Completed " + range + " POST operations on [" + testcase.getTestObject() + "] object.");
        System.out.println("*** Info: Total time for " + range + " POST Operations: " + (completeTime/1000000) + " milliseconds.");
        System.out.println("*** Info: Average time for " + range + " POST Operations: " + ((completeTime/1000000)/range) + " milliseconds.\n");
        System.out.println("*** Info: Wrote responses to Directory: [" + RunAutomation.getOutputLocation()+ "]\n");
        completeTime=0;
    }

    public static String doPost(String testId, RESTTestArtifact testcase, String restURL) {
        String restPayload = IOUtil.readFromFile(TestEnvironment.getTestEnvironment().getRequestPayloadPath(), testId);
        if (restPayload.equals("")) {
            System.out.println("\n\n!!! Error: Input Payload is absent for " + testcase.getTestId() + "\n\n");
            return "Payload is Absent";
        }
        if (testcase.isUnique()) {
            if (TestEnvironment.DEBUG) {
                System.out.println("   -- [DEBUG] testId [" + testId + "] has unique set to true.");
            }
            String newRestPayload = new RESTUtil().makeJSONRequestPayloadUnique(restPayload, testcase);
            if (newRestPayload != null && !newRestPayload.equals(restPayload)) {
                if (IOUtil.writeToFile(TestEnvironment.getTestEnvironment().getRequestPayloadPath(),
                                       testId + "-unique.txt", newRestPayload)) {
                }
                restPayload = newRestPayload;
            }
        }
        if (TestEnvironment.DEBUG) {
            System.out.println("-- [DEBUG] testId [" + testId + "] restPayload [" + restPayload + "]");
        }
        StringBuilder response = new StringBuilder();
        int statusCode = 0;
        long startTime = 0, endTime = 0, elapsedTime = 0;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(restURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", authToken);
            conn.setRequestMethod("POST");
            conn.setRequestProperty(Constants.CONTENT_TYPE, Constants.ADF_JSON_MEDIA_TYPE);
            conn.setDoOutput(true);
            conn.getOutputStream().write(restPayload.getBytes("UTF-8"));
            startTime = System.nanoTime();
            try {
                conn.getInputStream();
            } catch (IOException e) {
                System.out.println("!! Warning " + e.getMessage());
                System.out.println("!!! Error when invoking POST");
                statusCode = conn.getResponseCode();
                response.append("{ POST failed with Status Code: " + statusCode + "}");
            }
            endTime = System.nanoTime();
            elapsedTime=endTime-startTime;
            System.out.println(" - Elapsed time for current POST operation on [ " + testcase.getTestObject()+ " ] : " + elapsedTime/1000000 + " milliseconds");
            completeTime = completeTime + elapsedTime;
            statusCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String indentedResp = (new JSONObject(response.toString()).toString(4));
        return indentedResp;
    }
}
