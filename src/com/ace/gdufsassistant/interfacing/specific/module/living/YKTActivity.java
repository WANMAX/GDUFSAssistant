/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module.living;

import java.lang.ref.WeakReference;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.util.TestActivityState;
import com.ace.gdufsassistant.util.builder.DialogBuilder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import entity.YKTInformation;
import exception.LoginFailedException;
import exception.NotLoggedException;
import exception.UnfinishedException;
import main.API;

/**
 * @author wan
 *
 */
public class YKTActivity extends Activity {
	private TextView state;
	private TextView balance;
	private static YKTInformation yktInformation;
	private Handler handler = new MyHandler(this);
	private static class MyHandler extends Handler {
		private final WeakReference<YKTActivity> mykt;
		private MyHandler(YKTActivity activity) {
			mykt = new WeakReference<YKTActivity>(activity);
		}
		public void handleMessage(Message msg) {
			if (mykt == null) return;
			YKTActivity ykt = mykt.get();
			switch(msg.what){
			case 0x123:
				String content = "帐号：";
				content += yktInformation.getID();
				content += "\n状态：";
				content += yktInformation.getStateName();
				ykt.state.setText(content);
				content = "余额：";
				content += String.valueOf(yktInformation.getBalance());
				content += "元\n过渡余额：";
				content += String.valueOf(yktInformation.getTransitionBalance());
				content += "元";
				ykt.balance.setText(content);
				break;
			case 0x124:
				DialogBuilder.showErrorDialog(ykt);
				break;
			case 0x125:
				Toast.makeText(ykt, "校园通刷新完毕", Toast.LENGTH_SHORT).show();
				break;
			case 0x126:
				DialogBuilder.showNotFinishedDialog(ykt);
				break;
			case 0x127:
				DialogBuilder.showNotFinishedDialogNoReturn(ykt);
				break;
			case 0x128:
				DialogBuilder.showErrorDialogNoReturn(ykt);
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ykt_activity);
		state = (TextView) findViewById(R.id.state);
		balance = (TextView) findViewById(R.id.balance);
		new Thread(){
			public void run() {
				try{
					if(yktInformation==null)
						yktInformation = API.getInstance().getYKTInformation();
					handler.sendEmptyMessage(0x123);
					new Thread(){
						public void run() {
							while(TestActivityState.isTopActivy("com.ace.gdufsassistant.interfacing.specific.module.living.YKTActivity", YKTActivity.this)){
								try {
									this.sleep(60000);
								} catch (InterruptedException e1) {
								}
								new Thread(){
									public void run() {
										try{
											yktInformation = API.getInstance().getYKTInformation();
											handler.sendEmptyMessage(0x123);
										}catch(Exception e){
										}
									};
								}.start();
							}
						};
					}.start();
				} catch(UnfinishedException e) {
					handler.sendEmptyMessage(0x126);
				} catch(Exception e) {
					handler.sendEmptyMessage(0x124);
				}
			};
		}.start();
	}
	
	public static YKTInformation getYKTInformation() throws NotLoggedException, LoginFailedException, UnfinishedException{
		return yktInformation = API.getInstance().getYKTInformation();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0x123, 0, "更新校园通信息");
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0x123) {
			new Thread(){
				public void run() {
					try{
						yktInformation = API.getInstance().getYKTInformation();
						handler.sendEmptyMessage(0x123);
						handler.sendEmptyMessage(0x125);
					}catch(UnfinishedException e){
						handler.sendEmptyMessage(0x127);
					}catch(Exception e){
						handler.sendEmptyMessage(0x128);
					}
				};
			}.start();
		}
		return true;
	}
	public void showRechargeActivity(View view) {
		Intent intent = new Intent(this, RechargeActivity.class);
		startActivity(intent);
	}
	public void showTodayAccount(View view) {
		Intent intent = new Intent(this, TodayAccountActivity.class);
		startActivity(intent);
	}
	public void showHistoryAccoun(View view) {
		Intent intent = new Intent(this, HistoryAccountActivity.class);
		startActivity(intent);
	}
	public void showReportLossActivity(View view) {
		Intent intent = new Intent(this, ReportLossActivity.class);
		startActivity(intent);
	}
	public void showModifyPasswordActivity(View view) {
		Intent intent = new Intent(this, ModifyPasswordActivity.class);
		startActivity(intent);
	}
	
}
