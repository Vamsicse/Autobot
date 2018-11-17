package util;

public class HtmlUtil {

    public static StringBuilder writeInitialHtmlContent(String hostNPort, String user, String password,
                                                        String requestPayloadPath, String responsePayloadPath,
                                                        String startTimeStamp) {
        StringBuilder htmlReport = new StringBuilder();

        htmlReport.append("<html><head><style>" + "td,th {border: 1px solid #dddddd;text-align: left;padding: 6px;}" +
                          "tr:nth-child(even) {background-color: silver;}\n" + "</style></head>");

        htmlReport.append("<body bgcolor=azure>");
        htmlReport.append("<center><h1>Autobot Test Results</h1><center>");

        // First Table starts here
        htmlReport.append("<table>\n\n" + "<tr>" + "<th><center>Parameter</center></th>" +
                          "<th><center>Value</center></th>" + "  </tr>" + "<tr>" + "<td>Host+Port</td>" + "<td>" +
                          hostNPort + "</td>" + "  </tr>" + "<tr>" + "<td>User</td>" + "<td>" + user + "</td>" +
                          "  </tr>" + "<tr>" + "<td>Password</td>" + "  <td>" + password + "</td>" + "</tr>" + "<tr>" +
                          "  <td>Request Payload Path</td>" + "  <td>" + requestPayloadPath + "</td>" + "  </tr>" +
                          "<tr>" + "<td>Response Payload Path</td>" + "  <td>" + responsePayloadPath + "</td>" +
                          "  </tr>" + "<tr><td> Run Start Time </td><td>" + startTimeStamp.substring(9) + "</td></tr>" +
                          "</table>\n\n" + "<br/><br/>");

        // Second Table starts here
        htmlReport.append("<table><tr><th><center>Test Case#</center></th>" +
                          "<th><center>Test Case Details</center></th>" + "<th><center>Status</center></th></tr>\n");
        return htmlReport;
    }

    public static StringBuilder writeTestCaseDetails(StringBuilder htmlReport, long htmlTestId, String testId,
                                                     String testObj, String verb, String testDesc, String restURL) {
        htmlReport.append("<tr><td>" + htmlTestId + "</td><td>");
        htmlReport.append("TestID: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[")
                  .append(testId)
                  .append("]<br/>\n");
        htmlReport.append("Object: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[")
                  .append(testObj)
                  .append("]<br/>\n");
        htmlReport.append("Type: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[")
                  .append(verb)
                  .append("]<br/>\n");
        htmlReport.append("TestDesc: &nbsp;&nbsp;&nbsp;&nbsp;[")
                  .append(testDesc)
                  .append("]<br/>\n");
        htmlReport.append("REST URL: &nbsp;[")
                  .append("<a href="+restURL+">"+restURL+"</a>")
                  .append("]<br/>\n");
        return htmlReport;
    }
    
    public static StringBuilder writeAssertionResult(StringBuilder htmlReport, boolean assertResult){//remove this var
        htmlReport.append("Assertion Result: &nbsp;[")
                  .append("Pass")
                  .append("]<br/>\n");
        return htmlReport;
    }
    
    public static StringBuilder writeTestCaseResults(StringBuilder htmlReport, long execTime, int status,
                                                     double respSize, String result) {
        htmlReport.append("Status Code: &nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[")
                  .append(status)
                  .append("]<br/>\n");

        if (result.equals("Failed")) {
            htmlReport.append("Assertion Status: &nbsp;[").append(result).append("]<br/>\n");
        }
        htmlReport.append("Execution Time: &nbsp;&nbsp;[")
                  .append(execTime)
                  .append(" milliseconds]<br/>\n");
        if (respSize > 0) {
            htmlReport.append("Response Size: &nbsp;&nbsp;&nbsp;[")
                      .append(respSize)
                      .append(" KB]<br/>\n");
        }
        htmlReport.append("<td>");
        if (result.equals("Passed")) {
            htmlReport.append("<font color=green>" + result + "</font>");
        } else if (result.equals("Failed")) {
            htmlReport.append("<font color=red>" + result + "</font>");
        } else {
            htmlReport.append("<font color=black>" + result + "</font>");
        }
        htmlReport.append("<br/></td></tr>\n\n");
        return htmlReport;
    }

    public static StringBuilder writeClosingContent(StringBuilder htmlReport) {
        htmlReport.append("\n\n<br/><br/><br/><br/><h3>For more info visit Results.log file</h3>");
        htmlReport.append("</body></html>");
        return htmlReport;
    }

    public static StringBuilder writeStatistics(StringBuilder htmlReport, double total, double pass, double skip, double fail, double passPercent) {
        htmlReport.append("</table>\n");
        htmlReport.append("<br/><br/><table border=1><caption><h2>Statistics</h2></caption>");
        htmlReport.append("<tr><td>Total Test Cases Ran</td><td>" + (int)total +"</td></tr>");
        htmlReport.append("<tr><td>Total Test Cases Passed</td><td><font color=green>" + (int)pass +"</font></td></tr>");
        htmlReport.append("<tr><td>Total Test Cases Skipped</td><td>" + (int)skip +"</td></tr>");
        htmlReport.append("<tr><td>Total Test Cases Failed</td><td><font color=red>" + (int)fail +"</font></td></tr>");
        htmlReport.append("<tr><td> Pass Percentage </td><td>" + passPercent +"</td></tr></table>\n");
        return htmlReport;
    }

    public static StringBuilder writeAssertFileAbsence(StringBuilder htmlReport,
                                                       String sourceFileName,
                                                       String responsePayloadPath) {
        htmlReport.append("Assertion Error: &nbsp; [")
                  .append(sourceFileName)
                  .append("] is absent at").append(" [" + responsePayloadPath + "] )<br/>\n");
        return htmlReport;
    }
}
