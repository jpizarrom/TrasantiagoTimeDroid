package cl.droid.utils;

import java.util.Date;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {
	private static final String KEY_STATS_TIMESTAMP = "stats_timestamp";
	
	public static final String KEY_SEND_STATS = "sendUsageStats";
	public static final boolean KEY_SEND_STATS_DEFAULT_VALUE = true;
	
	public static final String KEY_LOAD_STOPS = "loadstops";
	public static final String KEY_LOAD_STOP = "loadstop";
	
	private final SharedPreferences preferences;
	private final Context context;

	public PreferenceHelper(final Context context) {
		this.context = context;
		this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	/**
     * Return a long representing the last stats send date
     */
	public long getStatsTimestamp() {
		return preferences.getLong(KEY_STATS_TIMESTAMP, 0);
	}
    /**
     * Set a long representing the last update
     */	
	public void setStatsTimestamp() {
		Date now = new Date();
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(KEY_STATS_TIMESTAMP, now.getTime());
		editor.commit();
	}
	
	public boolean isSendStatsEnabled()	{
		return preferences.getBoolean(KEY_SEND_STATS, KEY_SEND_STATS_DEFAULT_VALUE);
	}
	
	public int getLoadstops() {
		return preferences.getInt(KEY_LOAD_STOPS, 0);
	}
	public void setLoadstops() {
		int cur = preferences.getInt(KEY_LOAD_STOPS, 0);
		cur++;
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(KEY_LOAD_STOPS, cur);
		editor.commit();
	}
	public void resetLoadstops() {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(KEY_LOAD_STOPS, 0);
		editor.commit();
	}
	public int getLoadstop() {
		return preferences.getInt(KEY_LOAD_STOP, 0);
	}
	public void setLoadstop() {
		int cur = preferences.getInt(KEY_LOAD_STOP, 0);
		cur++;
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(KEY_LOAD_STOP, cur);
		editor.commit();
	}
	public void resetLoadstop() {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(KEY_LOAD_STOP, 0);
		editor.commit();
	}
}
