package org.opensatnav.android.services.routing;

import java.util.ArrayList;

import org.andnav.osm.util.GeoPoint;

public class Route {
	public ArrayList<GeoPoint> routeGPX;
	public Double totalDistance;
	public String startName;
	public String endName;
	public Double totalTime;
	public GeoPoint from;
	public GeoPoint to;
	public String vehicle;
	
	public ArrayList<RouteInstruction> routeInstructions;
	
	public Route() {
		routeGPX = new ArrayList<GeoPoint>();
		routeInstructions = new ArrayList<RouteInstruction>();
		firstMaxSet = false;
	}
	
	public Boolean firstMaxSet;
	public int maxLngE6;
	public int minLngE6;
	public int maxLatE6;
	public int minLatE6;
	
}
