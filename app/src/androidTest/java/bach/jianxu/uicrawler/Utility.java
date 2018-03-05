package bach.jianxu.uicrawler;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * UiAutomator helper
 */
public class Utility {
    private static final String TAG = Config.TAG;
    private static final String TAG_MAIN = Config.TAG_MAIN;

    public static int sScreenshotIndex = 0;

    public static String sLastFilename;

    public static UiWatchers sUiWatchers = new UiWatchers();

    public static void registerAnrAndCrashWatchers() {
        sUiWatchers.registerAnrAndCrashWatchers();
    }

    public static boolean handleAndroidUi() {
        if (sUiWatchers.handleAnr()) {
            takeScreenshots("[ANR]");
            return true;
        } else if (sUiWatchers.handleAnr2()) {
            takeScreenshots("[ANR]");
            return true;
        } else if (sUiWatchers.handleCrash()) {
            takeScreenshots("[CRASH]");
            return true;
        } else if (sUiWatchers.handleCrash2()) {
            takeScreenshots("[CRASH]");
            return true;
        } else {
            // Something we don't know
        }

        return false;
    }

    public static boolean handleCommonDialog() {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject button = null;
        for (String keyword : Config.COMMON_BUTTONS) {
            button = device.findObject(new UiSelector().text(keyword).enabled(true));
            if (button != null && button.exists()) {
                break;
            }
        }
        try {
            // sometimes it takes a while for the OK button to become enabled
            if (button != null && button.exists()) {
                button.waitForExists(5000);
                button.click();
                Log.i("AppCrawlerAction", "{Click} " + button.getText() + " Button succeeded");
                return true; // triggered
            }
        } catch (UiObjectNotFoundException e) {
            Log.w(TAG, "UiObject disappear");
        }
        return false; // no trigger
    }

    public static void inputRandomTextToEditText() {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject edit = null;
        int i = 0;
        do {
            edit = device.findObject(new UiSelector().className("android.widget.EditText").instance(i++));
            if (edit != null && edit.exists()) {
                try {
                    Random rand = new Random();
                    String text = Config.RANDOM_TEXT[rand.nextInt(Config.RANDOM_TEXT.length - 1)];
                    edit.setText(text);
                } catch (UiObjectNotFoundException e) {
                    // Don't worry
                }
            }
        } while (edit != null && edit.exists());
    }

    public static boolean isInTargetApp() {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        String pkg = device.getCurrentPackageName();
        if (pkg != null && 0 == pkg.compareToIgnoreCase(Config.sTargetPackage)) {
            return true;
        }
        return false;
    }

    public static boolean isInIgnoredActivity(UiScreen screen) {
        return isInIgnoredActivity(screen.name);
    }

    public static boolean isInIgnoredActivity(String activityName) {
        for (String ignore : Config.IGNORED_ACTIVITY) {
            if (0 == ignore.compareTo(activityName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInTheSameScreen(UiScreen target) {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject root = device.findObject(new UiSelector().packageName(Config.sTargetPackage));
        if (root == null || !root.exists()) {
            Log.e(TAG, "Fail to get screen root object");
            return false;
        }
        UiScreen current = new UiScreen(null, null, root);
        boolean result = current.equals(target);
        return result;
    }

    public static boolean launchTargetApp() {
        if (launchApp(Config.sTargetPackage)) {
            return true;
        }

        return false;
    }

    public static boolean launchApp(String targetPackage) {
        Log.i(TAG_MAIN, "{Launch} " + targetPackage);

        UiDevice device = UiDevice.getInstance(getInstrumentation());
        String launcherPackage = device.getLauncherPackageName();
        if (launcherPackage.compareToIgnoreCase(targetPackage) == 0) {
            launchHome();
            return true;
        }
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(targetPackage);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // Make sure each launch is a new task
            context.startActivity(intent);
            device.wait(Until.hasObject(By.pkg(Config.sTargetPackage).depth(0)), Config.sLaunchTimeout);
        } else {
            String err = String.format("(%s) No launchable Activity.\n", targetPackage);
            Log.e(TAG, err);
            Bundle bundle = new Bundle();
            bundle.putString("ERROR", err);
            getInstrumentation().finish(1, bundle);
        }
        return true;
    }

    public static void launchHome() {
        Log.i(TAG_MAIN, "{Press} Home");

        UiDevice uidevice = UiDevice.getInstance(getInstrumentation());
        uidevice.pressHome();
        String launcherPackage = uidevice.getLauncherPackageName();
        uidevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), Config.sLaunchTimeout);
    }

    public static String toValidFileName(String input) {
        if (input == null)
            return "";
        return input.replaceAll("[:\\\\/*\"?|<>']", "_");
    }

    // Take screenshots in Landscape and Portrait
    // TODO: Take screenshot for both portrait and landscape
    public static void takeScreenshots(String message) {
        //Log.v(TAG, new Exception().getStackTrace()[0].getMethodName() + "()");

        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.waitForIdle(Config.sWaitIdleTimeout);

        String activity = device.getCurrentActivityName(); // FIXME: deprecated
        if (activity == null)
            activity = "No Activity name";
        if (activity.length() > 30) {
            activity = activity.substring(0, 29);
        }

        // Dump window hierarchy for debug, remove it for better performance
        try {
            String fname = String.format("%s/%d_%s.xml", Config.sOutputDir.getAbsoluteFile(), sScreenshotIndex, activity);
            device.dumpWindowHierarchy(new File(fname));
            Log.i(TAG, "Dumping xml " + fname);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (message == null) {
            message = "";
        } else if (message.length() > 50) {
            message = message.substring(0, 49);
        }

        sLastFilename = "";
        if (message.length() > 0) {
            sLastFilename = String.format("%d_%s_%s.png",
                    sScreenshotIndex, toValidFileName(activity), toValidFileName(message));
        } else {
            sLastFilename = String.format("%d_%s.png", sScreenshotIndex, toValidFileName(activity));
        }
        device.takeScreenshot(new File(Config.sOutputDir.getAbsoluteFile() + "/" + sLastFilename));
        Log.i(TAG, "Screenshot stored at " + Config.sOutputDir.getAbsolutePath() + "/" + sLastFilename);
        sScreenshotIndex++;
        Log.i(TAG_MAIN, "{Screenshot} " + sLastFilename);
    }

    // imageName does not need a file extension like .png, just the name
    public static void storeBitmap(Bitmap bitmap, String folder, String imageName) {
        try {
            //create app folder
            File sdcard = Environment.getExternalStorageDirectory();
            File dir = new File(sdcard.getPath() + File.separator + folder);
            String appFolder = "";

            boolean isDirCreated = dir.exists() || dir.mkdirs();

            if (isDirCreated) {
                appFolder = dir.getPath();
                Log.d(TAG, "dir created success:" + appFolder);
            } else {
                Log.e(TAG, "dir failed to create");
            }

            File imageFile = new File(appFolder + File.separator + imageName + ".jpg");

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

            outputStream.flush();
            outputStream.close();

        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    public static HashSet<String> readText() {

        StringBuilder text = new StringBuilder();
        HashSet<String> apps = new HashSet<>();
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard,"app_list");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if (line == "" || line == null)
                    continue;
                text.append(line);
                text.append('\n');
                apps.add(line);
            }
            br.close() ;
            Log.d(TAG, "READ file " + text);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return apps;

    }

}
