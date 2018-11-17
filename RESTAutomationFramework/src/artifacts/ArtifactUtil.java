package artifacts;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class ArtifactUtil {

    private static final String FILE_NAME = "TestSuite.xml";

    public static MainRESTArtifact jaxbXMLToObject(String testSuiteAbsPath) {
        try {
            JAXBContext context = JAXBContext.newInstance(MainRESTArtifact.class);
            Unmarshaller un = context.createUnmarshaller();
            MainRESTArtifact allTestCases = null;

            if (testSuiteAbsPath == null) {
                testSuiteAbsPath = FILE_NAME;
            }
            File testSuiteFile = new File(testSuiteAbsPath);
            if (!testSuiteFile.exists()) {
                throw new RuntimeException("TestSuite.xml file path is not correct [" + testSuiteAbsPath + "]");
            } else {
                allTestCases = (MainRESTArtifact) un.unmarshal(testSuiteFile);
            }

            return allTestCases;
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }
}
