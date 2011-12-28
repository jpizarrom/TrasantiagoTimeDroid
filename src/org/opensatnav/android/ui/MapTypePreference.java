package org.opensatnav.android.ui;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import cl.droid.transantiago.R;

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
		entries = new String[TileSourceFactory.getTileSources().size()];
		entryValues = new String[TileSourceFactory.getTileSources().size()];
		for (int i = 0; i < TileSourceFactory.getTileSources().size(); i++) {
			entries[i] = new String(
					TileSourceFactory.getTileSources().get(i).name()); 
//				r
//					.getText(OpenStreetMapRendererInfo.values()[i].DESCRIPTION);
			entryValues[i] = new String(
					TileSourceFactory.getTileSources().get(i).name());
		}
		setKey("map_style");
		setTitle(R.string.prefs_map_style);
		setEntries(entries);
		setEntryValues(entryValues);
		setDefaultValue(TileSourceFactory.DEFAULT_TILE_SOURCE.name());
	}
}
