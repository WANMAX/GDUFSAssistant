/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module.learning;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.util.builder.DialogBuilder;
import com.ace.gdufsassistant.util.builder.TermBuilder;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import entity.Score;
import exception.UnfinishedException;
import main.API;

/**
 * @author wan
 *
 */
public class ScoreActivity extends Activity {
	private ProgressBar pb;
	private ListView scoreList;
	private ListAdapter adapter;

	private Handler handler = new MyHandler(this);
	private static class MyHandler extends Handler {
		private final WeakReference<ScoreActivity> msa;
		private MyHandler(ScoreActivity sa) {
			msa = new WeakReference<ScoreActivity>(sa);
		}
		@Override
		public void handleMessage(Message msg) {
			if (msa==null) return;
			ScoreActivity sa = msa.get();
			switch (msg.what) {
			case 0x120:
				sa.pb.setVisibility(View.VISIBLE);
				break;
			case 0x122:
				DialogBuilder.showNotFinishedDialogNoReturn(sa);
				sa.pb.setVisibility(View.GONE);
				break;
			case 0x123:
				DialogBuilder.showErrorDialogNoReturn(sa);
				sa.pb.setVisibility(View.GONE);
				break;
			case 0x124:
				if (sa.adapter!=null)
					sa.scoreList.setAdapter(sa.adapter);
				sa.pb.setVisibility(View.GONE);
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.score_activity);

		scoreList = (ListView) findViewById(R.id.score_list);
		pb = (ProgressBar) findViewById(R.id.progress);

		TermBuilder tb = TermBuilder.getInstance();
		final String[] yearArr = tb.getYear();
		final String[] termArr = tb.getTerm();
		final Spinner yearSpinner = (Spinner) findViewById(R.id.year);
		yearSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, yearArr));
		final Spinner termSpinner = (Spinner) findViewById(R.id.term);
		termSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, termArr));
		final Button applyButton = (Button) findViewById(R.id.apply);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		final int baseWidth = dm.widthPixels / 7;

		applyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new Thread() {
					@Override
					public void run() {
						String year = yearArr[yearSpinner.getSelectedItemPosition()];
						String term = termArr[termSpinner.getSelectedItemPosition()];

						handler.sendEmptyMessage(0x120);

						final ArrayList<Score> score;
						try {
							score = API.getInstance().getScore(year, term);
							adapter = new BaseAdapter() {
								@Override
								public int getCount() {
									if (score.size() == 0)
										return 1;
									return score.size() + 2;
								}

								@Override
								public Object getItem(int arg0) {
									return null;
								}

								@Override
								public long getItemId(int position) {
									if (position == score.size() + 1)
										return -1;
									return position - 1;
								}

								@Override
								public View getView(int position, View arg1, ViewGroup arg2) {
									LinearLayout line = new LinearLayout(ScoreActivity.this);
									TextView text1 = new TextView(ScoreActivity.this),
											text2 = new TextView(ScoreActivity.this),
											text3 = new TextView(ScoreActivity.this),
											text4 = new TextView(ScoreActivity.this),
											text5 = new TextView(ScoreActivity.this);

									text1.setWidth(baseWidth * 3);
									text2.setWidth(baseWidth);
									text3.setWidth(baseWidth);
									text4.setWidth(baseWidth);
									text5.setWidth(baseWidth);
									initTextView(text1);
									initTextView(text2);
									initTextView(text3);
									initTextView(text4);
									initTextView(text5);

									if (position == 0) {
										text1.setText("课程名字");
										text2.setText("平时成绩");
										text3.setText("期末成绩");
										text4.setText("总评");
										text5.setText("绩点");
										line.setBackgroundColor(Color.GRAY);
									} else if (position == score.size() + 1) {
										text1.setText("平均绩点");
										float totalScore = 0;
										float totalCredit = 0;
										for (int i = 0; i < score.size(); ++i){
											totalCredit+= Float.parseFloat(score.get(i).getCredit());
											totalScore += Float.parseFloat(score.get(i).getGradePoint())*Float.parseFloat(score.get(i).getCredit());
										}
										text5.setText(new DecimalFormat("0.00").format(totalScore / totalCredit));
									} else {
										text1.setText(score.get(position - 1).getCourseName());
										text2.setText(score.get(position - 1).getNormalPerformance());
										text3.setText(score.get(position - 1).getFinalExamScore());
										text4.setText(score.get(position - 1).getScore());
										text5.setText(score.get(position - 1).getGradePoint());
									}

									line.addView(text1);
									line.addView(text2);
									line.addView(text3);
									line.addView(text4);
									line.addView(text5);

									return line;
								}

								private void initTextView(TextView text) {
									text.setTextSize(20);
								}
							};
						} catch(UnfinishedException e) {
							handler.sendEmptyMessage(0x122);
							adapter = null;
						} catch (Exception e) {
							handler.sendEmptyMessage(0x123);
							adapter = null;
						}
						handler.sendEmptyMessage(0x124);
					};
				}.start();
			}
		});
	}
}
