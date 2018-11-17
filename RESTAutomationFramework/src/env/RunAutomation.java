package env;

import execute.DataLoadHelper;

import java.awt.Toolkit;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class RunAutomation {

    private static String testSuiteFilePath;
    private static String inputFilePath, outputFilePath;
    private static String resultLocation;
    private static boolean readZCXData;
    private static String authScheme;
    private static boolean testMode, assertionMode, dataInjectionMode, debugMode, smartMode;
    private static String objectName;
    private static int range;
    private static Map<String, String> validateMap = new HashMap<String, String>();
    private static String osName;
    
    public static void main(String... arg) throws IOException {

        String details[] = new String[6];// change size

        final Properties properties = new Properties();
        if (arg.length < 1) {
            System.out.println("\n***** Please pass the location of TestSuite.properties file. Exiting ******\n");
            return;
        }
        testSuiteFilePath = arg[0];
        InputStream inputStream;

        inputStream = new FileInputStream(testSuiteFilePath);
        properties.load(inputStream);

        details[0] = properties.getProperty("HostandPort");
        details[1] = properties.getProperty("UserId");
        details[2] = properties.getProperty("Passwd");

        testMode = Boolean.valueOf(properties.getProperty("TestMode"));
        assertionMode = Boolean.valueOf(properties.getProperty("AssertionMode"));
        smartMode = Boolean.valueOf(properties.getProperty("SmartMode"));
        testSuiteFilePath = properties.getProperty("TestSuiteLocation");
        inputFilePath = properties.getProperty("InputLocation");
        outputFilePath = properties.getProperty("OutputLocation");
        resultLocation = properties.getProperty("ResultLocation");
        readZCXData = Boolean.valueOf(properties.getProperty("ReadZCXCustomizations"));
        authScheme = properties.getProperty("AuthenticationScheme");
        dataInjectionMode = Boolean.valueOf(properties.getProperty("DataInjectionMode"));
        debugMode = Boolean.valueOf(properties.getProperty("DebugMode"));
        osName = System.getProperty("os.name");
            
        validateMap.put(Constants.TESTSUITE_FILE_PATH, testSuiteFilePath);
        validateMap.put(Constants.REQUEST_PAYLOAD_PATH, inputFilePath);
        validateMap.put(Constants.RESPONSE_FILE_PATH, outputFilePath);

        System.out.println("\n\t***** Validating Inputs ******\n");
        System.out.println("*** Host&Port - - - - - - - - [ " + details[0] + " ]");
        System.out.println("*** Username - - - - - - - -  [ " + details[1] + " ]");
        System.out.println("*** Password - - - - - - - -  [ " + details[2] + " ]");
        System.out.println("*** Operating System- - - - - [ " + osName + " ]");
        System.out.println("*** Test Mode - - - - - - - - [ " + testMode + " ]");
        System.out.println("*** Global Assertion- - - - - [ " + assertionMode + " ]");
        System.out.println("*** Smart Mode - - - - - - -  [ " + smartMode + " ]");
        System.out.println("*** ArtifactFile - - - - - -  [ " + testSuiteFilePath + " ]");
        System.out.println("*** InputLocation - - - - - - [ " + inputFilePath + " ]");
        System.out.println("*** OutputLocation - - - - -  [ " + outputFilePath + " ]");
        System.out.println("*** Result Log Path - - - - - [ " + outputFilePath + " ]");
        System.out.println("*** Debug/Verbose Mode - - -  [ " + debugMode + " ]");
        System.out.println("*** Read Extensibility Data - [ " + readZCXData + " ]");
        System.out.println("*** Authentication Scheme - - [ " + authScheme + " ]");
        System.out.println("*** Data Pumping Mode - - - - [ " + dataInjectionMode + " ]");
        
        TestEnvironment.DEBUG=debugMode;
        
        if (dataInjectionMode) {
            objectName = properties.getProperty("ObjectName");
            range = Integer.valueOf(properties.getProperty("Range"));
            System.out.println("*** Target Object Name(s) -   [ " + objectName + " ]");
            System.out.println("*** No. of POST Operations -  [ " + range + " ]");
        }
        try {
            URL url = new URL(details[0]);
            if (url.getPort() == -1)
                throw new MalformedURLException(" *** Warning: Port is absent in the URL *** ");
        } catch (MalformedURLException e) {
            System.out.println("\n!! Warning: Port is absent in the URL. Ignore if there is no discrepancy\n");
        }
        
        if (testMode) {
            System.out.println("\nStarting Test Suite Run...\n");
            TestEnvironment.getTestEnvironment(validateMap).startTest(details);
            System.out.println("Test Suite Run Completed...\n");
        }
        else{
            System.out.println("\n** Test Mode Disabled...\n");
        }
        if (smartMode) {
            // Will implement this code in future
            // System.out.println("\nStarting Smart Test Run...\n");
            // TestEnvironment.getTestEnvironment(validateMap).startTest(details);
            // System.out.println("Smart Test Run Completed...\n");
        }
        else{
            System.out.println("\n** Smart Mode Disabled... **\n");
        }
        if (dataInjectionMode) {
            System.out.println("Starting Data Injection Operation ...\n");
            System.out.println("** Info: [ " +objectName + " ] object will be loaded with " + range + " records.\n");
            DataLoadHelper.loadObject(objectName, range, details);
            System.out.println("Data Injection operation Complete ...\n\n");
        }
        else{
            System.out.println("\n** Data Injection Mode Disabled... **\n");
        }
        Toolkit.getDefaultToolkit().beep();
    }

    public static String getOutputLocation() {
        return outputFilePath;
    }

    public static boolean isReadZCXData() {
        return readZCXData;
    }

    public static String getResultLocation() {
        return resultLocation;
    }
    
    public static boolean isAssertionEnabled(){
        return assertionMode;
    }
    
    public static String getOSName(){
        return osName;
    }
    
    public static Map<String, String> getValidateMap(){
        return validateMap;
    }
    
    public static String getTestSuiteFilePath() {
        return testSuiteFilePath;
    }
}
