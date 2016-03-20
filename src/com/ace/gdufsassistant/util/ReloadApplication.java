/**
 * 每次新建activity，务必要将其联系到ExitApplication，退出时用这个类进行统一关闭。
 */
package com.ace.gdufsassistant.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Application;


public class ReloadApplication extends Application {
	private ArrayList<Reload> reloadList = new ArrayList<Reload>();
	private ReloadApplication(){}
	
	private static class InstanceContainer{private static Map<String, ReloadApplication> instance = new HashMap<String, ReloadApplication>();} 
	public static ReloadApplication getInstance(String name){
		if(!InstanceContainer.instance.containsKey(name))
			InstanceContainer.instance.put(name, new ReloadApplication());
		return InstanceContainer.instance.get(name);
	}
	
	public void addReload(Reload reload)
	{
		reloadList.add(reload);
	}
	public void reload(){
		for(Reload reload:reloadList){
			reload.reload();
		}
	}
 }