/**
 * 
 */
package com.ace.gdufsassistant.interfacing.main;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.interfacing.specific.module.living.WeatherActivity;
import com.ace.gdufsassistant.interfacing.specific.module.living.YKTActivity;
import com.ace.gdufsassistant.util.TestActivityState;
import com.ace.gdufsassistant.util.TestNetworkState;
import com.ace.gdufsassistant.util.builder.DialogBuilder;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import entity.Weather;
import entity.YKTInformation;
import main.API;

/**
 * @author wan
 *
 */
public class LivingFragment extends Fragment{
	private Weather todayWeather;
	private YKTInformation yktInformation;
	private Button weatherB;
	private Button YKTB;
	
	private Handler handler = new MyHandler(this);
	private static class MyHandler extends Handler {
		private final WeakReference<LivingFragment> mlf; 
		private MyHandler(LivingFragment lf) {
			mlf = new WeakReference<LivingFragment>(lf);
		}
		public void handleMessage(Message msg) {
			if (mlf == null) return;
			LivingFragment lf = mlf.get();
			switch(msg.what){
			case 0x123:
				int hour = Integer.parseInt(new SimpleDateFormat("H").format(new Date()));
				String temperatureStr = lf.todayWeather.getLowestTemperature()+"~"+lf.todayWeather.getHighestTemperature();
				lf.weatherB.setText(temperatureStr+"\n\n"+lf.todayWeather.getWind());
				String weatherString=lf.todayWeather.getWeather();
				Resources res = lf.getResources();
				Drawable weatherDrawable;
				int weatherID = WeatherActivity.nameToID(weatherString);
				switch(weatherID){
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
					weatherDrawable=res.getDrawable(WeatherActivity.weatherBigID[weatherID]);
					break;
				}
				weatherDrawable.setBounds(30, 0, weatherDrawable.getIntrinsicWidth()+30, weatherDrawable.getIntrinsicHeight());
//				lf.weatherB.setHeight(200);
				lf.weatherB.setCompoundDrawables(weatherDrawable,null, null,null); 
				break;
			case 0x124:
				if (lf.yktInformation.getStateCode()==YKTInformation.NORMAL){
					String content = lf.getResources().getString(R.string.YKT)+"  余额:"+lf.yktInformation.getBalance();
					if(lf.yktInformation.getTransitionBalance()!=0)
						content += "(过渡:"+ lf.yktInformation.getTransitionBalance()+")";
					lf.YKTB.setText(content);
				}
				else
					lf.YKTB.setText(lf.getResources().getString(R.string.YKT)+"  状态不正常！");
				break;
			case 0x125:
				lf.YKTB.setText(R.string.YKT);
				break;
			case 0x999:
				DialogBuilder.baseDialogNoReturn(lf.getActivity(), test).show();
				break;
			}
		};
	};
	private static String test = "test";
	public void onResume() {
		super.onResume();
		new Thread(){
			public void run() {
				TestNetworkState.waitForNetwork(getActivity());
				try{
					todayWeather = WeatherActivity.getWeather().get(0);
					handler.sendEmptyMessage(0x123);
				}catch(Exception e){}
				API api = API.getInstance();
				if(api.isLogged()) {
					try{
						yktInformation = YKTActivity.getYKTInformation();
						handler.sendEmptyMessage(0x124);
					}catch(Exception e){return;}
					new Thread(){
						boolean continue_ = true;
						public void run() {
							while(TestActivityState.isTopActivy("com.ace.gdufsassistant.interfacing.main.MainActivity", getActivity())&&continue_){
								try {
									Thread.sleep(300000);
								} catch (InterruptedException e1) {
								}
								new Thread(){
									public void run() {
										try{
											yktInformation = YKTActivity.getYKTInformation();
											handler.sendEmptyMessage(0x124);
										}catch(Exception e){
											continue_ = false;
										}
									};
								}.start();
							}
						};
					}.start();
				}
				else {
					handler.sendEmptyMessage(0x125);
				}
			};
		}.start();
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.living_fragment, container, false);
		weatherB = (Button) view.findViewById(R.id.weather);
		YKTB = (Button) view.findViewById(R.id.YKT);
		return view;
	}
}
