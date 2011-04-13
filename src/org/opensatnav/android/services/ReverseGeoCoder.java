package org.opensatnav.android.services;

import org.andnav.osm.util.GeoPoint;

import android.content.Context;
import android.os.Bundle;

public interface ReverseGeoCoder {

	public abstract Bundle query(GeoPoint location, Context context);
}
