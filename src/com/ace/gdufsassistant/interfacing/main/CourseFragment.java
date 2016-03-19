/**
 * 
 */
package com.ace.gdufsassistant.interfacing.main;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.interfacing.specific.module.CourseEditor;
import com.ace.gdufsassistant.util.Reload;
import com.ace.gdufsassistant.util.ReloadApplication;
import com.ace.gdufsassistant.util.builder.DialogBuilder;
import com.ace.gdufsassistant.util.builder.TermBuilder;
import com.ace.gdufsassistant.util.database.CourseDatabase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import entity.Course;
import main.API;

/**
 * @author wan
 *
 */
public class CourseFragment extends Fragment implements Reload {
	public final static int[] background = { R.drawable.course_item_bg_blue, R.drawable.course_item_bg_green,
			R.drawable.course_item_bg_red, R.drawable.course_item_bg_chocolate, R.drawable.course_item_bg_yellow };

	private Dialog dialog;
	private CourseDatabase db;
	private GridLayout courseGridLayout;
	private ArrayList<TextView> courseViewList = new ArrayList<TextView>();
	private int gridWidth;
	private int gridHeight;
	private Handler handler = new MyHandler(this) ;
	private static class MyHandler extends Handler{
		private final WeakReference<CourseFragment> mcf; 
		private MyHandler(CourseFragment cf){
			mcf = new WeakReference<CourseFragment>(cf);
		}
		@Override
		public void handleMessage(Message msg) {
			if (mcf==null)return;
			CourseFragment cf = mcf.get();
			switch (msg.what) {
			case 0x119:
				if (!cf.courseViewList.isEmpty())
					for (int i = cf.courseViewList.size() - 1; i >= 0; i--) {
						cf.courseGridLayout.removeView(cf.courseViewList.get(i));
						cf.courseViewList.remove(i);
					}
				break;
			case 0x120:
				cf.dialog.show();
				break;
			case 0x121:
				DialogBuilder.showErrorDialogNoReturn(cf.getActivity());
				break;
			case 0x123:
				if (cf.db.isEmpty()) {
					for (int week = 1; week <= 7; week++) {
						for (int startTime = 0; startTime < 15; startTime++) {
							TextView empty = getTextView(startTime, week, cf.getActivity());
							empty.setOnClickListener(new EditListener(new Course(startTime), week, cf.getActivity()));
							cf.courseViewList.add(empty);
							cf.courseGridLayout.addView(empty);
						}
					}
				} else {
					if (!cf.courseViewList.isEmpty())
						for (int i = cf.courseViewList.size() - 1; i >= 0; i--) {
							cf.courseGridLayout.removeView(cf.courseViewList.get(i));
							cf.courseViewList.remove(i);
						}
					int color = 0;
					ArrayList<Course>[] courseList = cf.db.getCourse();
					for (int week = 1; week <= 7; week++) {
						int startTime = 0, count;
						for (Course c : courseList[week - 1]) {
							count = c.getStartTime() - 1;
							for (int j = startTime; j < count; j++) {
								TextView empty = getTextView(j, week, cf.getActivity());
								empty.setOnClickListener(new EditListener(new Course(j+1), week, cf.getActivity()));
								cf.courseViewList.add(empty);
								cf.courseGridLayout.addView(empty);
							}

							String content = c.getCourseName().replaceAll("（.+）", "");
							if (c.getPlace()!=null&&!c.getPlace().equals(""))content += '\n' + c.getPlace();
							if (c.getTeacherName()!=null&&!c.getTeacherName().equals("")) content += '\n' + c.getTeacherName();
							if (!c.getMemorandum().equals(""))content+="\n【备忘】";
							TextView course = new TextView(cf.getActivity());
							course.setText(content);

							GridLayout.LayoutParams params = new GridLayout.LayoutParams();
							params.rowSpec = GridLayout.spec(c.getStartTime() - 1, c.getNumb());
							params.columnSpec = GridLayout.spec(week, 1);
							params.setGravity(Gravity.CENTER);
							course.setWidth(cf.gridWidth - 2);
							course.setHeight(cf.gridHeight * c.getNumb() - 2);
							course.setGravity(Gravity.CENTER);
							course.setBackgroundResource(background[color++ % 5]);
							course.setTextSize(12);
							course.setTextColor(Color.WHITE);
							course.getBackground().setAlpha(180);
							course.setOnClickListener(new EditListener(c, week, cf.getActivity()));
							startTime = c.getStartTime() + c.getNumb() - 1;

							cf.courseViewList.add(course);
							cf.courseGridLayout.addView(course, params);
						}
						for (int j = startTime; j < 15; j++) {
							TextView empty = getTextView(j, week, cf.getActivity());
							empty.setOnClickListener(new EditListener(new Course(j+1), week, cf.getActivity()));
							empty.setWidth(cf.gridWidth);
							cf.courseViewList.add(empty);
							cf.courseGridLayout.addView(empty);
						}
					}
				}
				reload = false;
				break;
			case 0x124:
				Toast.makeText(cf.getActivity(), "课程表加载完毕", Toast.LENGTH_SHORT).show();
				break;
			}
		};
	};
	private static boolean reload = false;
	@Override
	public void onResume() {
		super.onResume();
		if (reload)
			handler.sendEmptyMessage(0x123);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		db = new CourseDatabase(getActivity());
		View view = inflater.inflate(R.layout.course_fragment, container, false);

		TextView empty = (TextView) view.findViewById(R.id.first_empty);
		courseGridLayout = (GridLayout) view.findViewById(R.id.course_grid);

		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int firstWidth = dm.widthPixels * 3 / 31;
		gridWidth = dm.widthPixels * 4 / 31;
		gridHeight = dm.heightPixels / 14;
		if (gridHeight < gridWidth)
			gridHeight = gridWidth;
		empty.setWidth(firstWidth);

		TextView temp;
		for (int i = 1; i <= 15; i++) {
			temp = getTextView(i - 1, 0, getActivity());
			temp.setText(String.valueOf(i));
			temp.setBackgroundResource(R.drawable.course_table);
			temp.setWidth(firstWidth);
			temp.setHeight(gridHeight);
			temp.setGravity(Gravity.CENTER);
			courseGridLayout.addView(temp);
		}

		final TermBuilder tb = TermBuilder.getInstance();
		View courseDialogLayout = inflater.inflate(R.layout.course_loading_dialog, null);
		final Spinner yearSpinner = (Spinner) courseDialogLayout.findViewById(R.id.year);
		yearSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, tb.getYear()));
		final Spinner termSpinner = (Spinner) courseDialogLayout.findViewById(R.id.term);
		termSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, tb.getTerm()));
		dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.course_loading).setView(courseDialogLayout)
				.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						new Thread() {
							@Override
							public void run() {
								try {
									String year = tb.getYear()[yearSpinner.getSelectedItemPosition()];
									String term = tb.getTerm()[termSpinner.getSelectedItemPosition()];
									db.add(API.getInstance().getCourse(year, term));
									handler.sendEmptyMessage(0x123);
									handler.sendEmptyMessage(0x124);
								} catch (Exception e) {
									handler.sendEmptyMessage(0x121);
								}
							};
						}.start();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				}).create();

		handler.sendEmptyMessage(0x123);
		setHasOptionsMenu(true);
		
		ReloadApplication.getInstance("course").addReload(this);
		return view;
	}

	private static TextView getTextView(int i, int j, Activity activity) {
		TextView s = new TextView(activity);
		GridLayout.LayoutParams params = new GridLayout.LayoutParams();
		params.rowSpec = GridLayout.spec(i);
		params.columnSpec = GridLayout.spec(j);
		params.setGravity(Gravity.FILL);
		s.setLayoutParams(params);
		return s;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(0, 0x123, 0, "加载课表");
		menu.add(0, 0x125, 0, "刷新课表");
		menu.add(0, 0x124, 0, "清除课表");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0x123:
			handler.sendEmptyMessage(0x120);
			break;
		case 0x124:
			db.clear();
			handler.sendEmptyMessage(0x119);
			break;
		case 0x125:
			handler.sendEmptyMessage(0x123);
			handler.sendEmptyMessage(0x124);
			break;
		}
		return true;
	}

	private static class EditListener implements OnClickListener {
		private Course course;
		private int day;
		private Activity activity;

		public EditListener(Course course, int day, Activity activity) {
			this.course = course;
			this.day = day;
			this.activity = activity;
		}

		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(activity,
					CourseEditor.class);
			intent.putExtra("courseName", course.getCourseName());
			intent.putExtra("place", course.getPlace());
			intent.putExtra("startTime", course.getStartTime());
			intent.putExtra("numbs", course.getNumb());
			intent.putExtra("teacherName", course.getTeacherName());
			intent.putExtra("teacherEmail", course.getTeacherEmail());
			intent.putExtra("teacherNum", course.getTeacherPhone());
			intent.putExtra("memorandum", course.getMemorandum());
			intent.putExtra("day", day);
			activity.startActivity(intent);
		}
	}

	@Override
	public void reload() {
		reload = true;
	}
}
