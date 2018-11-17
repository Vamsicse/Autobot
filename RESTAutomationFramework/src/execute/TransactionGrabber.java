package execute;


public class TransactionGrabber {
    public static String getTransDetails() {
        String os = System.getProperty("os.name");
        if (os.startsWith("Windows"))
            return null;
        else
            return null;
    }
}
