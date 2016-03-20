/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module.living;

import java.lang.ref.WeakReference;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.util.builder.DialogBuilder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import entity.YKTInformation;
import exception.UnfinishedException;
import main.API;

/**
 * @author wan
 *
 */
public class RechargeActivity extends Activity {
	private YKTInformation ykt;
	private EditText money;
	private EditText password;
	private Handler handler = new MyHandler(this);
	private static class MyHandler extends Handler{
		private final WeakReference<RechargeActivity> mactivity;
		private MyHandler(RechargeActivity activity){
			mactivity = new WeakReference<RechargeActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			if (mactivity == null) return;
			RechargeActivity activity = mactivity.get();
			switch (msg.what) {
			case 0x123:
				try {
					((TextView) activity.findViewById(R.id.name)).setText(API.getInstance().getInformation().getName());
					((TextView) activity.findViewById(R.id.student_number)).setText(API.getInstance().getStudentNumber());
				} catch (Exception e) {
				}
				((TextView) activity.findViewById(R.id.ykt_number)).setText(activity.ykt.getID());
				((TextView) activity.findViewById(R.id.balance)).setText(String.valueOf(activity.ykt.getBalance()));
				((TextView) activity.findViewById(R.id.transition_balance)).setText(String.valueOf(activity.ykt.getTransitionBalance()));
				((TextView) activity.findViewById(R.id.state)).setText(activity.ykt.getStateName());
				break;
			case 0x120:
				DialogBuilder.showErrorDialog(activity);
				break;
			case 0x125:
				DialogBuilder.baseDialogNoReturn(activity, "充值成功！").show();
				break;
			case 0x126:
				DialogBuilder.baseDialogNoReturn(activity, "金额输入有误！").show();
				break;
			case 0x127:
				DialogBuilder.baseDialogNoReturn(activity, "充值失败！").show();
				break;
			case 0x128:
				DialogBuilder.showNotFinishedDialog(activity);
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recharge_activity);
		money = (EditText) findViewById(R.id.money);
		password = (EditText) findViewById(R.id.password);
		new Thread() {
			public void run() {
				try {
					ykt = API.getInstance().getYKTInformation();
					handler.sendEmptyMessage(0x123);
				} catch (Exception e) {
					handler.sendEmptyMessage(0x120);
				}
			};
		}.start();
	}

	public void recharge(View view) {
		new AlertDialog.Builder(this).setTitle(R.string.recharge).setMessage("确定充值吗？")
				.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						new Thread() {
							@Override
							public void run() {
								try {
									API.getInstance().recharge(Integer.parseInt(money.getText().toString()),
											password.getText().toString());
									handler.sendEmptyMessage(0x125);
								} catch (NumberFormatException e) {
									handler.sendEmptyMessage(0x126);
								} catch (UnfinishedException e) {
									handler.sendEmptyMessage(0x128);
								} catch (Exception e) {
									handler.sendEmptyMessage(0x127);
								}
							}
						}.start();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				}).create().show();
	}

	public void cancel(View view) {
		new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
	}
}
