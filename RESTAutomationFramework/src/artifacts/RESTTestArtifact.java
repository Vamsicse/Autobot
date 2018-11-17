package artifacts;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
         { "testId", "testDesc", "testObject", "restTestVerb", "restURL", "dependentTest", "dependentOnTest", "unique",
           "keyAttribute", "keyAttribute2", "JSONAttribute2", "JSONAttribute3", "JSONAttribute4", "JSONAttribute5",
           "negativeTest", "assertCheck"
    })

public class RESTTestArtifact {
    public RESTTestArtifact() {
        super();
    }

    private String testId = null;
    private String testDesc = null;
    private String testObject = null;
    private String restURL = null;
    private String restTestVerb = null;
    private boolean dependentTest = false;
    private boolean unique = false;
    private String keyAttribute = null;
    private String dependentOnTest = null;
    private boolean executed = false;
    private String keyAttribute2 = null;
    private String JSONAttribute2 = null;
    private String JSONAttribute3 = null;
    private String JSONAttribute4 = null;
    private String JSONAttribute5 = null;
    private boolean negativeTest = false;
    private boolean assertCheck = false;
    private boolean childTest = false;
    private String childForTest = null;

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isUnique() {
        return unique;
    }

    public String getKeyAttribute() {
        return keyAttribute;
    }

    public String getKeyAttribute2() {
        return keyAttribute2;
    }

    public void setKeyAttribute(String keyAttribute) {
        this.keyAttribute = keyAttribute;
    }

    public void setKeyAttribute2(String keyAttribute2) {
        this.keyAttribute2 = keyAttribute2;
    }

    public void setDependentTest(boolean dependentTest) {
        this.dependentTest = dependentTest;
    }

    public boolean isDependentTest() {
        return dependentTest;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    @XmlTransient
    public boolean isExecuted() {
        return executed;
    }

    public void setDependentOnTest(String dependentOnTest) {
        this.dependentOnTest = dependentOnTest;
    }

    public String getDependentOnTest() {
        return dependentOnTest;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestDesc(String testDesc) {
        this.testDesc = testDesc;
    }

    public String getTestDesc() {
        return testDesc;
    }

    public void setTestObject(String testObject) {
        this.testObject = testObject;
    }

    public String getTestObject() {
        return testObject;
    }

    public void setRestURL(String restURL) {
        this.restURL = restURL;
    }

    public String getRestURL() {
        return restURL;
    }

    public void setRestTestVerb(String restTestVerb) {
        this.restTestVerb = restTestVerb;
    }

    public String getRestTestVerb() {
        return restTestVerb;
    }

    public String getJSONAttribute2() {
        return JSONAttribute2;
    }

    public String getJSONAttribute3() {
        return JSONAttribute3;
    }

    public String getJSONAttribute4() {
        return JSONAttribute4;
    }

    public String getJSONAttribute5() {
        return JSONAttribute5;
    }

    public void setJSONAttribute2(String JSONAttribute2) {
        this.JSONAttribute2 = JSONAttribute2;
    }

    public void setJSONAttribute3(String JSONAttribute3) {
        this.JSONAttribute3 = JSONAttribute3;
    }

    public void setJSONAttribute4(String JSONAttribute4) {
        this.JSONAttribute4 = JSONAttribute4;
    }

    public void setJSONAttribute5(String JSONAttribute5) {
        this.JSONAttribute5 = JSONAttribute5;
    }

    public void setNegativeTest(boolean negativeTest) {
        this.negativeTest = negativeTest;
    }

    public boolean isNegativeTest() {
        return this.negativeTest;
    }

    public void setAssertCheck(boolean assertCheck) {
        this.assertCheck = assertCheck;
    }

    public boolean isAssertCheck() {
        return this.assertCheck;
    }

    public String toString() {
        return ("testId -> [" + testId + "] testObject -> [" + testObject + "] testDesc -> [" + testDesc +
                "] restTestVerb -> [" + restTestVerb + "] restURL -> [" + restURL + "] isDependentTest -> [" +
                dependentTest + "] dependentOnTest -> [" + dependentOnTest + "]");
    }
}
