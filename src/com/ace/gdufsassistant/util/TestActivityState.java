/**
 * 
 */
package com.ace.gdufsassistant.util;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;

/**
 * @author wan
 *
 */
public class TestActivityState {
	public static boolean isTopActivy(String cmdName, Activity context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(Integer.MAX_VALUE);
		String cmpNameTemp = null;
		if (null != runningTaskInfos) {
			cmpNameTemp = runningTaskInfos.get(0).topActivity.getClassName();
		}
		if (null == cmpNameTemp) {
			return false;
		}
		return cmpNameTemp.equals(cmdName);
	}
}
