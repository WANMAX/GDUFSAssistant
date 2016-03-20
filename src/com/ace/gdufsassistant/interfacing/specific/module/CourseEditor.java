package com.ace.gdufsassistant.interfacing.specific.module;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.util.ExitApplication;
import com.ace.gdufsassistant.util.ReloadApplication;
import com.ace.gdufsassistant.util.builder.DialogBuilder;
import com.ace.gdufsassistant.util.database.CourseDatabase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import entity.Course;

public class CourseEditor extends Activity {
	private Intent intent;

	private EditText courseNameE;
	private EditText placeE;
	private EditText startTimeE;
	private EditText numbsE;
	private EditText teacherNameE;
	private EditText teacherNumE;
	private EditText teacherEmailE;
	private EditText memorandumE;
	private Button applyB;
	private Button deleteB;
	
	private int day;
	private Course cOld;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.course_editor_activity);

		intent = getIntent();
		String courseName = intent.getStringExtra("courseName");
		String place = intent.getStringExtra("place");
		int startTime = intent.getIntExtra("startTime", 0);
		int numbs = intent.getIntExtra("numbs", 0);
		String teacherName = intent.getStringExtra("teacherName");
		String teacherNum = intent.getStringExtra("teacherNum");
		String teacherEmail = intent.getStringExtra("teacherEmail");
		String memorandum = intent.getStringExtra("memorandum");
		day = intent.getIntExtra("day", 0);
		
		cOld = new Course(courseName, teacherName, place, startTime, numbs,
				memorandum, teacherNum, teacherEmail);

		courseNameE = (EditText) findViewById(R.id.courseName);
		placeE = (EditText) findViewById(R.id.place);
		startTimeE = (EditText) findViewById(R.id.startTime);
		numbsE = (EditText) findViewById(R.id.numbs);
		teacherNameE = (EditText) findViewById(R.id.teacherName);
		teacherNumE = (EditText) findViewById(R.id.teacherNumber);
		teacherEmailE = (EditText) findViewById(R.id.teacherEmail);
		memorandumE = (EditText) findViewById(R.id.memorandum);
		applyB = (Button) findViewById(R.id.apply);
		deleteB = (Button) findViewById(R.id.delete);
		
		courseNameE.setText(courseName);
		placeE.setText(place);
		startTimeE.setText(""+startTime);
		numbsE.setText(""+(startTime+numbs-1));
		teacherNameE.setText(teacherName);
		teacherNumE.setText(teacherNum);
		teacherEmailE.setText(teacherEmail);
		memorandumE.setText(memorandum);

		applyB.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View arg0) {
				CourseDatabase db = new CourseDatabase(CourseEditor.this);

				String courseName = courseNameE.getText().toString().trim();
				String teacherName = teacherNameE.getText().toString().trim();
				String place = placeE.getText().toString().trim();
				String memorandum = memorandumE.getText().toString().trim();
				String teacherNum = teacherNumE.getText().toString().trim();
				String teacherEmail = teacherEmailE.getText().toString()
						.trim();
				int startTime;
				int numbs;
				try {
					startTime = Integer.parseInt(startTimeE.getText().toString().trim());
					int endTime = Integer.parseInt(numbsE.getText().toString().trim());
					numbs = endTime + 1 - startTime;
					if (numbs < 0 || endTime > 15)
						throw new Exception();
				} catch(Exception e) {
					DialogBuilder.baseDialogNoReturn(CourseEditor.this, "起始节数或结束节数不规范！").show();;
					return;
				}
				
				Course c = new Course(courseName, teacherName, place, startTime, numbs,
						memorandum, teacherNum, teacherEmail);
				try {
					db.update(cOld, c, day);
				} catch(Exception e) {
					DialogBuilder.baseDialogNoReturn(CourseEditor.this, e.getMessage()).show();;
					return;
				}
				
				Toast.makeText(CourseEditor.this, "保存成功", Toast.LENGTH_SHORT).show();
				ReloadApplication.getInstance("course").reload();
				new Thread() {
					@Override
					public void run() {
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);            
						if(imm.isActive()&&getCurrentFocus()!=null&&getCurrentFocus().getWindowToken()!=null)
								imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
						new Instrumentation()
								.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
					}
				}.start();
			}
		});
		deleteB.setOnClickListener(new OnClickListener() { 
			Dialog dialog;
			@Override
			public void onClick(View arg0) {
				dialog = new AlertDialog.Builder(CourseEditor.this).setTitle("删除课程")
						.setMessage("您确定要删除该课程吗？")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int which) {
								new CourseDatabase(CourseEditor.this).delete(day, cOld);
								Toast.makeText(CourseEditor.this, "删除成功", Toast.LENGTH_SHORT).show();
								dialog.dismiss();
								ReloadApplication.getInstance("course").reload();
								new Thread() {
									@Override
									public void run() {
										InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);            
										if(imm.isActive()&&getCurrentFocus()!=null&&getCurrentFocus().getWindowToken()!=null)
												imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
										try {
											Thread.sleep(500);
										} catch (InterruptedException e) {
										}
										new Instrumentation()
												.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
									}
								}.start();
							}
				}).setNegativeButton("取消", null).create();
				dialog.show();
			}
		});
		ExitApplication.getInstance().addActivity(this);
	}

}
