package execute;

import artifacts.ArtifactUtil;
import artifacts.MainRESTArtifact;
import artifacts.RESTTestArtifact;

import env.Constants;
import util.HtmlUtil;
import env.RunAutomation;
import env.TestEnvironment;
import util.FileUtil;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import reports.TestReport;
import reports.TestStatus;

import sun.misc.BASE64Encoder;

import util.IOUtil;
import util.RESTUtil;
import util.StringUtil;
import util.TrustUtil;
import util.ZCXUtil;

public class RESTTestAutomationManager {

    private String hostNPort;
    private static String user;
    private static String password;
    private static String requestPayloadPath = TestEnvironment.getTestEnvironment().getRequestPayloadPath();
    private static String responsePayloadPath = TestEnvironment.getTestEnvironment().getResponsePayloadPath();
    private static StringBuilder report = new StringBuilder();
    private static StringBuilder htmlReport = new StringBuilder();
    private static long htmlTestId=1;
    private boolean testResult;
    private static double pass = 0, fail = 0, total = 0, skip = 0;
    static TestReport testReport = TestEnvironment.getTestEnvironment().getTestReport();
    static RESTTestAutomationManager RESTMgr = new RESTTestAutomationManager();
    private static RESTUtil RestUtil = new RESTUtil();
    static Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    static boolean assertionMode, isHttpsUrl = false;
    private Map<String, String> completedTests = new HashMap<String, String>();
    private static String encodedString;
    
    public static RESTTestAutomationManager getInstance() {
        return RESTMgr;
    }

    public static String getTimeStamp() {
        String filesuffix = "_TestOpr_" + timestamp.toString().replace(":", ".");
        return filesuffix;
    }

    public void setTestReport(TestReport testReport) {
        RESTTestAutomationManager.testReport = testReport;
    }

    public TestReport getTestReport() {
        return testReport;
    }

    public void startRESTTestAutomation(String... details) {
        invokeRESTPayloads(details);
    }

    private void invokeRESTPayloads(String... details) {
        MainRESTArtifact mainArtifact = TestEnvironment.getMainArtifact();
        if (mainArtifact == null) {
            mainArtifact = ArtifactUtil.jaxbXMLToObject(null);
            TestEnvironment.setMainArtifact(mainArtifact);
        }
        hostNPort = details[0];
        user = details[1];
        password = details[2];
        if (hostNPort.startsWith("https") || hostNPort.startsWith("Https")) {
            isHttpsUrl = true;
        }
        isHttpsUrl = false;
        if (isHttpsUrl) {
            HttpHost proxy = new HttpHost("proxy.com", 911, "http");            
            try {
                TrustUtil.doTrustToCertificates();
            } catch (Exception e) {
                System.out.println("!!! Error: There was an exception while trusting certificates");
            }
        }
        testReport.setMainArtifact(mainArtifact);

        report.append("----------------------------------------------------\n");
        report.append("************** REST Test Automation ****************\n");
        report.append("----------------------------------------------------\n");
        report.append("--- Test Info. ---\n");
        report.append("Test Run Started on: " + new java.util.Date() + "\n");
        report.append("HostNPort: [")
              .append(hostNPort)
              .append("]\n");
        report.append("User: [")
              .append(user)
              .append("]\n");
        report.append("Password: [")
              .append(password)
              .append("]\n");
        report.append("RequestPayloadPath: [")
              .append(requestPayloadPath)
              .append("]\n");
        report.append("ResponsePayloadPath: [")
              .append(responsePayloadPath)
              .append("]\n");
        htmlReport = HtmlUtil.writeInitialHtmlContent(hostNPort,user,password,requestPayloadPath,responsePayloadPath,getTimeStamp());
        report.append("************** REST tests started ****************\n");

        //first execute independent tests and then dependent ones
        String dependentTestId = null;
        List<RESTTestArtifact> allTestCases = mainArtifact.getArtifact();
        total = allTestCases.size();
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("** Info: Total test cases: " + (int) total + " **");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        if (total < 1) {
            System.out.println("!! No Test Cases to execute. Exiting...\n");
            System.exit(1);
        }
        
        encodedString = getEncodedString();
        assertionMode = RunAutomation.isAssertionEnabled();
        
        for (RESTTestArtifact testcase : allTestCases) {
            if (testcase.isDependentTest()) {
                dependentTestId = testcase.getDependentOnTest();
                if (!completedTests.containsKey(dependentTestId)) {
                    if (TestEnvironment.DEBUG) {
                        System.out.println("   -- [DEBUG] Dependent testId [" + dependentTestId +
                                           "] is not executed yet, executing it now");
                    }
                    for (RESTTestArtifact artiFact : allTestCases) {
                        if (testcase.getTestId().equalsIgnoreCase(dependentTestId)) {
                            if (TestEnvironment.DEBUG) {
                                System.out.println("   -- [DEBUG] Dependent testId [" + dependentTestId +
                                                   "] found, executing it now");
                            }
                            invokeRESTTest(artiFact);
                            break;
                        }
                    }
                }
            }
            if (TestEnvironment.DEBUG) {
                System.out.println("   -- [DEBUG] Current Test Id: " + testcase.getTestId());
                System.out.println("   -- [DEBUG] Current Test Operation: " + testcase.getRestTestVerb() + "," +
                                   testcase.getTestDesc());
                if (testcase.isDependentTest()) {
                    System.out.println("   -- [DEBUG] testId [" + testcase.getTestId() + "] is dependent on TestId [" +
                                       dependentTestId + "]");
                } else
                    System.out.println("   -- [DEBUG] Current Test is Independent Test");
                System.out.printf("   -- [DEBUG] Current URL: " + hostNPort + testcase.getRestURL() + "\n");
            }

            testResult = invokeRESTTest(testcase);
        }
        report.append("\n************** REST Tests Completed ****************\n");
        report.append("~~~ Total Tests Ran:    " + (int)total + " ~~~\n");
        report.append("~~~ Test Cases Passed:  " + (int)pass + " ~~~\n");
        report.append("~~~ Test Cases Skipped: " + (int)skip + " ~~~\n");
        report.append("~~~ Test Cases Failed:  " + (int)(total-(pass+skip)) + " ~~~\n");
        double passPercent = (pass / (total));
        passPercent = Math.round((passPercent) * 100.0);
        report.append("~~~ Pass Percentage:    " + passPercent + "% ~~~\n");
        report.append("****************************************************\n");
        report.append("Status Codes for Reference: \n\t200-OK                    (Pass)\n\t201-Created               (Pass)\n\t204-No Content            (Pass)\n\t400-Bad Request           (Client Error)\n\t404-Not Found             (Client Error)\n\t500-Internal Server Error (Server Error)\n\t502-Bad Gateway\n\t503-Service Unavailable   (Server Error)\n");
        report.append("*****************************************************\n");
        report.append("Test Run ended on: " + new java.util.Date());
        System.out.println("\n\n\n\n" + report.toString());
        HtmlUtil.writeStatistics(htmlReport,total,pass,skip,fail,passPercent);
        IOUtil.writeToFile(RunAutomation.getResultLocation(), "Results"+getTimeStamp()+".log", report.toString());
        htmlReport = HtmlUtil.writeClosingContent(htmlReport);
        IOUtil.writeToFile(RunAutomation.getResultLocation(), "Results"+ getTimeStamp()+".html", htmlReport.toString());
        report.setLength(0);
        htmlReport.setLength(0);
    }

    private boolean invokeRESTTest(RESTTestArtifact testcase) {
        boolean success = true;
        String verb = testcase.getRestTestVerb(); // REST Call Type
        String testId = testcase.getTestId();
        String testDesc = testcase.getTestDesc();
        String testObj = testcase.getTestObject();
        String restURL = testcase.getRestURL();
        boolean isDependentTest = testcase.isDependentTest();
        String dependentOnTest = testcase.getDependentOnTest();
        boolean istestCaseMultiDependent=false;
        String parentTestCase=null,childTestCase=null;

        try {
            istestCaseMultiDependent = dependentOnTest.contains(",");
            if(istestCaseMultiDependent)
            {
            parentTestCase = dependentOnTest.split(",")[0];
            childTestCase = dependentOnTest.split(",")[1];
            }
        } catch (NullPointerException npe) {
            npe.getCause();
        }
        catch(ArrayIndexOutOfBoundsException aiob){
            aiob.getCause();
        }

        TestStatus testStatus = new TestStatus();
        Map<String, TestStatus> map = testReport.getTestStatusMap();
        if (map != null && !map.containsKey(testId)) {
            map.put(testId, testStatus);
        }
        testStatus.setTestID(testId);
        testStatus.setArtifact(testcase);
        testStatus.setExecuted(true);

        report.append("\n");
        report.append("-> TestID [")
              .append(testId)
              .append("]\n");
        report.append("   Object [")
              .append(testObj)
              .append("]\n");
        report.append("   Type [")
              .append(verb)
              .append("]\n");
        report.append("   TestDesc [")
              .append(testDesc)
              .append("]\n");

        if (TestEnvironment.DEBUG) {
            System.out.println("\n   -- [DEBUG] New REST invocation started... ");
            System.out.println("   -- [DEBUG] Action [" + verb + "]");
            System.out.println("   -- [DEBUG] testId [" + testId + "]");
            System.out.println("   -- [DEBUG] testDesc [" + testDesc + "]");
            System.out.println("   -- [DEBUG] testObj [" + testObj + "]");
            System.out.println("   -- [DEBUG] restURL [" + restURL + "]");
            System.out.println("   -- [DEBUG] isDependentTest [" + isDependentTest + "]");
            System.out.println("   -- [DEBUG] dependentOnTest [" + dependentOnTest + "]");
        }

        try {
            if (isDependentTest) {
                if (TestEnvironment.DEBUG) {
                    System.out.println("   -- [DEBUG] restURL ["+restURL+"] has placeholders, replacing with dependent test response payload values");
                }
                if (!istestCaseMultiDependent) {
                    //if its a dependent test, then the other test needs to have passed
                    String dependentTestResponse = completedTests.get(dependentOnTest);
                    // This if block will construct runtime REST URL
                    if (dependentTestResponse != null &&
                        dependentTestResponse.startsWith("{")) {
                        Map<String, String> valuesMap = RESTUtil.fetchDependentRESTResponseValues(restURL, completedTests.get(dependentOnTest));
                        restURL = StringUtil.replaceTokens(valuesMap, restURL);
                        if (TestEnvironment.DEBUG) {
                            System.out.println("   -- [DEBUG] Run time REST URL: " + restURL);
                        }
                    } else {
                        if (TestEnvironment.DEBUG) {
                            System.out.println("   -- [DEBUG] Dependent test result doesn't seem to be JSON response, hence not executing test case further.");
                            System.out.println("   -- [DEBUG] response [" + dependentTestResponse + "]");
                        }
                        report.append("   Status [NOT EXECUTED as dependent test failed...]\n");
                        htmlReport = HtmlUtil.writeTestCaseDetails(htmlReport, htmlTestId++,testId, testObj,verb, testDesc,hostNPort + restURL);
                        updateStatus(-1, "Dependent Test Failed", 0, testcase.getTestId(),testcase.isAssertCheck());
                        return false;
                    }
                    if (TestEnvironment.DEBUG) {
                        System.out.println("   -- [DEBUG] restURL [" + restURL + "] after substitution...");
                    }
                }
                else{
                    String dependentParentTestResponse = completedTests.get(parentTestCase);
                    String dependentChildTestResponse = completedTests.get(childTestCase);
                    if (dependentParentTestResponse != null && dependentParentTestResponse.startsWith("{") && dependentChildTestResponse != null && dependentChildTestResponse.startsWith("{")) {
                        Map<String, String> parentValuesMap = RESTUtil.fetchDependentRESTResponseValues(restURL, completedTests.get(parentTestCase));
                        Map<String, String> childValuesMap = RESTUtil.fetchDependentRESTResponseValues(restURL, completedTests.get(childTestCase));
                        restURL = StringUtil.replaceTokensForComplexURL(parentValuesMap, childValuesMap, restURL);
                        if (TestEnvironment.DEBUG) {
                            System.out.println("   -- [DEBUG] Run time REST URL: " + restURL);
                        }
                    } else {
                        if (TestEnvironment.DEBUG) {
                            System.out.println("   -- [DEBUG] Dependent test result doesn't seem to be JSON response, hence not executing test case further.");
                            System.out.println("   -- [DEBUG] Parent Response [" + dependentParentTestResponse + "]");
                            System.out.println("   -- [DEBUG]  Child Response [" + dependentChildTestResponse + "]");
                        }
                        report.append("   Status [NOT EXECUTED as dependent test failed...]\n");
                        htmlReport = HtmlUtil.writeTestCaseDetails(htmlReport, htmlTestId++,testId, testObj,verb, testDesc,hostNPort + restURL);
                        updateStatus(-1, "Dependent Test Failed", 0, testcase.getTestId(),testcase.isAssertCheck());
                        return false;
                    }
                    if (TestEnvironment.DEBUG) {
                        System.out.println("   -- [DEBUG] restURL [" + restURL + "] after substitution...");
                    }
                }
        }
            String respStr = null;
            String absoluteURL = hostNPort + restURL;
            htmlReport = HtmlUtil.writeTestCaseDetails(htmlReport,htmlTestId++,testId,testObj,verb,testDesc, absoluteURL);
            if (Constants.GET.equalsIgnoreCase(verb) || Constants.DESCRIBE.equalsIgnoreCase(verb)) {
                respStr = buildGet(testcase, absoluteURL, verb);
            } else if (Constants.POST.equalsIgnoreCase(verb)) {
                respStr = buildPost(testId, testcase, absoluteURL);
            } else if (Constants.PATCH.equalsIgnoreCase(verb)) {
                respStr = buildPatch(testId, testcase, absoluteURL);
            } else if (Constants.DELETE.equalsIgnoreCase(verb)) {
                respStr = buildDelete(testcase, absoluteURL);
            }
            completedTests.put(testId, respStr);
            testcase.setExecuted(true);
            String indented = respStr;
            if (!Constants.DELETE.equalsIgnoreCase(verb)) {
                if (respStr.startsWith("{")) { 
                    try {
                        indented = (new JSONObject(respStr).toString(4));
                    } catch (JSONException e) {
                        if (TestEnvironment.DEBUG)
                            System.out.println("   -- [DEBUG] Could not convert JSON to indented JSON Format. Continuing...\n");
                    }
                } else {
                    if (TestEnvironment.DEBUG) {
                        System.out.println("   -- [DEBUG] JSON Response doesn't seem to be correct, dumping it");
                        System.out.println("   -- [DEBUG] JSON Response [" + respStr + "]");
                    }
                }
            }
            testStatus.setLastRun(new Date());
            testStatus.setResponsePayload(indented);
            testStatus.setStatus(TestStatus.STATUS_PASSED);
            testReport.setTestsPCount(testReport.getTestsPCount() + 1);
            if (!Constants.DELETE.equalsIgnoreCase(verb)) {
                if (IOUtil.writeToFile(responsePayloadPath, testId + getTimeStamp() + ".txt", indented)) {
                    if (TestEnvironment.DEBUG) {
                        System.out.println("   -- [DEBUG] response written to file successfully...");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println();
            success = false;
            testStatus.setStatus(TestStatus.STATUS_FAILED);
            report.append("   Invocation Status [FAILED with ERROR]\n");
        }
        if (TestEnvironment.DEBUG) {
            System.out.println("   -- [DEBUG] REST invocation completed... \n\n");
        }
        return success;
    }

    private String buildGet(RESTTestArtifact testcase, String restURL, String action) {
        StringBuilder response = new StringBuilder();
        int statusCode = 0;
        long startTime = 0, endTime = 0, elapsedTime = 0;
        String testId = testcase.getTestId();
        boolean failed=false;
        HttpURLConnection conn=null;
        BufferedReader br=null;
        URL url = null;
        try {
            url = new URL(restURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", encodedString);
            conn.setRequestMethod("GET");
            startTime = System.nanoTime();
            try {
                conn.getInputStream();
            } catch (IOException e) {
                if (!testcase.isNegativeTest()) {
                    System.out.println("!! Warning " + e.getMessage());
                    System.out.println("!!! Error when invoking " + action);
                    statusCode = conn.getResponseCode();
                    failed=true;
                }
            }
            endTime = System.nanoTime();
            statusCode = conn.getResponseCode();
            br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println();
        } catch (IOException e) {
            if (!testcase.isNegativeTest()) {
                e.printStackTrace();
                System.out.println();
            }
        }
        finally{
            conn.disconnect();
        }
        if (statusCode == 500 && testcase.getRestTestVerb().equals("DESCRIBE")) {
            response.append("DESCRIBE failed with status 500");
        }
        report.append("   REST URL: [" + restURL + "]\n");
        elapsedTime = endTime - startTime;
        if (elapsedTime < 0)
            elapsedTime = 0;
        
        boolean assertResult=true;
        if(failed && testcase.isAssertCheck()){
            System.out.println("** Info: Current Test Failed. Hence skipping Assertion check.");
        }
        
        if((testcase.isAssertCheck() && !failed) || (assertionMode && !failed)) {
            System.out.println("** Info:  Assert Check enabled for the test case: " + testId);
            String assertSrcFileName = testId+"_AssertSource.txt";
            String assertDestFileName = testId+"_AssertDest.txt";
            IOUtil.writeToFile(responsePayloadPath, assertDestFileName, new JSONObject(response).toString(4));
            
            if(!responsePayloadPath.endsWith(File.separator))
                  responsePayloadPath = responsePayloadPath + File.separator;

            if (!testcase.isNegativeTest()) {
                assertResult =
                        doAssertCheck(assertSrcFileName, responsePayloadPath +
                                      assertDestFileName,
                                      responsePayloadPath + assertSrcFileName);

                if (assertResult) {
                    System.out.println("** Info:  Assertion Passed for Test: " +
                                       testId);
                    report.append("   Assert Status: [ Pass ]\n");
                    htmlReport =
                            HtmlUtil.writeAssertionResult(htmlReport, assertResult);
                } else {
                    System.out.println("!! Error: Assertion Failed for Test: " +
                                       testId);
                }
            }
        }

        if (!testcase.isNegativeTest()) {
            updateStatus(statusCode, response.toString(), elapsedTime, testId, assertResult);
        } else {
            updateStatusforNegTest(statusCode, response.toString(), elapsedTime, testId);
        }
        if (action.equals("DESCRIBE") && RunAutomation.isReadZCXData())
            try {
                if (TestEnvironment.DEBUG)
                    System.out.println("Trying to extract extensibility data..");
                ZCXUtil.extractZCXData(response.toString(), testcase.getTestObject());
            } catch (JSONException e) {
                System.out.println("!! Warning: Describe for " + testcase.getTestObject() + " might have failed.\n");
            }
        return response.toString();
    }

    private String buildDelete(RESTTestArtifact testcase, String restURL) {
        StringBuilder response = new StringBuilder();
        int statusCode = 0;
        long startTime = 0, endTime = 0, elapsedTime = 0;
        HttpURLConnection conn = null;
        URL url = null;
        try {
            url = new URL(restURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", encodedString);
            conn.setRequestMethod("DELETE");
            conn.setDoOutput(true);
            startTime = System.nanoTime();
            try {
                conn.getInputStream();
            } catch (IOException e) {
                System.out.println("!! Warning: " + e.getMessage());
                System.out.println("!!! Error when invoking DELETE");
                statusCode = conn.getResponseCode();
                response.append("{ DELETE failed with Status Code: " + statusCode + "}");
            }
            endTime = System.nanoTime();
            statusCode = conn.getResponseCode();
            if (response.equals(null))
                response = response.append("{Delete Status " + statusCode + "}");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println();
        }
        finally{
            conn.disconnect();
        }
        elapsedTime = endTime - startTime;
        if (elapsedTime < 0)
            elapsedTime = 0;
        if(testcase.isAssertCheck()){
            System.out.println("!! Warning: Assertion check is not allowed for DELETE operation.");
        }
        
        if (!testcase.isNegativeTest()) {
            updateStatus(statusCode, response.toString(), elapsedTime, testcase.getTestId(),true);
        } else {
            updateStatusforNegTest(statusCode, response.toString(), elapsedTime, testcase.getTestId());
        }
        return response.toString();
    }


    public static String buildPost(String testId, RESTTestArtifact testcase, String restURL) {
        boolean failed=false;
        String restPayload = IOUtil.readFromFile(requestPayloadPath, testId);
        if (restPayload.equals("")) {
            System.out.println("\n\n!!! Error: Input Payload is absent for " + testcase.getTestId() + "\n\n");
            updateStatus(-1, "Payload is Absent", 0, testcase.getTestId(), testcase.isAssertCheck());
            return "Payload is Absent";
        }
        if (testcase.isUnique()) {
            if (TestEnvironment.DEBUG) {
                System.out.println("   -- [DEBUG] testId [" + testId + "] has unique set to true.");
            }
            String newRestPayload = RestUtil.makeJSONRequestPayloadUnique(restPayload, testcase);
            if (newRestPayload != null && !newRestPayload.equals(restPayload)) {
                if (IOUtil.writeToFile(requestPayloadPath, testId + "-unique.txt", newRestPayload)) {
                    System.out.println("** Info: Request payload content made unique and new request payload file created with name [" +
                                       testId + "-unique" + "] **");
                }
                restPayload = newRestPayload;
            }
        }

        testReport.getTestStatusMap()
                  .get(testId)
                  .setRequestPayload(restPayload);

        if (TestEnvironment.DEBUG) {
            System.out.println("   -- [DEBUG] testId [" + testId + "] restPayload [" + restPayload + "]");
        }
        StringBuilder response = new StringBuilder();
        int statusCode = 0;
        long startTime = 0, endTime = 0, elapsedTime = 0;
        HttpURLConnection conn = null;
        URL url = null;
        BufferedReader br = null;
        try {
            url = new URL(restURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", encodedString);
            conn.setRequestMethod("POST");
            conn.setRequestProperty(Constants.CONTENT_TYPE, Constants.ADF_JSON_MEDIA_TYPE);
            conn.setDoOutput(true);
            conn.getOutputStream().write(restPayload.getBytes("UTF-8"));
            startTime = System.nanoTime();
            try {
                conn.getInputStream();
            }
            catch(EOFException e){
                System.out.println("!! Warning " + e.getMessage());
                System.out.println("!!! Error when invoking POST");
                failed = true;
            }
            catch (IOException e) {
                System.out.println("!! Warning " + e.getMessage());
                System.out.println("!!! Error when invoking POST");
                statusCode = conn.getResponseCode();
                failed = true;
            }
            endTime = System.nanoTime();
            statusCode = conn.getResponseCode();

            br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println();
        }
        finally{
            conn.disconnect();
        }
        elapsedTime = endTime - startTime;
        if (elapsedTime < 0)
            elapsedTime = 0;
        
        boolean assertResult = true;
        if(failed && testcase.isAssertCheck()){
            System.out.println("** Info: POST failed. Hence skipping corresponding assertion.");
        }
        if((assertionMode && !failed) || (testcase.isAssertCheck() && !failed)) {
            System.out.println("** Info: Assert Check enabled for the test case: " + testId);
            String assertSrcFileName = testId+"_AssertSource.txt";
            String assertDestFileName = testId+"_AssertDest.txt";
            IOUtil.writeToFile(responsePayloadPath, assertDestFileName, new JSONObject(response.toString()).toString(4));
            
            if(!responsePayloadPath.endsWith(File.separator))
                responsePayloadPath = responsePayloadPath + File.separator;
            
            assertResult = doAssertCheck(assertSrcFileName, responsePayloadPath+assertDestFileName, responsePayloadPath+assertSrcFileName);
            if(assertResult) {
                System.out.println("** Info: Assertion Passed for Test: " + testId);
                report.append("   Assert Status: [ Pass ]\n");
                htmlReport = HtmlUtil.writeAssertionResult(htmlReport, assertResult);
            }
            else{
                System.out.println("!! Error:  Assertion Failed for Test: " + testId);
            }
        }        
        if (!testcase.isNegativeTest()) {
            updateStatus(statusCode, response.toString(), elapsedTime, testId, assertResult);
        } else {
            updateStatusforNegTest(statusCode, response.toString(), elapsedTime, testId);
        }
        return response.toString();
    }

    private String buildPatch(String testId, RESTTestArtifact testcase, String restURL) {
        String restPayload = IOUtil.readFromFile(requestPayloadPath, testId);
        boolean failed=false;
        if (restPayload.equals("")) {
            System.out.println("\n\n!!! Error: Input Payload is absent for " + testcase.getTestId() + "\n\n");
            updateStatus(-1, "Payload is Absent", 0, testcase.getTestId(), testcase.isAssertCheck());
            return "Payload is Absent";
        }
        if (testcase.isUnique()) {
            if (TestEnvironment.DEBUG) {
                System.out.println("   -- [DEBUG] testId [" + testId + "] has unique set to true.");
            }
            if (TestEnvironment.DEBUG) {
                System.out.println("   -- [DEBUG] Property [" + testcase.getKeyAttribute() + "] needs to be unique.");
            }
            String newRestPayload = RestUtil.makeJSONRequestPayloadUnique(restPayload, testcase);
            
            // This is required only for PUT operations (Meant for future use)
            /*            try {
                newRestPayload = RestUtil.addDependentKeys(newRestPayload, testcase, completedTests);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
            if (newRestPayload != null && !newRestPayload.equals(restPayload)) {
                if (IOUtil.writeToFile(requestPayloadPath, testId + "-unique.txt", newRestPayload)) {
                    System.out.println("** Info: Request payload content made unique and new request payload file created with name [" +
                                       testId + "-unique" + "] **");
                }
                restPayload = newRestPayload;
            }
        }
        testReport.getTestStatusMap()
                  .get(testId)
                  .setRequestPayload(restPayload);

        if (TestEnvironment.DEBUG) {
            System.out.println("   -- [DEBUG] testId [" + testId + "] restPayload [" + restPayload + "]");
        }
        int statusCode = 0;
        long startTime = 0, endTime = 0, elapsedTime = 0;

        HttpPatch httpPatch = new HttpPatch(restURL);
        httpPatch.addHeader("Authorization", encodedString);
        httpPatch.addHeader("Content-Type", Constants.ADF_JSON_MEDIA_TYPE);
        StringEntity se = null;
        try {
            se = new StringEntity(restPayload);
        } catch (UnsupportedEncodingException e) {
            System.out.println("!! Error: There was an issue with input Payload");
        }
        httpPatch.setEntity(se);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        if (isHttpsUrl) {
            httpClient = (DefaultHttpClient) TrustUtil.trustClient(httpClient);
        }
        HttpResponse httpResponse = null;
        startTime = System.nanoTime();
        try {
            httpResponse = httpClient.execute(httpPatch);
            statusCode = httpResponse.getStatusLine().getStatusCode();
        } catch (SSLPeerUnverifiedException e) {
            System.out.println("!! Error: Peer not Authenticated");
            updateStatus(-1, "Peer Not Authenticated", 0, testcase.getTestId(), testcase.isAssertCheck());
            failed=true;
            return "Peer Not Authenticated";
        } catch (ClientProtocolException e) {
            statusCode = httpResponse.getStatusLine().getStatusCode();
            System.out.println("!! Warning " + e.getMessage());
            System.out.println("!!! Error when invoking PATCH");
            failed=true;
        } catch (IOException e) {
            System.out.println("!! Warning " + e.getMessage());
            e.printStackTrace();
            System.out.println();
            System.out.println("!!! Error when invoking PATCH");
            failed=true;
        }
        endTime = System.nanoTime();
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
        boolean assertResult = true;
        try {
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
        } catch (IOException e) {
            System.out.println("!! Warning " + e.getMessage());
        }
        elapsedTime = endTime - startTime;
        if (elapsedTime < 0)
            elapsedTime = 0;
        if(testcase.isAssertCheck() && failed){
            System.out.println("** Info: Current Test Failed. Skipping Assertion.");
        }
        if((assertionMode && !failed) || (testcase.isAssertCheck() && !failed)) {
            System.out.println("** Info: Assert Check enabled for the test case: " + testId);
            String assertSrcFileName = testId+"_AssertSource.txt";
            String assertDestFileName = testId+"_AssertDest.txt";
            IOUtil.writeToFile(responsePayloadPath, assertDestFileName, response.toString());
            if(!responsePayloadPath.endsWith(File.separator))
                  responsePayloadPath = responsePayloadPath + File.separator;
            
            assertResult = doAssertCheck(assertSrcFileName, responsePayloadPath+assertDestFileName, responsePayloadPath+assertSrcFileName);
            if(assertResult) {
                System.out.println("** Info: Assertion Passed for Test: " + testId);
                report.append("   Assert Status: [ Pass ]\n");
                htmlReport = HtmlUtil.writeAssertionResult(htmlReport, assertResult);
            }
            else{
                System.out.println("!! Error:  Assertion Failed for Test: " + testId);
            }
        }
        if (!testcase.isNegativeTest()) {
            updateStatus(statusCode, response.toString(), elapsedTime, testId, assertResult);
        } else {
            updateStatusforNegTest(statusCode, response.toString(), elapsedTime, testcase.getTestId());
        }
        return response.toString();
    }

    public static void updateStatus(int statusCode, String response, long timeElapsed, String testname, boolean assertResult) {
        report.append("   Execution Time: [")
              .append(timeElapsed / 1000000)
              .append(" milliseconds]\n");
        report.append("   Status [")
              .append(statusCode)
              .append("]\n");
        boolean isTestPass = (statusCode == 200 || statusCode == 201 || statusCode == 204) ;
        double respSize = 0;
        if(!assertResult && isTestPass){
            fail+=1;
            System.out.println("** Info: Current Test Case [" + testname + "] Passed with Status Code [" + statusCode +"], but assertion failed **");
            System.out.println("         Hence marking current testcase status to fail\n");
            report.append("   Error Message: Assertion Failure\n");
            report.append("   Assert Status: [ Fail ]\n");
            report.append("   Result: [ Fail ]\n");
            HtmlUtil.writeTestCaseResults(htmlReport, timeElapsed/1000000, statusCode, respSize, "Failed");
            return;
        }
           if (isTestPass) {
            pass += 1;
            System.out.println("** Info: Current Test Case [" + testname + "] Passed **\n");
            report.append("   Result: Pass\n");
            if (statusCode != 204) {
                try {
                    respSize = (response.getBytes("UTF-8").length) / 1024.0;
                    report.append("   Response Size: " + respSize + " KB.\n");
                } catch (UnsupportedEncodingException e) {
                    e.getMessage();
                }
            }
            HtmlUtil.writeTestCaseResults(htmlReport, timeElapsed/1000000, statusCode, respSize, "Passed");
        } else if (statusCode == -1) {
            skip += 1;
            System.out.println("** Info: Current Test Case [" + testname + "] Skipped **\n");
            report.append("   Result: Skipped\n");
            if (response != null && !response.isEmpty())
                report.append("   Error Message: " + response + "\n");
            HtmlUtil.writeTestCaseResults(htmlReport, timeElapsed/1000000, statusCode, 0, "Skipped");
        } else {
            fail += 1;
            System.out.println("** Info: Current Test Case [" + testname + "] Failed **\n");
            report.append("   Result: Fail\n");
            if (response != null && !response.isEmpty())
                report.append("   Error Message: " + response + "\n");
            HtmlUtil.writeTestCaseResults(htmlReport, timeElapsed/1000000, statusCode, respSize, "Failed");   
        }
    }

    public static void updateStatusforNegTest(int statusCode, String response, long timeElapsed, String testname) {
        report.append("   Execution Time: [")
              .append(timeElapsed / 1000000)
              .append(" milliseconds]\n");
        report.append("   Status [")
              .append(statusCode)
              .append("]\n");
        double respSize = 0;
        if (statusCode == 200 || statusCode == 201 || statusCode == 204) {
            fail += 1;
            System.out.println("** Info: Current Negative Test Case Failed [" + testname + "] **\n");
            report.append("   Result: Fail\n");
            report.append("   Error Message: Expected status code 5xx or 4xx. Current Status: " + statusCode + "\n");
            if (statusCode != 204) {
                try {
                    respSize = (response.getBytes("UTF-8").length) / 1024.0;
                    report.append("   Response Size: " + respSize + " KB.\n");
                } catch (UnsupportedEncodingException e) {
                    e.getMessage();
                }
            }
            HtmlUtil.writeTestCaseResults(htmlReport, timeElapsed/1000000, statusCode, respSize, "Failed");   
        } else if (statusCode == -1) {
            skip += 1;
            System.out.println("** Info: Current Negative Test Case Skipped [" + testname + "] **\n");
            report.append("   Result: Skip\n");
            if (response != null && !response.isEmpty())
                report.append("   Error Message: " + response + "\n");
            HtmlUtil.writeTestCaseResults(htmlReport, timeElapsed/1000000, statusCode, respSize, "Skipped");   
        } else {
            pass += 1;
            System.out.println("** Info: Current Negative Test Case Passed [" + testname + "] **\n");
            report.append("   Result: Pass\n");
            if (response != null && !response.isEmpty())
                report.append("   Error Message: " + response + "\n");
            HtmlUtil.writeTestCaseResults(htmlReport, timeElapsed/1000000, statusCode, respSize, "Passed");   
        }
    }

    public static String getEncodedString() {
        String encoded = new BASE64Encoder().encode((user + ":" + password).getBytes());
        return "Basic " + encoded;
    }
    
    public static boolean doAssertCheck(String sourceFileName, String destFile, String sourceFile){
       File source = new File(sourceFile.trim());
       File assertFile = source.getAbsoluteFile();
        boolean assertFilePresent = assertFile.exists();
        if(!assertFilePresent){
         System.out.println("!! Error: [" + sourceFileName + "] is absent at [" + responsePayloadPath + "].");
         report.append("   Error: [").append(sourceFileName).append("] is not present at [").append(responsePayloadPath).append("]\n");
         htmlReport = HtmlUtil.writeAssertFileAbsence(htmlReport, sourceFileName, responsePayloadPath);
         return false;
        }
        // The following commented piece of code compares files using Apache FileUtils jar.
        /*
        File dest = new File(destFile.trim());
        try {
            boolean compareResult;
            compareResult = FileUtils.contentEquals(source, dest);
            System.out.println("!! Error: Two files are not identical.");
            return compareResult;
        } 
        */      
        try{
            boolean compareResult;
            compareResult = FileUtil.compareFiles(sourceFile, destFile);
            if(!compareResult)
                System.out.println("!! Error: Two files are not identical.");
            return compareResult;
        }
        catch (IOException e) {
            System.out.println("!! Error: There was an IOException while comparing files for assertion.");
            return false;
        }
    }
}

