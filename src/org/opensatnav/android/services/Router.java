package org.opensatnav.android.services;

import org.andnav.osm.util.GeoPoint;
import org.opensatnav.android.services.routing.Route;

import android.content.Context;

public interface Router {

	public static final String CAR = "motorcar";
	public static final String BICYCLE = "bicycle";
	public static final String WALKING = "foot";

	/**
	 * 
	 * @param from
	 *            where the user is
	 * @param to
	 *            where the user wants to go
	 * @param vehicle
	 *            one of the vehicle constants (CAR, BICYCLE or WALKING)
	 * @param context
	 *            reference to caller (used to get the name and version
	 *            number of the program to add the user agent in network ops)
	 * @return a route which may contain geopoints and optionally route instructions)
	 */
	public abstract Route getRoute(GeoPoint from, GeoPoint to, String vehicle, Context context);
	
}