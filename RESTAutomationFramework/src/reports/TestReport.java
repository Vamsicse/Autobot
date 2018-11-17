package reports;

import java.util.HashMap;
import java.util.Map;

import artifacts.ArtifactUtil;
import artifacts.MainRESTArtifact;

public class TestReport {
    private TestReport() {
        super();
    }

    public static TestReport getTestReport() {
        TestReport report = new TestReport();
        return report;
    }

    private int totalTestsCount = 0;
    private int testsPCount = 0;
    private Map<String, TestStatus> testStatusMap = new HashMap<String, TestStatus>();
    private MainRESTArtifact mainArtifact = null;

    public void setTestStatusMap(Map<String, TestStatus> testStatusMap) {
        this.testStatusMap = testStatusMap;
    }

    public Map<String, TestStatus> getTestStatusMap() {
        return testStatusMap;
    }

    public void setMainArtifact(MainRESTArtifact mainArtifact) {
        this.mainArtifact = mainArtifact;
    }

    public MainRESTArtifact getMainArtifact() {
        if (mainArtifact == null) {
            mainArtifact = ArtifactUtil.jaxbXMLToObject(null);
        }
        return mainArtifact;
    }

    public void setTotalTestsCount(int totalTestsCount) {
        this.totalTestsCount = totalTestsCount;
    }

    public int getTotalTestsCount() {
        if (mainArtifact != null) {
            totalTestsCount = mainArtifact.getArtifact().size();
        }
        return totalTestsCount;
    }

    public void setTestsPCount(int testsPCount) {
        this.testsPCount = testsPCount;
    }

    public int getTestsPCount() {
        return testsPCount;
    }

}
