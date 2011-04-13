package org.opensatnav.android;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		Log.v(OpenSatNavConstants.LOG_TAG, "Called to update the app widget");
		// Perform this loop procedure for each App Widget that belongs to this provider
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];
			RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);
			//This is only really called very infrequently. TODO: Make it betters!

			
				
				updateViews.setTextViewText(R.id.distanceLeft, "No Current Route"); 
			
			appWidgetManager.updateAppWidget(appWidgetId, updateViews);
		}	
	}


} 
