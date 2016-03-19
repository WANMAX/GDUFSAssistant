/**
 * 
 */
package com.ace.gdufsassistant.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author wan
 *
 */
public class TestNetworkState {
	public static boolean testNetwork(Activity activity) {
		ConnectivityManager cwjManager=(ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo info = cwjManager.getActiveNetworkInfo(); 
		if (info != null && info.isAvailable()) 
			return true;
		else
			return false;
	}
	public static void waitForNetwork(Activity activity, long ms) {
		while (!testNetwork(activity)) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
			}
		}
	}
	public static void waitForNetwork(Activity activity) {
		waitForNetwork(activity, 500);
	}
}
