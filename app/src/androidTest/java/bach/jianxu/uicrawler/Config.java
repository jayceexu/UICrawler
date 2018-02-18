package bach.jianxu.uicrawler;

import java.io.File;

/**
 * Configurations
 */
public class Config {
    public static final String TAG = "UICrawler";
    public static final String TAG_MAIN = TAG + "Main";

    public static int sLaunchTimeout = 5000;
    public static int sWaitIdleTimeout = 100;
    public static int sMaxDepth = 30;
    public static int sMaxSteps = 999;
    public static int sMaxRuntime = 3600;
    public static int sMaxScreenshot = 999;
    public static int sMaxScreenLoop = 20;
    public static int sScreenSignatueLength = 160;

    public static boolean sDebug = false;
    public static boolean sCaptureSteps = false;
    public static boolean sRandomText = true;

    public static File sOutputDir;
    public static String sOutputDirName = "/sdcard/uicrawler/";

    // TODO: add package lists
    public static String sTargetPackage = "com.spotify.music";
    //public static String sTargetPackage = "com.google.android.apps.wearable.settings";


    // Activities to be ignored
    public static final String[] IGNORED_ACTIVITY = {
            "Feedback & Help"
    };

    // Common buttons that we can handle.
    public static final String[] COMMON_BUTTONS = {
            "OK", "Cancel", "Yes", "No"
    };

    // Text for EditText testing
    public static final String[] RANDOM_TEXT = {
            "LOVE", "Latte", "Coffee", "Beer"
    };

}
