package org.opensatnav.android.services;

import org.andnav.osm.util.GeoPoint;

import android.content.Context;
import android.os.Bundle;

public interface GeoCoder {
	static final int IN_AREA=1;
	static final int FROM_POINT=2;
	/**
	 * 
	 * @param query
	 *            where the user wants to go
	 * @param from
	 * 			  the users' location
	 * @param maxResults
	 *            max results required
	 * @param context
	 *            reference to caller (used to get the name and version number
	 *            of the program to add the user agent in network ops)
	 * @param mode
	 * 			  IN_AREA to search for matches within a (large) radius and order by best match
	 *            FROM_POINT to search from point and return matches in the order of distance away
	 *            (ideal for POI search as there are no other ways of ordering results)
	 *            
	 * @return a Bundle containing parallel arrays named names, latitudes,
	 *         longitudes and info where <i>name</i> is the name of the place,
	 *         where <i>latitudes</i> and <i>longitudes</i> are in integer (E6)
	 *         format and <i>info</i> can be any text that will help the user
	 *         choose the place they want
	 */
	public abstract Bundle query(String query, GeoPoint from, int mode, int maxResults, Context context);

}