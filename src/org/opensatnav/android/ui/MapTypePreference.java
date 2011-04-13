package org.opensatnav.android.ui;

import org.andnav.osm.views.util.OpenStreetMapRendererInfo;
import org.opensatnav.android.R;

import android.content.Context;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class MapTypePreference extends ListPreference {
	private CharSequence[] entries;
	private String[] entryValues;
	private Resources r;

	public MapTypePreference(Context context, AttributeSet attrs) {
		super(context);
		r = context.getResources();
		entries = new String[OpenStreetMapRendererInfo.values().length];
		entryValues = new String[OpenStreetMapRendererInfo.values().length];
		for (int i = 0; i < OpenStreetMapRendererInfo.values().length; i++) {
			entries[i] = r
					.getText(OpenStreetMapRendererInfo.values()[i].DESCRIPTION);
			entryValues[i] = new String(
					OpenStreetMapRendererInfo.values()[i].PREFNAME);
		}
		setKey("map_style");
		setTitle(R.string.prefs_map_style);
		setEntries(entries);
		setEntryValues(entryValues);
		setDefaultValue(OpenStreetMapRendererInfo.getDefault().PREFNAME);
	}
}
