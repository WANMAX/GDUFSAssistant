/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module.living;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.util.ReloadApplication;
import com.ace.gdufsassistant.util.builder.DialogBuilder;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import entity.Weather;
import exception.UnfinishedException;
import main.API;

/**
 * @author wan
 *
 */
public class WeatherActivity extends Activity {
	final public static String []weatherName={"晴","多云","阴","雨","中雨","大雨","雷阵雨"};
	final public static int []weatherBigID={R.drawable.weather_sunny_big,R.drawable.weather_cloudy_big,R.drawable.weather_shadow_big,R.drawable.weather_smallrain_big,R.drawable.weather_middlerain_big,R.drawable.weather_bigrain_big,R.drawable.weather_thunderrain_big,R.drawable.weather_unknown_big};
	final public static int []weatherID={R.drawable.weather_sunny,R.drawable.weather_cloudy,R.drawable.weather_shadow,R.drawable.weather_smallrain,R.drawable.weather_middlerain,R.drawable.weather_bigrain,R.drawable.weather_thunderrain,R.drawable.weather_unknown};
	public static int nameToID(String name){
		for(int j=6;j>=0;j--)	
		{
			if(name.contains(WeatherActivity.weatherName[j]))
			{
				return j;
			}
		}
		return 7;
	}
	
	private Button[] buttons;
	private Handler handler = new MyHandler(this);
	private static class MyHandler extends Handler {
		private final WeakReference<WeatherActivity> mwa;
		private MyHandler(WeatherActivity wa) {
			mwa = new WeakReference<WeatherActivity>(wa);
		}
		public void handleMessage(android.os.Message msg) {
			if (mwa == null) return;
			WeatherActivity wa = mwa.get();
			switch(msg.what){
			case 0x123:
				Resources res = wa.getResources();
				String temperatureStr;
				int id;
				Drawable weatherDrawable;
				
				temperatureStr = weather.get(0).getLowestTemperature()+"~"+weather.get(0).getHighestTemperature();
				wa.buttons[0].setText(temperatureStr+"\n"+weather.get(0).getWind()+"\n"+weather.get(0).getWeek()+"("+weather.get(0).getDate()+")");
				id = nameToID(weather.get(0).getWeather());
				int hour = Integer.parseInt(new SimpleDateFormat("H").format(new Date()));
				switch(id){
				case 0:
					if(hour>=6&&hour<=18)
						weatherDrawable=res.getDrawable(R.drawable.weather_sunny_big);
					else weatherDrawable=res.getDrawable(R.drawable.weather_sunnynight_big);
					break;
				case 1:
					if(hour>=6&&hour<=18)
						weatherDrawable=res.getDrawable(R.drawable.weather_cloudy_big);
					else weatherDrawable=res.getDrawable(R.drawable.weather_cloudynight_big);
					break;
				default:
					weatherDrawable=res.getDrawable(WeatherActivity.weatherBigID[id]);
					break;
				}
				weatherDrawable.setBounds(5, 0, weatherDrawable.getIntrinsicWidth()+5, weatherDrawable.getIntrinsicHeight());
				wa.buttons[0].setCompoundDrawables(weatherDrawable, null, null,null);
				
				for(int i = 1;i < 3;++i){
					temperatureStr = weather.get(i).getLowestTemperature()+"~"+weather.get(i).getHighestTemperature();
					wa.buttons[i].setText(temperatureStr+"\n"+weather.get(i).getWind()+"\n"+weather.get(i).getWeek()+"("+weather.get(i).getDate()+")");
					id = nameToID(weather.get(i).getWeather());
					weatherDrawable = res.getDrawable(weatherID[id]);
					weatherDrawable.setBounds(0, 20, weatherDrawable.getIntrinsicWidth(), weatherDrawable.getIntrinsicHeight()+20);
					wa.buttons[i].setCompoundDrawables(null, weatherDrawable, null,null);
				}
				for(int i = 3;i < 7;++i){
					temperatureStr = weather.get(i).getLowestTemperature()+"~"+weather.get(i).getHighestTemperature();
					wa.buttons[i].setText(temperatureStr+"\n"+weather.get(i).getWind()+"\n"+weather.get(i).getWeek()+"("+weather.get(i).getDate()+")");
					id = nameToID(weather.get(i).getWeather());
					weatherDrawable = res.getDrawable(weatherID[id]);
					weatherDrawable.setBounds(0, 50, weatherDrawable.getIntrinsicWidth(), weatherDrawable.getIntrinsicHeight()+50);
					wa.buttons[i].setCompoundDrawables(null, weatherDrawable, null,null);
				}
				break;
			case 0x124:
				DialogBuilder.showErrorDialogWithoutLogin(wa);
				break;
			case 0x125:
				Toast.makeText(wa, "天气刷新完毕", Toast.LENGTH_SHORT).show();
				break;
			case 0x126:
				DialogBuilder.showNotFinishedDialog(wa);
				break;
			case 0x127:
				DialogBuilder.showNotFinishedDialogNoReturn(wa);
				break;
			case 0x128:
				DialogBuilder.showErrorNoReturnDialogWithoutLogin(wa);
				break;
			}
		};
	};
	
	private static ArrayList<Weather> weather = null;
	private static long time = 0;
	public static ArrayList<Weather> getWeather() throws Exception{
		if(weather!=null||System.currentTimeMillis()-time<3600000)return weather;
		weather = API.getInstance().getWeather();
		time = System.currentTimeMillis();
		return weather;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather_activity);
		buttons = new Button[7];
		buttons[0] = (Button)findViewById(R.id.weather_today);
		buttons[1] = (Button)findViewById(R.id.weather_tomorrow);
		buttons[2] = (Button)findViewById(R.id.weather_the_day_after_tomorrow);
		buttons[3] = (Button)findViewById(R.id.weather_1);
		buttons[4] = (Button)findViewById(R.id.weather_2);
		buttons[5] = (Button)findViewById(R.id.weather_3);
		buttons[6] = (Button)findViewById(R.id.weather_4);
		new Thread(){
			@Override
			public void run() {
				try{
					getWeather();
					handler.sendEmptyMessage(0x123);
				} catch(UnfinishedException e){
					handler.sendEmptyMessage(0x126);
				} catch(Exception e){
					handler.sendEmptyMessage(0x124);
				}
			}
		}.start();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0x123, 0, "刷新天气信息");
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0x123) {
			new Thread(){
				public void run() {
					try{
						weather = API.getInstance().getWeather();
						time = System.currentTimeMillis();
						handler.sendEmptyMessage(0x123);
						handler.sendEmptyMessage(0x125);
					} catch(UnfinishedException e){
						handler.sendEmptyMessage(0x127);
					}
					catch(Exception e){
						handler.sendEmptyMessage(0x128);
					}
				}
			}.start();
		}
		return true;
	}
}
