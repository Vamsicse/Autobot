package reports;

import java.util.Date;

import artifacts.ArtifactUtil;
import artifacts.MainRESTArtifact;
import artifacts.RESTTestArtifact;

public class TestStatus {
    public TestStatus() {
        super();
    }
    
    public static final String STATUS_PASSED = "PASSED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_NOT_RUN = "NOT-RUN";
    
    private String testID = null;
    private RESTTestArtifact testcase = null;
    private boolean executed = false;
    private String status = null;
    private Date lastRun = null;
    private String requestPayload = null;
    private String responsePayload = null;

    public void setTestID(String testID) {
        this.testID = testID;
    }

    public String getTestID() {
        return testID;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setArtifact(RESTTestArtifact testcase) {
        this.testcase = testcase;
    }

    public RESTTestArtifact getArtifact() {
        if(testcase == null && testID != null){
            MainRESTArtifact allTestCases = ArtifactUtil.jaxbXMLToObject(null);
            for(RESTTestArtifact art : allTestCases.getArtifact()){
                if(art.getTestId().equalsIgnoreCase(testID)){
                    testcase = art;
                    break;
                }
            }
        }
        return testcase;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setLastRun(Date lastRun) {
        this.lastRun = lastRun;
    }

    public Date getLastRun() {
        return lastRun;
    }
}

