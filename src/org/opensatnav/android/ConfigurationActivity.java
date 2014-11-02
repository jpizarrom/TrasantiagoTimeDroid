package org.opensatnav.android;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import cl.droid.transantiago.R;
import cl.droid.utils.PreferenceHelper;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ConfigurationActivity extends PreferenceActivity {
	private Context mContext;
	private PreferenceHelper mPreferenceHelper;
	
	private GoogleAnalyticsTracker tracker;
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = getBaseContext();
		mPreferenceHelper = new PreferenceHelper(mContext);
		if (mPreferenceHelper.isSendStatsEnabled()) {
		    tracker = GoogleAnalyticsTracker.getInstance();
		    tracker.startNewSession("UA-29423878-1", this);
		    tracker.trackPageView("/ConfigurationActivity");
	        tracker.dispatch();
		}
		
		addPreferencesFromResource(R.xml.prefs);
	}
}
