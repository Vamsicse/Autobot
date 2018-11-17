package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil {
    public static boolean compareFiles(String file1, String file2) throws FileNotFoundException, IOException{
        File outFile = new File(file1); // OUTFILE
        File inFIle = new File(file2); // INPUT

        FileReader outFReader = new FileReader(outFile);
        FileReader inFReader = new FileReader(inFIle);

        BufferedReader reader1 = new BufferedReader(outFReader);
        BufferedReader reader2 = new BufferedReader(inFReader);

        String line1 = null;
        String line2 = null;
        while (((line1 = reader1.readLine()) != null) &&
               ((line2 = reader2.readLine()) != null)) {
            if (!line1.equalsIgnoreCase(line2))
                return false;
        }
        reader1.close();
        reader2.close();
        return true;
    }
}

