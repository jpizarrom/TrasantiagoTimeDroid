package org.opensatnav.android.ui;

import cl.droid.transantiago.R;

import android.content.Context;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class UnitSystemPreference extends ListPreference {
	private CharSequence[] entries;
	private CharSequence[] entryValues;
	private Resources r;

	public UnitSystemPreference(Context context, AttributeSet attrs) {
		super(context);
		r = context.getResources();
		entries = new String[2];
		entryValues = new CharSequence[2];
		
		entries[0]=r.getText(R.string.prefs_units_metric);
		entryValues[0]="metric";
		entries[1]=r.getText(R.string.prefs_units_imperial);
		entryValues[1]="imperial";
		
		setKey("unit_system");
		setTitle(R.string.prefs_units);
		setEntries(entries);
		setEntryValues(entryValues);
		
		String currentLocale = context.getResources().getConfiguration().locale.getCountry();
		if ((currentLocale.compareTo("GB") == 0) || (currentLocale.compareTo("US") == 0)) {
			setDefaultValue("imperial");
		}
		else
			setDefaultValue("metric");
	}

}
