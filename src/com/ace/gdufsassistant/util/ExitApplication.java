/**
 * 每次新建activity，务必要将其联系到ExitApplication，退出时用这个类进行统一关闭。
 */
package com.ace.gdufsassistant.util;

import java.util.ArrayList;
import android.app.Activity;
import android.app.Application;


public class ExitApplication extends Application {
	private ArrayList<Activity> activityList = new ArrayList<Activity>();
	
	private static ExitApplication instance = new ExitApplication();
	private ExitApplication(){}
	public static ExitApplication getInstance(){
		return instance;
	}
	
	public void addActivity(Activity activity)
	{
		activityList.add(activity);
	}
	public void exit(){
		for(Activity activity:activityList){
			activity.finish();
		}
		System.exit(0);
	}
 }