/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module.learning;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.interfacing.specific.module.WebViewActivity;
import com.ace.gdufsassistant.util.builder.DialogBuilder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import entity.News;
import exception.UnfinishedException;
import main.API;

/**
 * @author wan
 *
 */
public class NewsActivity extends Activity {
	private PullToRefreshListView newsList;
	private NewsAdapter adapter;
	
	private static ArrayList<News> news;

	private Handler handler = new MyHandler(this);
	private static class MyHandler extends Handler {
		private final WeakReference<NewsActivity> mna;
		private MyHandler(NewsActivity na) {
			mna = new WeakReference<NewsActivity>(na);
		}
		@Override
		public void handleMessage(Message msg) {
			if (mna==null) return;
			NewsActivity na = mna.get();
			switch(msg.what) {
			case 0x120:
				DialogBuilder.showNotFinishedDialog(na);
				break;
			case 0x121:
				DialogBuilder.showErrorDialogWithoutLogin(na);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pull_to_refresh_list_view);
		newsList = (PullToRefreshListView) findViewById(R.id.list);
		newsList.setMode(Mode.BOTH);
		newsList.getLoadingLayoutProxy(false, true).setPullLabel("加载更多");
		newsList.getLoadingLayoutProxy(false, true).setReleaseLabel("松开开始加载");
		newsList.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载");
		newsList.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if (newsList.isShownHeader()) {
					new RefreshDate().execute();
				} else if (newsList.isShownFooter()) {
					new GetMoreData().execute();
				}
			}
		});

		if (getNewsForStart() == null)
			new RefreshDate().execute();
		else
			newsList.setAdapter(adapter = new NewsAdapter(getNewsForStart(), NewsActivity.this));
	}

	public static ArrayList<News> getNews() throws Exception{
		if (news!=null) return news;
		return news = API.getInstance().getNewsFirstPage();
	}
	
	protected ArrayList<News> getNewsForStart(){
		return news;
	}
	
	protected ArrayList<News> refresh() throws Exception {
		return news = API.getInstance().getNewsFirstPage();
	}

	protected ArrayList<News> getMore() throws Exception {
		return API.getInstance().getNewsNextPage();
	}

	// 刷新列表
	private class RefreshDate extends AsyncTask<Void, Void, ArrayList<News>> {
		@Override
		protected void onPostExecute(ArrayList<News> result) {
			if (result == null) {
				return;
			}
			newsList.setAdapter(adapter = new NewsAdapter(result, NewsActivity.this));
			newsList.onRefreshComplete();
		}

		@Override
		protected ArrayList<News> doInBackground(Void... arg0) {
			try {
				return refresh();
			} catch(UnfinishedException e) {
				handler.sendEmptyMessage(0x120);
				return null;
			} catch (Exception e) {
				handler.sendEmptyMessage(0x121);
				return null;
			}
		}
	}

	// 增加列表
	private class GetMoreData extends AsyncTask<Void, Void, ArrayList<News>> {
		@Override
		protected void onPostExecute(ArrayList<News> result) {
			if (result == null) {
				handler.sendEmptyMessage(0x119);
				return;
			}
			adapter.append(result);
			adapter.notifyDataSetChanged();
			newsList.onRefreshComplete();
		}

		@Override
		protected ArrayList<News> doInBackground(Void... arg0) {
			try {
				return getMore();
			} catch (Exception e) {
				return null;
			}
		}

	}

	public static class NewsAdapter extends BaseAdapter {
		private ArrayList<News> list;
		private Activity activity;
		private int resourceID;

		public NewsAdapter(ArrayList<News> list, Activity activity) {
			this.list = list;
			this.activity = activity;
			this.resourceID = -1;
		}
		
		public NewsAdapter(ArrayList<News> list, Activity activity, int resourceID) {
			this.list = list;
			this.activity = activity;
			this.resourceID = resourceID;
		}

		public void append(ArrayList<News> list) {
			this.list.addAll(list);
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {
			LayoutInflater inflater = activity.getLayoutInflater();
			View view = inflater.inflate(R.layout.news_item, null);
			((TextView) view.findViewById(R.id.title)).setText(list.get(arg0).getTitle());
			((TextView) view.findViewById(R.id.date)).setText(list.get(arg0).getDate());
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg3) {
					Intent intent = new Intent(activity, WebViewActivity.class);
					intent.putExtra("url", list.get(arg0).getUrl().toString());
					activity.startActivity(intent);
				}
			});
			if (resourceID!=-1)
				view.setBackgroundResource(resourceID);
			return view;
		}
	}
}
