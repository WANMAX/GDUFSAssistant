/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module.learning;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.util.builder.DialogBuilder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import entity.CETScore;
import exception.UnfinishedException;
import main.API;

/**
 * @author wan
 *
 */
public class CETScoreActivity extends Activity {
	private PullToRefreshListView scoreList;
	private TextView emptyText;
	private Handler handler = new MyHandler(this);
	private static class MyHandler extends Handler {
		private final WeakReference<CETScoreActivity> mcet;
		private MyHandler(CETScoreActivity cet) {
			mcet = new WeakReference<CETScoreActivity>(cet);
		}
		@Override
		public void handleMessage(Message msg) {
			if (mcet==null) return;
			CETScoreActivity cet = mcet.get();
			switch(msg.what){
			case 0x123:
				cet.emptyText.setVisibility(View.VISIBLE);
				break;
			case 0x124:
				cet.emptyText.setVisibility(View.GONE);
				break;
			case 0x125:
				DialogBuilder.showNotFinishedDialog(cet);
				break;
			case 0x126:
				DialogBuilder.showErrorDialog(cet);
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pull_to_refresh_list_view);
		emptyText = (TextView)findViewById(R.id.empty_message);
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

	private class RefreshDate extends AsyncTask<Void, Void, ArrayList<CETScore>> {
		@Override
		protected void onPostExecute(ArrayList<CETScore> result) {
			if (result==null) {
				handler.sendEmptyMessage(0x123);
			}
			else{
				handler.sendEmptyMessage(0x124);
				scoreList.setAdapter(new CETScoreAdapter(result, CETScoreActivity.this));
				scoreList.onRefreshComplete();
			}
		}

		@Override
		protected ArrayList<CETScore> doInBackground(Void... arg0) {
			try {
				return API.getInstance().getCETScore();
			} catch(UnfinishedException e) {
				handler.sendEmptyMessage(0x125);
				return null;
			} catch (Exception e) {
				handler.sendEmptyMessage(0x126);
				return null;
			}
		}
	}

	private static class CETScoreAdapter extends BaseAdapter {
		private ArrayList<CETScore> score;
		private Activity activity;

		private CETScoreAdapter(ArrayList<CETScore> score, Activity activity) {
			this.score = score;
			this.activity = activity;
		}

		@Override
		public int getCount() {
			return score.size();
		}

		@Override
		public Object getItem(int arg0) {
			return score.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View view = activity.getLayoutInflater().inflate(R.layout.cet_score_item, null);
			((TextView) view.findViewById(R.id.name)).setText("类别："+score.get(arg0).getExaminationName());
			((TextView) view.findViewById(R.id.number)).setText("考号："+score.get(arg0).getExamineeNumber());
			((TextView) view.findViewById(R.id.date)).setText("时间："+score.get(arg0).getDate());

			((TextView) view.findViewById(R.id.listening_score))
					.setText(String.valueOf(score.get(arg0).getListeningScore()));
			((TextView) view.findViewById(R.id.reading_score))
					.setText(String.valueOf(score.get(arg0).getReadingScore()));
			((TextView) view.findViewById(R.id.writing_score))
					.setText(String.valueOf(score.get(arg0).getWritingScore()));
			((TextView) view.findViewById(R.id.comprehensive_score))
					.setText(String.valueOf(score.get(arg0).getComprehensiveScore()));
			((TextView) view.findViewById(R.id.score)).setText(score.get(arg0).getScore());
			view.setBackgroundResource(R.drawable.button_rectangle);
			return view;
		}
	};
}
