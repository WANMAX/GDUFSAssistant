/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module;

import com.ace.gdufsassistant.R;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

/**
 * @author wan
 *
 */
public class CourseDesktop extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.course_desktop);
		
		RemoteViews temp = new RemoteViews(context.getPackageName(), R.layout.course_grid1);
		remoteViews.addView(R.id.course_grid, temp);
		
		ComponentName componentName = new ComponentName(context, CourseDesktop.class);
		appWidgetManager.updateAppWidget(componentName, remoteViews);
	}
}
