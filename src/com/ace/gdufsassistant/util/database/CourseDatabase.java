package com.ace.gdufsassistant.util.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import entity.Course;
/**
 * 课程表数据库调用：
 * 1.创建对象即创建数据库
 * 2.add()方法插入课程表
 * 3.getAllCourses()返回课程表信息ArrayList<C> []
 * 
 * isEmpty()方法可用于判断课程信息是否已存入，是则从数据库获取，否则爬取
 * @author Administrator
 *
 */
public class CourseDatabase {
	private CourseDatabaseHelper helper;
	public CourseDatabase(Context context)	//数据库名称name
	{
		helper=new CourseDatabaseHelper(context);
	} 
	
	public long add(ArrayList<Course> []list)	//传入Arraylist[]类型课程插入数据库，返回-1失败
	{
		clear();								//清空原有数据表，避免数据重复
		SQLiteDatabase db=helper.getWritableDatabase();
		int dayNum=0;
		long id=-1;
		for(ArrayList<Course> courses:list)
		{
			dayNum++;
			for(Course c:courses)
			{
				ContentValues values =new ContentValues();
				values.put("day", dayNum);
				values.put("courseName", c.getCourseName());
				values.put("place", c.getPlace());
				values.put("teacherName", c.getTeacherName());
				values.put("startTime", c.getStartTime());
				values.put("numb", c.getNumb());
				values.put("memorandum", "");
				values.put("teacherPhone", "");
				values.put("teacherEmail", "");
				id=db.insert("Course", null, values);
			}
		}
		db.close();
		return id;
	}
	public void clear()
	{
		String SQL="delete from Course ";
		SQLiteDatabase db=helper.getWritableDatabase();
		db.execSQL(SQL);
		db.close();
		courses = null;
	}	
	private long add(Course c, int day) {
		SQLiteDatabase db=helper.getWritableDatabase();
		ContentValues values =new ContentValues();
		values.put("day", day);
		values.put("courseName", c.getCourseName());
		values.put("place", c.getPlace());
		values.put("teacherName", c.getTeacherName());
		values.put("startTime", c.getStartTime());
		values.put("numb", c.getNumb());
		values.put("memorandum", c.getMemorandum());
		values.put("teacherPhone", c.getTeacherPhone());
		values.put("teacherEmail", c.getTeacherEmail());
		long id=db.insert("Course", null, values);
		db.close();
		courses = null;
		return id;
	}
	public void delete(int day, Course c)
	{
		String SQL="delete from Course where day="+day+" and startTime="+c.getStartTime();
		SQLiteDatabase db=helper.getWritableDatabase();
		db.execSQL(SQL);
		db.close();
		courses = null;
	}
	private void updateReal(Course cOld, Course cNew, int day) {
		SQLiteDatabase db=helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("courseName", cNew.getCourseName());
		values.put("place", cNew.getPlace());
		values.put("teacherName", cNew.getTeacherName());
		values.put("startTime", cNew.getStartTime());
		values.put("numb", cNew.getNumb());
		values.put("memorandum", cNew.getMemorandum());
		values.put("teacherPhone", cNew.getTeacherPhone());
		values.put("teacherEmail", cNew.getTeacherEmail());
		db.update("Course", values,"day=?  AND startTime=?",new String[]{day+"", cOld.getStartTime()+""});
		db.close();
		courses = null;
	}
	public void update(Course cOld, Course cNew, int day) throws Exception	//改、新增老师电话与邮箱
	{
		boolean tag = false;
		boolean tag2 = false;
		ArrayList<Course>[] courses = getCourse();
		ArrayList<Course> dayCourse = courses[day-1];
		for (Course course : dayCourse) {
			if (course.getStartTime() == cOld.getStartTime()) { 
				tag2 = true;
				continue;
			}
			if (course.getStartTime() > cNew.getStartTime()) {
				if (cNew.getNumb() >= course.getStartTime() - cNew.getStartTime()) 
					if (cNew.getCourseName().equals(course.getCourseName())&&cNew.getTeacherName().equals(course.getTeacherName())){
						int numb1 = cNew.getStartTime()+cNew.getNumb();
						int numb2 = course.getStartTime()+course.getNumb();
						int numb = numb1>numb2?numb1:numb2;
						Course temp = new Course(course.getCourseName(), course.getTeacherName(), course.getPlace(), cNew.getStartTime(), numb - cNew.getStartTime(), course.getMemorandum(),
								course.getTeacherPhone(), course.getTeacherEmail());
						delete(day, cOld);
						updateReal(course, temp, day);
						tag = true;
						break;
					}
					else if (cNew.getNumb() > course.getStartTime() - cNew.getStartTime()) 
						throw new Exception("与课程"+course.getCourseName()+"有冲突，请先调整冲突课程！");
			}
			else if (cNew.getStartTime() > course.getStartTime()) 
				if (course.getNumb() >= cNew.getStartTime() - course.getStartTime()) 
					if (cNew.getCourseName().equals(course.getCourseName())&&cNew.getTeacherName().equals(course.getTeacherName())){
						int numb1 = cNew.getStartTime()+cNew.getNumb();
						int numb2 = course.getStartTime()+course.getNumb();
						int numb = numb1>numb2?numb1:numb2;
						Course temp = new Course(course.getCourseName(), course.getTeacherName(), course.getPlace(), course.getStartTime(), numb-course.getStartTime(), course.getMemorandum(),
								course.getTeacherPhone(), course.getTeacherEmail());
						delete(day, cOld);
						updateReal(course, temp, day);
						tag = true;
						break;
					}
					else if (course.getNumb() > cNew.getStartTime() - course.getStartTime()) 
						throw new Exception("与课程"+course.getCourseName()+"有冲突，请先调整冲突课程！");
		}
		if (tag) return;
		if (tag2)
			updateReal(cOld, cNew, day);
		else
			add(cNew, day);
	}
	private static ArrayList<Course>[] courses;
	public ArrayList<Course> []getCourse()
	{
		if (courses != null)
			return courses;
		courses=new ArrayList[7];
		for(int i=0;i<=6;i++)
		{
			ArrayList<Course> list=new ArrayList<Course>();
			list=getCourse(i+1);
			courses[i]=list;
		}
		return courses;
	}
	
	private ArrayList<Course> getCourse(int day)		//调用前判断isEmpty(),空会出错
	{
		SQLiteDatabase db=helper.getReadableDatabase();
		ArrayList<Course> courses=new ArrayList<Course>();
		Cursor cursor=db.rawQuery("select * from Course where day="+day, null);
		while(cursor.moveToNext())
		{
			String courseName=cursor.getString(cursor.getColumnIndex("courseName"));
			String place=cursor.getString(cursor.getColumnIndex("place"));
			String teacherName=cursor.getString(cursor.getColumnIndex("teacherName"));
			int startTime=cursor.getInt(cursor.getColumnIndex("startTime"));
			int numb=cursor.getInt(cursor.getColumnIndex("numb"));
			String memorandum=cursor.getString(cursor.getColumnIndex("memorandum"));
			String teacherPhone=cursor.getString(cursor.getColumnIndex("teacherPhone"));
		    String teacherEmail=cursor.getString(cursor.getColumnIndex("teacherEmail"));
			Course c=new Course(courseName, teacherName, place, startTime, numb, memorandum,teacherPhone,teacherEmail);
			courses.add(c);
		}
		db.close();
		return courses;
	}
	
	public boolean isEmpty()
	{
		SQLiteDatabase db=helper.getReadableDatabase();
		Cursor cursor=db.rawQuery("select * from Course", null);
		boolean result =cursor.moveToNext();
		db.close();
		return !result;
	}
}
