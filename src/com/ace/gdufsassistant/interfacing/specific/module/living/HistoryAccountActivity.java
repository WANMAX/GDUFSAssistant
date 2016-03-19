/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module.living;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.util.builder.DialogBuilder;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import entity.Account;
import exception.UnfinishedException;
import main.API;

/**
 * @author wan
 *
 */
public class HistoryAccountActivity extends Activity {
	private ProgressBar progress;
	private ListView accountsList;
	private ArrayList<Account> accounts;
	private Handler handler = new MyHandler(this);
	private static class MyHandler extends Handler{
		private final WeakReference<HistoryAccountActivity> mactivity;
		private MyHandler(HistoryAccountActivity activity){
			mactivity = new WeakReference<HistoryAccountActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			if (mactivity == null) return;
			HistoryAccountActivity activity = mactivity.get();
			switch(msg.what){
			case 0x123:
				if (activity.accounts != null)
					activity.accountsList.setAdapter(new TodayAccountActivity.AccountAdapter(activity.accounts, activity));
				activity.progress.setVisibility(View.GONE);
				break;
			case 0x124:
				DialogBuilder.baseDialogNoReturn(activity, "日期输入格式需为20020202，请检查！").show();
				activity.progress.setVisibility(View.GONE);
				break;
			case 0x120:
				activity.progress.setVisibility(View.VISIBLE);
				break;
			case 0x121:
				DialogBuilder.showErrorDialogNoReturn(activity);
				activity.progress.setVisibility(View.GONE);
				break;
			case 0x122:
				DialogBuilder.showNotFinishedDialog(activity);
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_account_activity);
		progress = (ProgressBar) findViewById(R.id.progress);
		accountsList = (ListView) findViewById(R.id.accounts_list);
	}
	
	public void getHistoryAccount(View view){
		new Thread(){
			@Override
			public void run() {
				handler.sendEmptyMessage(0x120);
				try {
					SimpleDateFormat df = new SimpleDateFormat("yyyyDDmm");
					String startDay = ((EditText) findViewById(R.id.start_day))
							.getText().toString();
					df.parse(startDay);
					String endDay = ((EditText) findViewById(R.id.end_day))
							.getText().toString();
					df.parse(endDay);
					accounts = API.getInstance().getHistoryAccount(startDay,
							endDay);
					handler.sendEmptyMessage(0x123);
				} catch(ParseException e){
					handler.sendEmptyMessage(0x124);
				} catch(UnfinishedException e){
					handler.sendEmptyMessage(0x122);
				} catch (Exception e) {
					handler.sendEmptyMessage(0x121);
				}
			}
		}.start();
	}
}
