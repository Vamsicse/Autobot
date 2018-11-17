package env;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import util.IOUtil;

public class Test {

    public static void main(String[] args) {
        Set<String> transKeywordsSet = new HashSet<String>();
        Set<String> staticKeywordSet = new HashSet<String>();
        Set<String> matchedKeywordSet = new HashSet<String>();
        String path = "D:\\";
        String fileName = "Trans.txt";
        String transDetails = IOUtil.readFromFile(path, fileName);
        System.out.println(transDetails);
        transKeywordsSet = extractTransKeywords(transDetails);
        transKeywordsSet = refineTransKeywords(transKeywordsSet);
        staticKeywordSet = loadStaticKeywordSet();
        matchedKeywordSet = loadmatchedKeywordSet(transKeywordsSet,staticKeywordSet);
    }

    public static Set<String> extractTransKeywords(String transDetails) {
        Set<String> transKeywordsSet = new HashSet<String>();
        int startIndex = transDetails.indexOf("AFFECTED FILES:");
        System.out.println("Start index of File Names: " + startIndex);
        int endIndex = transDetails.indexOf("CHECKEDOUT RESERVED:");
        System.out.println("End index of File Names: " + endIndex + "\n\n\n");
        String affectedFiles = transDetails.substring(startIndex + 15, endIndex);
        System.out.println("AFFECTED FILES:\n" + affectedFiles);
        System.out.println("Start and EndIndex of Trans: " + Arrays.asList(affectedFiles.split("/")));
        String[] keywords = affectedFiles.split("/");
        for (int i = 0; i < keywords.length; i++) {
            transKeywordsSet.add(keywords[i]);
        }
        System.out.println("Transaction Keywords Length: " + transKeywordsSet.size());
        System.out.println("Set Elements: \n" + Arrays.asList(transKeywordsSet));
        return transKeywordsSet;
    }

    public static Set<String> refineTransKeywords(Set<String> transKeywordsSet) {
        return transKeywordsSet;
    }

    public static Set<String> loadStaticKeywordSet() {
        Set<String> staticKeywordSet = new HashSet<String>();
        staticKeywordSet.add("moo");
        staticKeywordSet.add("mot");
        return staticKeywordSet;
    }
    public static Set<String> loadmatchedKeywordSet(Set<String> transKeywordsSet,Set<String> staticKeywordSet) {
        transKeywordsSet.retainAll(staticKeywordSet);
        return transKeywordsSet;
    }
    
}
