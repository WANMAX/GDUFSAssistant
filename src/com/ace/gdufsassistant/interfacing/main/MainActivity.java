/**
 * 
 */
package com.ace.gdufsassistant.interfacing.main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.interfacing.specific.module.learning.AnnouncementActivity;
import com.ace.gdufsassistant.interfacing.specific.module.learning.CETScoreActivity;
import com.ace.gdufsassistant.interfacing.specific.module.learning.NewsActivity;
import com.ace.gdufsassistant.interfacing.specific.module.learning.ScoreActivity;
import com.ace.gdufsassistant.interfacing.specific.module.living.WeatherActivity;
import com.ace.gdufsassistant.interfacing.specific.module.living.YKTActivity;
import com.ace.gdufsassistant.interfacing.specific.module.personal.center.AboutActivity;
import com.ace.gdufsassistant.interfacing.specific.module.personal.center.SettingActivity;
import com.ace.gdufsassistant.interfacing.specific.module.personal.center.UserInformationActivity;
import com.ace.gdufsassistant.util.ExitApplication;
import com.ace.gdufsassistant.util.TestNetworkState;
import com.ace.gdufsassistant.util.database.CourseDatabase;
import com.ace.gdufsassistant.util.myView.MyViewPager;
import com.ace.gdufsassistant.util.myView.MyViewPager.OnPageChangeListener;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import entity.Course;
import entity.Weather;
import main.API;

/**
 * @author wan
 * 
 */
public class MainActivity extends FragmentActivity {
	public static final int COURSESFRAGMENT = 0;
	public static final int LEARNINGFRAGMENT = 1;
	public static final int LIVINGFRAGMENT = 2;
	public static final int PERSONALCENTERFRAGMENT = 3;

	private SharedPreferences privateSP;
	private SharedPreferences defaultSP;
	
	private MyViewPager viewPager;
	private List<Fragment> fragmentList;
	private RadioGroup bottomList;

	private NotificationManager notificationM;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		viewPager = (MyViewPager)findViewById(R.id.fragment_root);
		fragmentList = new ArrayList<Fragment>();
		fragmentList.add(new CourseFragment());
		fragmentList.add(new LearningFragment());
		fragmentList.add(new LivingFragment());
		fragmentList.add(new PersonalCenterFragment());
		FragmentPagerAdapter fpAdapter =new FragmentPagerAdapter(getSupportFragmentManager())
		{
			@Override
			public int getCount()
			{
				return fragmentList.size();
			}
			@Override
			public Fragment getItem(int arg0)
			{
				return fragmentList.get(arg0);
			}
			
		};
		viewPager.setAdapter(fpAdapter);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0)
			{
				switch(arg0)
				{
				case COURSESFRAGMENT:
					((RadioButton) findViewById(R.id.button_courses)).setChecked(true);
					break;
				case LEARNINGFRAGMENT:
					((RadioButton) findViewById(R.id.button_learning)).setChecked(true);
					break;
				case LIVINGFRAGMENT:
					((RadioButton) findViewById(R.id.button_living)).setChecked(true);
					break;
				case PERSONALCENTERFRAGMENT:
					((RadioButton) findViewById(R.id.button_personal_center)).setChecked(true);
				}
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
		});
		viewPager.setOffscreenPageLimit(3);
		
		bottomList = (RadioGroup) findViewById(R.id.bottomList);
		bottomList.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				switch (arg1) {
				case R.id.button_courses:
					viewPager.setCurrentItem(COURSESFRAGMENT);
					break;
				case R.id.button_learning:
					viewPager.setCurrentItem(LEARNINGFRAGMENT);
					break;
				case R.id.button_living:
					viewPager.setCurrentItem(LIVINGFRAGMENT);
					break;
				case R.id.button_personal_center:
					viewPager.setCurrentItem(PERSONALCENTERFRAGMENT);
					break;
				}
			}
		});
		
		privateSP = getSharedPreferences("saveUserNamePwd", Context.MODE_PRIVATE);
		defaultSP = PreferenceManager.getDefaultSharedPreferences(this);
		
		notificationM = ((NotificationManager) MainActivity.this
				.getSystemService(NOTIFICATION_SERVICE));
		
		new Thread(){
			@Override
			public void run() {
				startAction();
			}
		}.start();
		
		ExitApplication.getInstance().addActivity(this);
	}
	
	private boolean showing = false;
	private Course showCourse;
	private final static int[][][] courseTime = {{{7, 45}, {8, 15}}, {{8, 30}, {9, 10}}, {{9, 10}, {9, 50}}, {{10, 10}, {10, 50}}, {{10, 50}, {11, 30}}, {{11, 35}, {12, 15}}, {{12, 15}, {14, 00}}, {{14, 00}, {14, 40}}, {{14, 40}, {15, 20}},
			{{15, 40}, {16, 20}}, {{16, 20}, {17, 00}}, {{18, 30}, {19, 10}}, {{19, 10}, {19, 50}}, {{20, 00}, {20, 40}}, {{20, 40}, {9, 20}}};
	private void showNotification(String title, String content) {
		Notification.Builder builder = new Notification.Builder(
				MainActivity.this);
		Intent intent = new Intent(MainActivity.this, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0,
				intent, 0);
		builder.setSmallIcon(R.drawable.ic_launcher).setContentTitle(title)
				.setContentText(content)
				.setContentIntent(pi)
				.setWhen(System.currentTimeMillis())
				.setDefaults(
						Notification.DEFAULT_SOUND
						| Notification.DEFAULT_VIBRATE
						| Notification.DEFAULT_LIGHTS);
		notificationM.notify(
				0x124, builder.build());
		showing = true;
	}
	private void cancelNotification() {
		notificationM.cancel(0x124);
		showCourse = null;
		showing = false;
	}
	private void courseAlert() {
		new Thread(){
			private ArrayList<Course>[] courses;
			@Override
			public void run() {
				while (true) {
					while (true) {
						try {
							CourseDatabase db = new CourseDatabase(MainActivity.this);
							courses = db.getCourse();
							break;
						} catch(Exception e) {
							try{
								Thread.sleep(60000);
							} catch(Exception ex){}
						}
					}
					try {
						int tag = 0;
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(new Date());
						int week = calendar.get(Calendar.DAY_OF_WEEK) - 2;
						week = week > 0 ? week : 6;
						ArrayList<Course> dayCourse = courses[week];
						Course temp = null;
						for (Course course : dayCourse) {
							int start = course.getStartTime();
							int end = start + course.getNumb()-1;
							int[] startTime = courseTime[start-1][0];
							int[] endTime = courseTime[end-1][1];
							Calendar alertCalendar = Calendar.getInstance();
							alertCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), startTime[0], startTime[1]);
							alertCalendar.add(Calendar.MINUTE, -30);
							Calendar startCalendar = Calendar.getInstance();
							startCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), startTime[0], startTime[1]);
							Calendar endCalendar = Calendar.getInstance();
							endCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), endTime[0], endTime[1]);
							if (calendar.compareTo(alertCalendar) > 0 && calendar.compareTo(startCalendar) < 0) {
									temp = course;
									tag = 1;
									break;
							}
							else if (calendar.compareTo(startCalendar) > 0 && calendar.compareTo(endCalendar) < 0) {
									temp = course;
									tag = 2;
									break;
							}
						}
						switch(tag) {
						case 1:
							if (!showing||!temp.equals(showCourse)) 
								showNotification("要上课啦！", courseTime[temp.getStartTime()-1][0][0]+":"+courseTime[temp.getStartTime()-1][0][1]+"在"+temp.getPlace()+"上"+temp.getCourseName());
							showCourse = temp;
							break;
						case 2:
							if (!showing||!temp.equals(showCourse)) 
								showNotification("正在上课！", "在"+temp.getPlace()+"上"+temp.getCourseName());
							showCourse = temp;
							break;
						case 0:
							cancelNotification();
						}
						Thread.sleep(60000);
					} catch (InterruptedException e) {
					}
				}
			}
		}.start();
	}
	
	private boolean weatherShowing = false;
	private String weatherContent;
	private void weatherAlert() {
		new Thread() {
			@Override
			public void run() {
				while (true) {
					Notification.Builder builder = null;
					boolean tag = false;
					String temp = null;
					while (true) {
						try {
							ArrayList<Weather >weather = WeatherActivity.getWeather();
							builder = new Notification.Builder(
									MainActivity.this);
							if (weather.get(0).getWeather().contains("雨")) {
								temp = "今天天气为" + weather.get(0).getWeather() + "，注意带伞！";
								builder.setTicker("今天可能有雨！").setContentText(
										temp);
							} else if (weather.get(1).getWeather().contains("雨")) {
								temp = "明天天气为" + weather.get(0).getWeather() + "，注意带伞！";
								builder.setTicker("明天可能有雨！").setContentText(
										temp);
							} else
								tag = true;
							break;
						} catch (Exception e) {
							try {
								Thread.sleep(60000);
							} catch (Exception ex){}
						}
					}
					if (tag &&weatherShowing) {
						notificationM.cancel(0x123);
						weatherShowing = false;
						weatherContent = null;
					}
					else if(!tag && (!weatherShowing || !temp.equals(weatherContent))) {
						Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
						PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0,
								intent, 0);
						builder.setSmallIcon(R.drawable.ic_launcher)
								.setContentTitle("天气预报")
								.setDefaults(
										Notification.DEFAULT_SOUND
												| Notification.DEFAULT_VIBRATE)
								.setWhen(System.currentTimeMillis())
								.setContentIntent(pi);
						notificationM.notify(
								0x123, builder.build());
						weatherShowing = true;
						weatherContent = temp;
					}
					try {
						Thread.sleep(3600000);
					} catch (Exception ex){}
				}
			}
		}.start();
	}
	
	private void startAction() {
		courseAlert();
		TestNetworkState.waitForNetwork(this);
		
		if (privateSP.getBoolean("auto_login", false)){
			final String username = privateSP.getString("name", "");
			final String password = privateSP.getString("password", "");
			new Thread(){
				public void run() {
					try {
						API api = API.getInstance();
						api.Login(username, password);
						while (true){
							try{
								api.getInformation();
								break;
							}catch(Exception e){}
						}
						if (defaultSP.getBoolean("display", true));
							UserInformationActivity.getPhoto();
					} catch (Exception e) {
					}
				};
			}.start();
		}
		
		new Thread() {
			public void run() {
				try{
					NewsActivity.getNews();
				}catch(Exception e){}
				try{
					AnnouncementActivity.getAnnouncement();
				}catch(Exception e){}
			}
		}.start();
		
		weatherAlert();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return moveTaskToBack(true);
		}
		return false;
	}

	public void exit(View view){
		new AlertDialog.Builder(this).setTitle(R.string.warning)
		.setMessage("确定要退出吗？")
		.setPositiveButton(R.string.apply, new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				ExitApplication.getInstance().exit();
			}
		}).setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
			}
		}).create().show();
	}
	public void startUserInformationActivity(View view){
		Intent intent = new Intent(this, UserInformationActivity.class);
		startActivity(intent);
	}
	public void startSettingActivity(View view){
		Intent intent = new Intent(this, SettingActivity.class);
		startActivity(intent);
	}
	public void startAboutActivity(View view){
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}
	public void showCoursesFragment(View view){
		viewPager.setCurrentItem(COURSESFRAGMENT);
	}
	public void showNewsActivity(View view){
		Intent intent = new Intent(this, NewsActivity.class);
		startActivity(intent);
	}
	public void showAnnouncementActivity(View view){
		Intent intent = new Intent(this, AnnouncementActivity.class);
		startActivity(intent);
	}
	public void showScoreActivity(View view){
		Intent intent = new Intent(this, ScoreActivity.class);
		startActivity(intent);
	}
	public void showCETScoreActivity(View view){
		Intent intent = new Intent(this, CETScoreActivity.class);
		startActivity(intent);
	}
	public void showWeatherActivity(View view){
		Intent intent = new Intent(this, WeatherActivity.class);
		startActivity(intent);
	}
	public void showYKTActivity(View view){
		Intent intent = new Intent(this, YKTActivity.class);
		startActivity(intent);
	}
}
