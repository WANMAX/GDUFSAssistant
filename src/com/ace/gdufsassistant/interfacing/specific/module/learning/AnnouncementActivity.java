/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module.learning;

import java.util.ArrayList;

import entity.News;
import main.API;

/**
 * @author wan
 *
 */
public class AnnouncementActivity extends NewsActivity {
	private static ArrayList<News> announcement;

	public static ArrayList<News> getAnnouncement() throws Exception{
		if (announcement!=null) return announcement;
		return announcement = API.getInstance().getAnnouncementFirstPage();
	}
	
	protected ArrayList<News> getNewsForStart(){
		return announcement;
	}
	
	protected ArrayList<News> refresh() throws Exception {
		return announcement = API.getInstance().getAnnouncementFirstPage();
	}

	protected ArrayList<News> getMore() throws Exception {
		return API.getInstance().getAnnouncementNextPage();
	}
}
