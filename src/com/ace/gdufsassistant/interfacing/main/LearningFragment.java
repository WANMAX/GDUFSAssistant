/**
 * 
 */
package com.ace.gdufsassistant.interfacing.main;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.interfacing.specific.module.learning.AnnouncementActivity;
import com.ace.gdufsassistant.interfacing.specific.module.learning.NewsActivity;
import com.ace.gdufsassistant.util.TestNetworkState;
import com.ace.gdufsassistant.util.builder.DialogBuilder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.Button;
import entity.News;

/**
 * @author wan
 *
 */
public class LearningFragment extends Fragment {
	private Button newsB;
	private Button announcementB;
	private AdapterViewFlipper newsF;
	private AdapterViewFlipper announcementF;
	
	private ArrayList<News> news;
	private ArrayList<News> announcement;
	
	private Handler handler = new MyHandler(this);
	private static class MyHandler extends Handler {
		private final WeakReference<LearningFragment> mlf; 
		private MyHandler(LearningFragment lf){
			mlf = new WeakReference<LearningFragment>(lf);
		}
		@Override
		public void handleMessage(Message msg) {
			if (mlf == null) return;
			LearningFragment lf = mlf.get();
			switch(msg.what){
			case 0x123:
				lf.newsB.setBackgroundResource(R.drawable.button_top_rectangle);
				lf.newsF.setAdapter(new NewsActivity.NewsAdapter(lf.news, lf.getActivity(), R.drawable.drop_list));
				lf.newsF.setVisibility(View.VISIBLE);
				lf.newsF.startFlipping();
				break;
			case 0x124:
				lf.announcementB.setBackgroundResource(R.drawable.button_top_rectangle);
				lf.announcementF.setAdapter(new NewsActivity.NewsAdapter(lf.announcement, lf.getActivity(), R.drawable.drop_list));
				lf.announcementF.setVisibility(View.VISIBLE);
				lf.announcementF.startFlipping();
				break;
			}
		};
	};
	@Override
	public void onResume() {
		super.onResume();
		new Thread(){
			@Override
			public void run() {
				TestNetworkState.waitForNetwork(getActivity());
				try{
					news = NewsActivity.getNews();
					handler.sendEmptyMessage(0x123);
				}catch(Exception e){}
				try{
					announcement = AnnouncementActivity.getAnnouncement();
					handler.sendEmptyMessage(0x124);
				}catch(Exception e){
				}
			};
		}.start();
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.learning_fragment, container, false);
		newsB = (Button)view.findViewById(R.id.news);
		announcementB = (Button)view.findViewById(R.id.announcement);
		newsF = (AdapterViewFlipper)view.findViewById(R.id.news_flipper);
		announcementF = (AdapterViewFlipper)view.findViewById(R.id.announcement_flipper);
		return view;
	}
}
