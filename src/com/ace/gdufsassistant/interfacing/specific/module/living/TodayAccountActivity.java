/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module.living;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.util.builder.DialogBuilder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import entity.Account;
import exception.UnfinishedException;
import main.API;

/**
 * @author wan
 *
 */
public class TodayAccountActivity extends Activity {
	private PullToRefreshListView scoreList;
	private TextView emptyText;
	private Handler handler = new MyHandler(this);
	private static class MyHandler extends Handler{
		private final WeakReference<TodayAccountActivity> mta;
		private MyHandler(TodayAccountActivity ta){
			mta = new WeakReference<TodayAccountActivity>(ta);
		}
		@Override
		public void handleMessage(Message msg) {
			if (mta == null) return;
			TodayAccountActivity ta = mta.get();
			switch (msg.what) {
			case 0x123:
				ta.emptyText.setVisibility(View.VISIBLE);
				break;
			case 0x124:
				ta.emptyText.setVisibility(View.GONE);
				break;
			case 0x125:
				DialogBuilder.showErrorDialogNoReturn(ta);
				break;
			case 0x126:
				DialogBuilder.showNotFinishedDialogNoReturn(ta);
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pull_to_refresh_list_view);
		emptyText = (TextView) findViewById(R.id.empty_message);
		scoreList = (PullToRefreshListView) findViewById(R.id.list);
		scoreList.setMode(Mode.PULL_FROM_START);
		scoreList.getLoadingLayoutProxy(false, true).setPullLabel("加载更多");
		scoreList.getLoadingLayoutProxy(false, true).setReleaseLabel("松开开始加载");
		scoreList.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载");
		scoreList.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				new RefreshDate().execute();
			}
		});
		new RefreshDate().execute();
	}

	private class RefreshDate extends AsyncTask<Void, Void, ArrayList<Account>> {
		@Override
		protected void onPostExecute(ArrayList<Account> result) {
			if (result == null) {
				handler.sendEmptyMessage(0x123);
			} else {
				handler.sendEmptyMessage(0x124);
				scoreList.setAdapter(new AccountAdapter(result, TodayAccountActivity.this));
				scoreList.onRefreshComplete();
			}
		}

		@Override
		protected ArrayList<Account> doInBackground(Void... arg0) {
			try {
				return API.getInstance().getTodayAccount();
			} catch (UnfinishedException e) {
				handler.sendEmptyMessage(0x126);
				return null;
			} catch (Exception e) {
				handler.sendEmptyMessage(0x125);
				return null;
			}
		}
	}

	public static class AccountAdapter extends BaseAdapter {
		private ArrayList<Account> account;
		private Activity activity;
		private int baseWidth;

		public AccountAdapter(ArrayList<Account> account, Activity activity) {
			this.account = account;
			this.activity = activity;
			DisplayMetrics dm = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
			baseWidth = dm.widthPixels / 6;
		}

		@Override
		public int getCount() {
			if(account.size()==0)return 1;
			return account.size()+2;
		}

		@Override
		public Object getItem(int arg0) {
			return account.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			if (arg0 == account.size() + 1)
				return -1;
			return arg0 - 1;
		}

		@Override
		public View getView(int position, View arg1, ViewGroup arg2) {
			LinearLayout line = new LinearLayout(activity);
			TextView text1 = new TextView(activity),
					text2 = new TextView(activity),
					text3 = new TextView(activity),
					text4 = new TextView(activity);
			text1.setWidth(baseWidth * 2);
			text2.setWidth(baseWidth * 2);
			text3.setWidth(baseWidth);
			text4.setWidth(baseWidth);
			initTextView(text1);
			initTextView(text2);
			initTextView(text3);
			initTextView(text4);
			if (position == 0) {
				text1.setText("时间");
				text2.setText("类型");
				text3.setText("金额");
				text4.setText("余额");
				line.setBackgroundColor(Color.GRAY);
			} else if (position == account.size() + 1) {
				text1.setText("总消费");
				float totalMoney = 0;
				float temp;
				for (int i = 0; i < account.size(); ++i){
					temp = account.get(i).getTradingVolume();
					if(temp<0)
						totalMoney+=temp; 
				}
				text3.setText(new DecimalFormat("0.0").format(totalMoney));
			} else {
				text1.setText(account.get(position - 1).getTime());
				text2.setText(account.get(position - 1).getTransactionType());
				text3.setText(String.valueOf(account.get(position - 1).getTradingVolume()));
				text4.setText(String.valueOf(account.get(position - 1).getBalance()));
			}

			line.addView(text1);
			line.addView(text2);
			line.addView(text3);
			line.addView(text4);

			return line;
		}
		private void initTextView(TextView text) {
			text.setTextSize(20);
		}
	}
}
