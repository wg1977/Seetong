package com.seetong.app.seetong.ui.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Administrator on 2015/12/18.
 */
public class ActivityUtil {
    private static String TAG = "Utils";
    private static ArrayList<Activity> activityList = new ArrayList<>();
    public static void addToList(Activity activity) {
        activityList.add(activity);
    }

    public static void stopApp() {
        Iterator<Activity> itr = activityList.iterator();
        Activity a = null;
        while (itr.hasNext()) {
            a = itr.next();
            a.finish();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
