package com.ace.gdufsassistant.util.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CourseDatabaseHelper extends SQLiteOpenHelper{

	public CourseDatabaseHelper(Context context) {
		super(context, "Course.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table Course(id integer primary key autoincrement,day integer(10),courseName varchar(20),place varchar(20),teacherName varchar(20),startTime integer(20),numb integer(20),memorandum varchar(20),teacherPhone varchar(15),teacherEmail varchar(20))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}
	
}
