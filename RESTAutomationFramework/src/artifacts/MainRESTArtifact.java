package artifacts;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name = "TestSuite")
@XmlType(propOrder = {
         "hostNPort", "user", "password", "artifact"
    })
public class MainRESTArtifact {
    
    private String hostNPort = "http://localhost:7011";
    private String user = "mhoope";
    private String password = "Welcome1";

    private List<RESTTestArtifact> testcase = new ArrayList<RESTTestArtifact>();

    public void setHostNPort(String hostNPort) {
        this.hostNPort = hostNPort;
    }

    public String getHostNPort() {
        return hostNPort;
    }
    
    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }


    public void setArtifact(List<RESTTestArtifact> testcase) {
        this.testcase = testcase;
    }

    @XmlElement(name = "testcase")
    public List<RESTTestArtifact> getArtifact() {
        return testcase;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HostNPort -> [").append(hostNPort).append("] \n");
        for (RESTTestArtifact ra : testcase) {
            sb.append(ra.toString()).append("\n");
        }
        return sb.toString();
    }
}
