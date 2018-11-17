package util;

import env.TestEnvironment;

import java.util.Map;

public class StringUtil {
    public StringUtil() {
        super();
    }


    /**
     * @param tokenValues {"OptyNumber":"CDRM_417598"}
     * @param strWithToken "/salesApi/resources/{latest}/opportunities/{OptyNumber}"
     * @return "/salesApi/resources/{latest}/opportunities/CDRM_417598"
     */
    public static String replaceTokens(Map<String, String> tokenValues, String strWithToken){
        StringBuilder newString = new StringBuilder();
        // String toReplace= null;
        int startPos= 0;
        int endPos= 0;
        startPos = strWithToken.indexOf("{", 0);
        if(startPos == -1){
            return strWithToken;
        }
        String placeHolderStr=null;
        newString.append(strWithToken.substring(0, startPos));
        while (startPos != -1 && startPos < strWithToken.length()) {
            endPos = strWithToken.indexOf("}", startPos);
            if(strWithToken.charAt(startPos) != '{'){                
              //  newString.append(strWithToken.substring(startPos, strWithToken.indexOf("{", startPos)));
                 newString.append(strWithToken.substring(startPos));
                return newString.toString();
            }            
            placeHolderStr = strWithToken.substring(startPos + 1, endPos);
            newString.append((String)tokenValues.get(placeHolderStr));
            startPos = endPos+1;
        }
        return newString.toString();
    }
    
    public static String replaceTokensForComplexURL(Map<String, String> parentValuesMap,
                                                    Map<String, String> childValuesMap,
                                                    String restURL) {
        if(TestEnvironment.DEBUG)
            System.out.println("   -- [DEBUG] Rest URL to be modified: " + restURL);
        System.out.println("TokenValues: " + parentValuesMap);
        System.out.println("TokenValues: " + childValuesMap);
        
        String parentKey = (String)parentValuesMap.keySet().toArray()[0];
        String parentKeyValue = parentValuesMap.get(parentKey);
        parentKey = "{" + parentKey + "}";

        restURL = restURL.replace(parentKey, parentKeyValue);
        
        String childKey = (String)childValuesMap.keySet().toArray()[0];
        String childKeyValue = childValuesMap.get(childKey);
        childKey = "{" + childKey + "}";
        
        restURL = restURL.replace(childKey, childKeyValue);
        if(TestEnvironment.DEBUG)
            System.out.println("   -- [DEBUG] Modified Rest URL: " + restURL);
        return restURL;
    }
}
