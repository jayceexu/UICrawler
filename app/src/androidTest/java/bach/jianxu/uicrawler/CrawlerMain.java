package bach.jianxu.uicrawler;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.Configurator;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 *
 * In this project, we primarily use this test framework to grab UI/images for creating UI dataset
 * It's a forked project from https://github.com/Eaway/AppCrawler.
 *
 * UICrawler fixes bugs and supports dumping XML and images
 *
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class CrawlerMain {
    private static final String TAG = "UICrawler";

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = getTargetContext();

        assertEquals("bach.jianxu.uicrawler", appContext.getPackageName());
    }

    @BeforeClass
    public static void beforeClass() {
        // Create screenshot folder
        File path = Environment.getExternalStorageDirectory();
        Config.sOutputDir = new File(String.format("%s/uicrawler/%s", path.getAbsolutePath(), Config.sTargetPackage));
        if (!Config.sOutputDir.exists()) {
            if (!Config.sOutputDir.mkdirs()) {
                Log.e(TAG, "Failed to create screenshot folder: " + Config.sOutputDir.getPath());
                return;
            }
        }

        //  Set timeout longer so we can see the ANR dialog?
        Configurator conf = Configurator.getInstance();
        conf.setActionAcknowledgmentTimeout(200L); // Generally, this timeout should not be modified, default 3000
        conf.setScrollAcknowledgmentTimeout(100L); // Generally, this timeout should not be modified, default 200
        conf.setWaitForIdleTimeout(0L);
        conf.setWaitForSelectorTimeout(0L);

        // Register UiWatchers: ANR, CRASH, ....
        Utility.registerAnrAndCrashWatchers();

        // Good practice to start from the home screen (launcher)
        Utility.launchHome();
    }

    /**
     * The entry point of UICrawler...
     */
    @Test
    public void testMain() {


        Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() + "()");
        DepthFirstCrawler crawler = new DepthFirstCrawler();

        try {
            crawler.run();
        } catch (IllegalStateException e) {
            Log.v(TAG, "IllegalStateException: UiAutomation not connected!");
        }
    }
}
