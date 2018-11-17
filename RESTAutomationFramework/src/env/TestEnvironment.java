package env;

import artifacts.ArtifactUtil;
import artifacts.MainRESTArtifact;

import execute.RESTTestAutomationManager;

import java.util.Map;

import reports.TestReport;

import util.IOUtil;

public class TestEnvironment {
    
    private TestEnvironment(Map<String, String> params) {
        super();
        testReport = TestReport.getTestReport();
        validateProvidedDetails(params);
    }

    @SuppressWarnings("oracle.jdeveloper.java.unrestricted-field-access")
    
    public static boolean DEBUG = false;
    public static boolean DEBUG2 = false;
    
    private static TestEnvironment env = null;
    private static TestReport testReport = null;
    private static MainRESTArtifact mainArtifact = null;
    
    private String testSuite_path = null;
    private String requestPayloadPath = null;
    private String responsePayloadPath = null;
    
    public void setArtifact_path(String testSuite_path) {
        this.testSuite_path = testSuite_path;
    }

    public String getArtifact_path() {
        return testSuite_path;
    }

    public void setRequestPayloadPath(String requestPayloadPath) {
        this.requestPayloadPath = requestPayloadPath;
    }

    public String getRequestPayloadPath() {
        return requestPayloadPath;
    }

    public void setResponsePayloadPath(String responsePayloadPath) {
        this.responsePayloadPath = responsePayloadPath;
    }

    public String getResponsePayloadPath() {
        return responsePayloadPath;
    }

    public static void setMainArtifact(MainRESTArtifact mainArtifact) {
        TestEnvironment.mainArtifact = mainArtifact;
    }


    public static TestEnvironment getTestEnvironment(Map<String, String> params) {
        if (env == null) {
            env = new TestEnvironment(params);            
        }
        return env;
    }
    
    public static TestEnvironment getTestEnvironment() {
        if (env == null) {
            throw new RuntimeException("Test env. was never initialized with map params");       
        }
        return env;
    }

    public static MainRESTArtifact getMainArtifact() {
        return mainArtifact;
    }


    public TestReport getTestReport() {
        return testReport;
    }

    public void startTest(String... details) {
        RESTTestAutomationManager.getInstance().startRESTTestAutomation(details);
    }
    
    private boolean validateProvidedDetails(Map<String, String> params){
        if(params == null){
            throw new RuntimeException("parameters must be not null.");
        }else{
            testSuite_path = params.get(Constants.TESTSUITE_FILE_PATH);
            requestPayloadPath = params.get(Constants.REQUEST_PAYLOAD_PATH);
            responsePayloadPath = params.get(Constants.RESPONSE_FILE_PATH);
            if(testSuite_path == null){
                throw new RuntimeException("[" + Constants.TESTSUITE_FILE_PATH + "] parameter missing in map provided.");
            }
            if(requestPayloadPath == null){
                throw new RuntimeException("[" + Constants.REQUEST_PAYLOAD_PATH + "] parameter missing in map provided.");
            }
            if(responsePayloadPath == null){
                throw new RuntimeException("[" + Constants.RESPONSE_FILE_PATH + "] parameter missing in map provided.");
            }
            
            mainArtifact = ArtifactUtil.jaxbXMLToObject(testSuite_path);
            if(!(IOUtil.dirExists(requestPayloadPath) && IOUtil.dirExists(requestPayloadPath))){
                throw new RuntimeException("[" + Constants.RESPONSE_FILE_PATH + " / " + Constants.REQUEST_PAYLOAD_PATH +"] parameter provided are not correct.");
            }
            return true;
        }
    }
    
    public void setDebugLogging(boolean debug){
        DEBUG = debug;
    }
}
