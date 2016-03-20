/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module.personal.center;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.util.builder.DialogBuilder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import entity.Information;
import exception.NotLoggedException;
import main.API;

/**
 * @author wan
 *
 */
public class UserInformationActivity extends Activity {
	private final static String PATH = "GDUFSAssistant/";
	public final static String SHARENAME = "saveUserNamePwd";

	private static API api = API.getInstance();
	private static Information information;
	private static SharedPreferences privateSP;
	private static SharedPreferences defaultSP;

	private Dialog dialog;
	private EditText usernameE;
	private EditText passwordE;
	private CheckBox rememberCB;
	private CheckBox autoLoginCB;
	private Button loginB;
	private Button logoutB;
	private ProgressBar progressBar;

	private ImageView photoI;
	private TextView nameT;
	private TextView studentNumberT;
	private TextView identityT;
	private TextView academyT;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0x123:
				try {
					nameT.setText(information.getName());
					studentNumberT.setText(api.getStudentNumber());
					identityT.setText(information.getIdentity());
					academyT.setText(information.getAcademy());
					progressBar.setVisibility(View.GONE);
					loginB.setVisibility(View.GONE);
					logoutB.setVisibility(View.VISIBLE);
				} catch (Exception ex) {
				}
				break;
			case 0x124:
				photoI.setImageResource(R.drawable.not_logged_photo);
				nameT.setText(R.string.not_logged2);
				studentNumberT.setText("");
				identityT.setText("");
				academyT.setText("");
				logoutB.setVisibility(View.GONE);
				loginB.setVisibility(View.VISIBLE);
				break;
			case 0x125:
				progressBar.setVisibility(View.GONE);
				DialogBuilder.baseDialogNoReturn(UserInformationActivity.this, R.string.login_failed).show();
				break;
			case 0x126:
				try {
					photoI.setImageBitmap(getPhoto());
				} catch(Exception e){}
				break;
			case 0x127:
				progressBar.setVisibility(View.VISIBLE);
				progressBar.setProgress(50);
				progressBar.setSecondaryProgress(0);
				break;
			case 0x128:
				progressBar.setProgress(60);
				progressBar.setSecondaryProgress(50);
				break;
			case 0x129:
				progressBar.incrementProgressBy(10);
				progressBar.incrementSecondaryProgressBy(10);
				break;
			case 0x130:
				Toast.makeText(UserInformationActivity.this, "个人信息刷新完毕", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_information_activity);

		privateSP = getSharedPreferences(SHARENAME, Context.MODE_PRIVATE);
		defaultSP = PreferenceManager.getDefaultSharedPreferences(this);

		photoI = (ImageView) findViewById(R.id.photo);
		nameT = (TextView) findViewById(R.id.name);
		studentNumberT = (TextView) findViewById(R.id.student_number);
		identityT = (TextView) findViewById(R.id.identity);
		academyT = (TextView) findViewById(R.id.academy);

		loginB = (Button) findViewById(R.id.login_button);
		logoutB = (Button) findViewById(R.id.logout_button);
		progressBar = (ProgressBar) findViewById(R.id.progress);

		View view = getLayoutInflater().inflate(R.layout.login_dialog, null);
		usernameE = (EditText) view.findViewById(R.id.student_number);
		passwordE = (EditText) view.findViewById(R.id.password);
		rememberCB = (CheckBox) view.findViewById(R.id.remenber_check);
		autoLoginCB = (CheckBox) view.findViewById(R.id.auto_login_check);

		rememberCB.setChecked(privateSP.getBoolean("remenber", false));
		rememberCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (!arg1)
					autoLoginCB.setChecked(false);
			}
		});
		autoLoginCB.setChecked(privateSP.getBoolean("auto_login", false));
		autoLoginCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					rememberCB.setChecked(isChecked);
			}
		});
		if (rememberCB.isChecked()) {
			usernameE.setText(privateSP.getString("name", ""));
			passwordE.setText(privateSP.getString("password", ""));
		}

		dialog = new AlertDialog.Builder(this).setTitle(R.string.login).setView(view)
				.setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						new Thread() {
							@Override
							public void run() {
								try {
									handler.sendEmptyMessage(0x127);
									api.Login(usernameE.getText().toString(), passwordE.getText().toString());
									Editor editor = privateSP.edit();
									if (rememberCB.isChecked()) {
										editor.putString("name", usernameE.getText().toString());
										editor.putString("password", passwordE.getText().toString());
									}
									editor.putBoolean("remenber", rememberCB.isChecked());
									editor.putBoolean("auto_login", autoLoginCB.isChecked());
									editor.commit();
									handler.sendEmptyMessage(0x128);
								} catch (Exception e) {
									handler.sendEmptyMessage(0x125);
								}
								if (api.isLogged()) {
									while (true) {
										try {
											information = api.getInformation();
											break;
										} catch (Exception e) {
											handler.sendEmptyMessage(0x129);
										}
									}
									handler.sendEmptyMessage(0x123);
									if (defaultSP.getBoolean("display_photo", true))
										try {
											getPhoto();
											handler.sendEmptyMessage(0x126);
										} catch (Exception e) {
										}
								}
							};
						}.start();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				}).create();
		usernameE.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}
		});

		if (api.isLogged()) {
			handler.sendEmptyMessage(0x127);
			handler.sendEmptyMessage(0x128);
			new Thread() {
				public void run() {
					while(true){
						try {
							information = api.getInformation();
							break;
						} catch (Exception e) {
							handler.sendEmptyMessage(0x129);
						}
					}
					handler.sendEmptyMessage(0x123);
					if (defaultSP.getBoolean("display_photo", true)){
						try {
							getPhoto();
							handler.sendEmptyMessage(0x126);
						} catch(Exception e){}
					}
				};
			}.start();
		}
	}

	public void login(View view) {
		dialog.show();
		usernameE.requestFocus();
	}

	public void logout(View view) {
		handler.sendEmptyMessage(0x124);
		api.logoff();
	}

	private static Lock lock = new ReentrantLock();
	private static String currentStuNum;
	private static Bitmap imageBm;
	public static void deletePhoto() {
		lock.lock();
		File file;
		try { 
			file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + PATH + "cache"
					+ File.separator + API.getInstance().getStudentNumber() + ".jpg");
			if (file.exists()) {
				file.delete();
			}
		} catch (NotLoggedException e) {}
		finally {
			lock.unlock();
		}
	}
	public static Bitmap getPhoto() throws MalformedURLException, IOException {
		try {
			if (currentStuNum!=null&&currentStuNum.equals(api.getStudentNumber())&&imageBm!=null)
				return imageBm;
		} catch (Exception e1) {
		}
		lock.lock();
		try {
			try {
				currentStuNum = api.getStudentNumber();
			} catch (NotLoggedException e1) {}
			boolean externalStorageExist = Environment.getExternalStorageState()
					.equals(android.os.Environment.MEDIA_MOUNTED);
			File file = null;
			try {
				file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + PATH + "cache"
						+ File.separator + API.getInstance().getStudentNumber() + ".jpg");
			} catch (Exception e) {
			}
			if (file.exists()) {
				imageBm = BitmapFactory.decodeFile(file.getAbsolutePath());
			} 
			if (imageBm != null)
				return imageBm;
			else
				file.delete();
			byte[] imgaeB = information.getPhoto();
			imageBm = BitmapFactory.decodeByteArray(imgaeB, 0, imgaeB.length);
			if (imageBm == null) {
				throw new IOException("图片下载失败！");
			}
			if (externalStorageExist) {
				FileOutputStream out = null;
				try {
					new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + PATH + "cache"
							+ File.separator).mkdirs();
					out = new FileOutputStream(file);
					imageBm.compress(Bitmap.CompressFormat.JPEG, 90, out);
					out.flush();
				} catch (Exception e) {
				} finally {
					if (out != null)
						try {
							out.close();
						} catch (Exception e) {
						}
				}
			}
		} finally{
			lock.unlock();
		}
		return imageBm;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0x123, 0, "刷新个人信息");
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0x123) {
			new Thread(){
				public void run() {
					if (!api.isLogged())
						return;
					api.clear();
					handler.sendEmptyMessage(0x127);
					handler.sendEmptyMessage(0x128);
					while(true)
						try {
							information = api.getInformation();
							handler.sendEmptyMessage(0x123);
							handler.sendEmptyMessage(0x130);
							break;
						} catch (Exception e) {
							handler.sendEmptyMessage(0x129);
						}
					if (defaultSP.getBoolean("display_photo", true))
						try {
							getPhoto();
							handler.sendEmptyMessage(0x126);
						} catch (Exception e) {}
				};
			}.start();
		}
		return true;
	}
}