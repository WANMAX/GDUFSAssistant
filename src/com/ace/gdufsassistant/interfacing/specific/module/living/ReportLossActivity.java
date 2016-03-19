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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import exception.UnfinishedException;
import main.API;

public class ReportLossActivity extends Activity {
	private EditText passwordE;
	private ImageView passwordI;
	private Bitmap bm;
	private int width;
	private int height;
	private Handler handler = new MyHandler(this);
	private static class MyHandler extends Handler{
		private final WeakReference<ReportLossActivity> mactivity;
		private MyHandler(ReportLossActivity activity){
			mactivity = new WeakReference<ReportLossActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			if (mactivity == null) return;
			ReportLossActivity activity = mactivity.get();
			switch (msg.what) {
			case 0x123:
				activity.passwordI.setImageBitmap(activity.bm);
				activity.width = activity.passwordI.getWidth();
				activity.height= activity.passwordI.getHeight();
				break;
			case 0x124:
				DialogBuilder.baseDialog(activity, "安全密码图加载错误！").show();
				break;
			case 0x125:
				DialogBuilder.baseDialog(activity, "挂失成功！").show();
				break;
			case 0x126:
				DialogBuilder.baseDialogNoReturn(activity, "挂失失败！").show();
				activity.setImage();
				break;
			case 0x127:
				DialogBuilder.showNotFinishedDialog(activity);
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_loss_activity);

		passwordE = (EditText) findViewById(R.id.password);
		passwordE.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		passwordE.setKeyListener(null);

		passwordI = (ImageView) findViewById(R.id.password_photo);
		passwordI.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				String input;
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					input = getPhotoNumber((int) event.getX(), (int) event.getY(), (double) width / 150,
							(double) height / 190);
					if (!input.equals("back") && !input.equals("clear")) {
						passwordE.append(input);
					} else if (input.equals("clear")) {
						passwordE.setText("");
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
				} catch (UnfinishedException e) {
					handler.sendEmptyMessage(0x127);
				} catch (Exception e) {
					handler.sendEmptyMessage(0x124);
				}
			};
		}.start();
	}

	public void reportLoss(View view) {
		new AlertDialog.Builder(this).setTitle(R.string.recharge).setMessage("确定挂失吗？")
				.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						new Thread() {
							public void run() {
								try {
									API.getInstance().reportLoss(passwordE.getText().toString());
									handler.sendEmptyMessage(0x125);
								} catch(UnfinishedException e) {
									handler.sendEmptyMessage(0x127);
								}
								catch (Exception e) {
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

	public static String getPhotoNumber(int width, int height, double wx, double hx) {
		if (height >= 31 * hx & height <= 71 * hx) {
			if (width >= 4 * wx & width <= 44 * wx) {
				return "0";
			} else if (width >= 54 * wx & width <= 94 * wx) {
				return "1";
			} else if (width >= 99 * wx & width <= 141 * wx) {
				return "2";
			} else {
				return "";// 非法坐标，返回空值。
			}
		} else if (height >= 74 * hx & height <= 107 * hx) {
			if (width >= 5 * wx & width <= 46 * wx) {
				return "3";
			} else if (width >= 54 * wx & width <= 95 * wx) {
				return "4";
			} else if (width >= 101 * wx & width <= 141 * wx) {
				return "5";
			} else {
				return "";// 非法坐标，返回空值。
			}
		} else if (height >= 120 * hx & height <= 143 * hx) {
			if (width >= 6 * wx & width <= 47 * wx) {
				return "6";
			} else if (width >= 54 * wx & width <= 93 * wx) {
				return "7";
			} else if (width >= 102 * wx & width <= 142 * wx) {
				return "8";
			} else {
				return "";// 非法坐标，返回空值。
			}
		} else if (height >= 150 * hx & height <= 183 * hx) {
			if (width >= 6 * wx & width <= 46 * wx) {
				return "9";
			} else if (width >= 54 * wx & width <= 94 * wx) {
				return "clear";// 表示清除，即重置
			} else if (width >= 101 * wx & width <= 142 * wx) {
				return "back";// 表示输入完成需要退出。
			} else {
				return "";// 非法坐标，返回空值。
			}
		} else {
			return "";// 非法坐标，返回空值。
		}
	}
}
