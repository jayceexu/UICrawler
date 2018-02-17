package bach.jianxu.uicrawler;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

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
public class ExampleInstrumentedTest {
    private static final String TAG = "UICrawler";

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("bach.jianxu.uicrawler", appContext.getPackageName());
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
