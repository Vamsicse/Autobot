package util;

import env.RunAutomation;
import env.TestEnvironment;

import execute.RESTTestAutomationManager;

import org.json.JSONException;
import org.json.JSONObject;

public class ZCXUtil {

    private static StringBuilder zcxReport = new StringBuilder();

    public static void extractZCXData(String response, String testObject) throws JSONException {
        int zcxFields = 0, size = 0;
        JSONObject json = null;
        boolean isCustObj = testObject.endsWith("_c");
        boolean childCustObjPresent = false;

        String currentObject = testObject.substring(0, 1).toUpperCase() + testObject.substring(1);
        if (TestEnvironment.DEBUG) {
            System.out.println("   -- [DEBUG] Started extracting Extensibility Info..");
        }
        try {
            json = new JSONObject(response);
        } catch (Exception e) {
            System.out.println("!!! Error: Describe failed for " + currentObject);
            zcxReport.append("Error: Describe failed for " + currentObject + "\n");
            IOUtil.writeToFile(RunAutomation.getOutputLocation(),
                               currentObject + "_ZCXData" + RESTTestAutomationManager.getTimeStamp() + ".txt",
                               zcxReport.toString());
            zcxReport.setLength(0);
            return;
        }

        JSONObject jsonZCX = new JSONObject(json.getJSONObject("Resources")
                                                .getJSONObject(testObject)
                                                .toString());
        zcxReport.append("ZCX Data for Object: " + currentObject + "\n");

        zcxFields = extractObjectZCXData(jsonZCX);

        if (zcxFields == 0)
            zcxReport.append("No ZCX Fields are present for the Object [" + currentObject + "].\n\n");
        else
            System.out.println("** Info: Generated ZCX Data for [" + currentObject + "] object.\n");

        if (isCustObj)
            size = json.getJSONObject("Resources")
                       .getJSONObject(testObject)
                       .getJSONObject("children")
                       .length();

        if (size > 3)
            childCustObjPresent = true;

        if (childCustObjPresent) {
            jsonZCX = new JSONObject(json.getJSONObject("Resources")
                                         .getJSONObject(testObject)
                                         .getJSONObject("children")
                                         .toString());

            for (int i = 0; i < size; i++) {
                String key = jsonZCX.names()
                                    .get(i)
                                    .toString();
                if (key.endsWith("_c") && !key.startsWith("LOVVA_For_")) {
                    JSONObject currentJSON = new JSONObject(jsonZCX.get(key).toString());
                    zcxReport.append("ZCX Data for Child Object: " + currentJSON.get("title") + "\n");
                    zcxFields = extractObjectZCXData(new JSONObject((jsonZCX.get(key).toString())));
                    if (zcxFields == 0)
                        zcxReport.append("No ZCX Fields are present for the Child Object [" + currentJSON.get("title") +
                                         "].\n");
                    else
                        System.out.println("** Info: Generated ZCX Data for [" + currentJSON.get("title") +
                                           "] object.\n");
                }
            }
        }

        IOUtil.writeToFile(RunAutomation.getOutputLocation(),
                           currentObject + "_ZCXData" + RESTTestAutomationManager.getTimeStamp() + ".txt",
                           zcxReport.toString());
        zcxReport.setLength(0);
    }


    public static int extractObjectZCXData(JSONObject jsonZCX) throws JSONException {
        zcxReport.append("***************************************************\n");
        int zcxFields = 0;
        int length = jsonZCX.getJSONArray("attributes").length();
        for (int i = 0; i < length; i++) {
            JSONObject currentObj = new JSONObject(jsonZCX.getJSONArray("attributes")
                                                          .getJSONObject(i)
                                                          .toString());
            String fieldName = jsonZCX.getJSONArray("attributes")
                                      .getJSONObject(i)
                                      .get("name")
                                      .toString();

            if (fieldName.endsWith("_c")) {
                String attributeType = null;
                try {
                    attributeType = currentObj.getJSONObject("properties")
                                              .get("AttributeType")
                                              .toString();
                } catch (JSONException e) {
                    continue;
                }

                if (attributeType.equals("ForeignKey"))
                    continue;

                zcxReport.append("Custom Field Name: " + currentObj.get("title") + "\n");
                if (attributeType.equals("Lookup")) {
                    // attributeType for DCL is ForeignKey
                    zcxReport.append("Attribute Type:    " + "DCL/" + attributeType + "\n");
                    zcxReport.append("Lookup Type:       " + currentObj.getJSONObject("properties").get("LookupType") +
                                     "\n");
                    zcxReport.append("Display Attribute: " +
                                     currentObj.getJSONObject("properties").get("ListDisplayAttribute") + "\n");
                } else if (attributeType.equals("Picklist")) {
                    // attributeType for FCL is Picklist
                    zcxReport.append("Attribute Type:    " + "FCL/" + attributeType + "\n");
                    zcxReport.append("Lookup Type:       " + currentObj.getJSONObject("properties").get("LookupType") +
                                     "\n");
                } else if (attributeType.equals("RecordType")) {
                    zcxReport.append("Attribute Type:    " + attributeType + "\n");
                    zcxReport.append("Lookup Type:       " + currentObj.getJSONObject("properties").get("LookupType") +
                                     "\n");
                } else if (attributeType.equals("Clob"))
                    zcxReport.append("Attribute Type:    " + "LongText/" + attributeType + "\n");
                else
                    zcxReport.append("Attribute Type:    " + attributeType + "\n");
                zcxFields++;
                zcxReport.append("\n");
            }
        }

        return zcxFields;
    }
}
