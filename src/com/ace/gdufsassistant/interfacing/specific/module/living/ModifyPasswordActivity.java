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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import exception.UnfinishedException;
import main.API;

/**
 * @author wan
 *
 */
public class ModifyPasswordActivity extends Activity {
	private EditText currentE;
	private EditText oldPasswordE1;
	private EditText oldPasswordE2;
	private EditText newPasswordE;
	private ImageView passwordI;
	private Bitmap bm;
	private int width;
	private int height;
	private Handler handler = new MyHandler(this);
	private static class MyHandler extends Handler{
		private final WeakReference<ModifyPasswordActivity> mactivity;
		private MyHandler(ModifyPasswordActivity activity){
			mactivity = new WeakReference<ModifyPasswordActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			if (mactivity == null) return;
			ModifyPasswordActivity activity = mactivity.get();
			switch (msg.what) {
			case 0x123:
				activity.passwordI.setImageBitmap(activity.bm);
				activity.width = activity.passwordI.getWidth();
				activity.height=activity. passwordI.getHeight();
				break;
			case 0x124:
				DialogBuilder.baseDialog(activity, "安全密码图加载错误！").show();
				break;
			case 0x125:
				DialogBuilder.baseDialog(activity, "修改成功！").show();
				break;
			case 0x126:
				DialogBuilder.baseDialogNoReturn(activity, "修改失败！").show();
				activity.setImage();
				activity.oldPasswordE1.setText("");
				activity.oldPasswordE2.setText("");
				activity.newPasswordE.setText("");
				break;
			case 0x127:
				DialogBuilder.baseDialogNoReturn(activity, "两次输入的密码不一样！").show();
				activity.oldPasswordE1.setText("");
				activity.oldPasswordE2.setText("");
				activity.newPasswordE.setText("");
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
		setContentView(R.layout.modify_password_activity);

		oldPasswordE1 = (EditText) findViewById(R.id.old_password1);
		oldPasswordE1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				currentE = oldPasswordE1;
				return false;
			}
		});
		oldPasswordE1.setKeyListener(null);
		oldPasswordE2 = (EditText) findViewById(R.id.old_password2);
		oldPasswordE2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				currentE = oldPasswordE2;
				return false;
			}
		});
		oldPasswordE2.setKeyListener(null);
		newPasswordE = (EditText) findViewById(R.id.new_password);
		newPasswordE.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				currentE = newPasswordE;
				return false;
			}
		});
		newPasswordE.setKeyListener(null);

		currentE = oldPasswordE1;
		
		passwordI = (ImageView) findViewById(R.id.password_photo);
		passwordI.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				String input;
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					input = ReportLossActivity.getPhotoNumber((int) event.getX(), (int) event.getY(), (double) width / 150,
							(double) height / 190);
					if (!input.equals("back") && !input.equals("clear")) {
						currentE.append(input);
					} else if (input.equals("clear")) {
						currentE.setText("");
					} else if (input.equals("back")) {
						new Thread(){
							public void run() {
								new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
							};
						}.start();
					}
				}
				return true;
			}
		});
		setImage();
	}

	private void setImage() {
		new Thread() {
			public void run() {
				try {
					byte[] imageB = API.getInstance().getPasswordImage();
					bm = BitmapFactory.decodeByteArray(imageB, 0, imageB.length);
					handler.sendEmptyMessage(0x123);
				} catch (Exception e) {
					handler.sendEmptyMessage(0x124);
				}
			};
		}.start();
	}
	
	public void modifyPassword(View view) {
		new AlertDialog.Builder(this).setTitle(R.string.recharge).setMessage("确定挂失吗？")
				.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						new Thread() {
							public void run() {
								String password1 = oldPasswordE1.getText().toString();
								String password2 = oldPasswordE2.getText().toString();
								if(!password1.equals(password2)){
									handler.sendEmptyMessage(0x127);
									return;
								}
								try {
									API.getInstance().modifyPassword(password1, newPasswordE.getText().toString());
									handler.sendEmptyMessage(0x125);
								} catch(UnfinishedException e){
									handler.sendEmptyMessage(0x128);
								} catch (Exception e) {
									handler.sendEmptyMessage(0x126);
								}
							};
						}.start();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				}).create().show();
	}
}
