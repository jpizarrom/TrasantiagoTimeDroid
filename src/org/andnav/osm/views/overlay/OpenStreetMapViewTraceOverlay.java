/* 
This file is part of OpenSatNav.


    OpenSatNav is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenSatNav is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenSatNav.  If not, see <http://www.gnu.org/licenses/>.
 */
//Some code from MyTracks
/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.andnav.osm.views.overlay;

import java.io.Serializable;
import java.util.ArrayList;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.TypeConverter;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.opensatnav.android.OpenSatNavConstants;
import org.opensatnav.android.contribute.content.Track;
import org.opensatnav.android.contribute.content.Waypoint;
import org.opensatnav.android.contribute.util.MyTracksUtils;
import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

/**
 * 
 * @author Kieran Fleming
 * @author Stephen Brown
 */
public class OpenStreetMapViewTraceOverlay extends OpenStreetMapViewOverlay implements Serializable {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	/**
	 * 
	 */
	protected Paint mPaint = new Paint();
	protected Paint wayPointPaint = new Paint();
	private Track selectedTrack;
	private final int markerWidth, markerHeight;
	private Location mLocation;
	private final Drawable waypointMarker;
	private final ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
	private static final String TAG = "OpenSatNav.TraceOverlay";
	public void setSelectedTrack(Track track) {
		selectedTrack = track;
	}

	public Track getSelectedTrack() {
		return selectedTrack;
	}

	public void addWaypoint(Waypoint wpt) {
		waypoints.add(wpt);
	}

	public void clearWaypoints() {
		waypoints.clear();
	}

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenStreetMapViewTraceOverlay(final Context ctx) {
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(4);
		mPaint.setColor(Color.RED);
		mPaint.setAlpha(100);
		mPaint.setAntiAlias(true);
		wayPointPaint.setAlpha(120);
		
		waypointMarker = ctx.getResources().getDrawable(R.drawable.star_on);
		markerWidth = waypointMarker.getIntrinsicWidth();
	    markerHeight = waypointMarker.getIntrinsicHeight();
		waypointMarker.setBounds(0, 0, markerWidth, markerHeight);

	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setLocation(final Location mp) {
		this.mLocation = mp;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {
		return;
	}

	@Override
	public void onDraw(final Canvas c, final OpenStreetMapView osmv) {
		if (selectedTrack != null) {
			Log.d(OpenSatNavConstants.LOG_TAG, "Drawing " + selectedTrack.getLocations().size() + " points");
			drawTrack(c, osmv, selectedTrack, true);
			
			// Draw the waypoints:
		    ArrayList<Waypoint> currentWaypoints = waypoints;
		    for (int i = 1; i < currentWaypoints.size(); i++) {
		    	
		      Location loc = currentWaypoints.get(i).getLocation();
		      if (loc == null) {
		        continue;
		      }
		      GeoPoint geoPoint = MyTracksUtils.getGeoPoint(loc);
		      Point pt = new Point();
		      osmv.getProjection().toPixels(geoPoint, pt);
		      c.save();
		      c.translate(pt.x - (markerWidth / 2) + 3, pt.y - (markerHeight));
		      waypointMarker.draw(c);
		      c.restore();
		    }
		}
	}
	
	public void drawTrack(Canvas canvas, OpenStreetMapView osmv,
			Track track, boolean selected) {

		if (track == null) {
			return;
		}
		ArrayList<Location> points = track.getLocations();
		/*if (points.size() < 2) {
			return;
		}

		// Get the current viewing window:
		int w = (int) mapView.getLongitudeSpan();
		int h = (int) mapView.getLatitudeSpan();
		int cx = mapView.getMapCenter().getLongitudeE6();
		int cy = mapView.getMapCenter().getLatitudeE6();
		Rect rect = new Rect(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2);

		Point pt = new Point();
		GeoPoint geoPoint;
		Location loc;
		Location lastValidLocation = null;
		Path path;
		boolean wasInside;

		// Do as much allocation and preparation outside the loop over track
		// points:
		wasInside = false;
		int locLon = 0, locLat = 0, minLon, maxLon, minLat, maxLat;
		int lastLocLon = (int) (points.get(0).getLongitude() * 1E6);
		int lastLocLat = (int) (points.get(0).getLatitude() * 1E6);

		// Line decimation for dummies:
		// Skip di additional points, where di depends on zoom level:
		int di = 0;
		int dl = 17 - mapView.getZoomLevel();
		if (dl > 0) {
			di += (dl * dl);
		}

		// Loop over track points:
		path = new Path();
		for (int i = 1; i < points.size(); i++) {

			loc = points.get(i);
			if (loc == null) {
				continue;
			}
			locLon = (int) (loc.getLongitude() * 1E6);
			locLat = (int) (loc.getLatitude() * 1E6);

			// Skip to every n-th point (depends on zoom level, see above):
			for (int j = 0; j < di && i < points.size() - 1; j++) {
				// TODO Check the thread synchronization.
				// There is no reason that points.get(i + 1) should be null but
				// users
				// have reported it causing null pointer exceptions.
				if ((locLat > 90E6) || (lastLocLat > 90E6)
						|| (points.get(i + 1) == null)
						|| (points.get(i + 1).getLatitude() > 90)) {
					break;
				}
				i++;
				loc = points.get(i);
				locLon = (int) (loc.getLongitude() * 1E6);
				locLat = (int) (loc.getLatitude() * 1E6);
			}

			// Draw a line segment if it's inside the viewing window:
			if (locLat < 90E6 && lastLocLat < 90E6) {
				lastValidLocation = loc;
				minLon = Math.min(locLon, lastLocLon);
				maxLon = Math.max(locLon, lastLocLon);
				minLat = Math.min(locLat, lastLocLat);
				maxLat = Math.max(locLat, lastLocLat);
				if (rect.intersects(minLon, minLat, maxLon, maxLat)) {
					if (!wasInside) {
						geoPoint = new GeoPoint(lastLocLat, lastLocLon);
						mapView.getProjection().toPixels(geoPoint, pt);
						path.moveTo(pt.x, pt.y);
					}
					geoPoint = new GeoPoint(locLat, locLon);
					mapView.getProjection().toPixels(geoPoint, pt);
					path.lineTo(pt.x, pt.y);
					wasInside = rect.contains(locLon, locLat);
				}
			} else {
				wasInside = false;
			}
			lastLocLon = locLon;
			lastLocLat = locLat;
		}*/
		
		//canvas.drawPath(path, mPaint);
		
		ArrayList<Location> locations = track.getLocations();
		ArrayList<GeoPoint> pointRoute = new ArrayList<GeoPoint>();
		ArrayList<Point> pixelRoute = new ArrayList<Point>();
		Path routePath = new Path();
		for (int i = 0; i < locations.size(); i++) {
			GeoPoint nextPoint = TypeConverter.locationToGeoPoint(locations.get(i));
			nextPoint.setCoordsE6(nextPoint.getLatitudeE6(), nextPoint.getLongitudeE6());
			pointRoute.add(nextPoint);
		}
		Point firstPixelPoint = null, oldFirstPoint = null, lastPixelPoint = null, oldLastPixelPoint = null;
		final OpenStreetMapViewProjection pj = osmv.getProjection();
		if (pointRoute.size() != 0) {
			if (firstPixelPoint != null)
				oldFirstPoint = firstPixelPoint;
			if (lastPixelPoint != null)
				oldLastPixelPoint = lastPixelPoint;
			// this is optimisation so the computation from lat/long to pixel
			// coords
			// is only done if we need to, as this is expensive
			// right now this optimises map panning, but can optimise for zoom
			// as well

			// get the points
			firstPixelPoint = pj.toPixels(pointRoute.get(0), null);
			lastPixelPoint = pj.toPixels(pointRoute.get(pointRoute.size() - 1), null);

			// they panned, don't recompute from lat/long, just move the pixel
			// route the appropriate amount
			if ((oldFirstPoint != null)
					&& (oldFirstPoint.x - firstPixelPoint.x) == (oldLastPixelPoint.x - lastPixelPoint.x)
					&& (oldFirstPoint.y - firstPixelPoint.y) == (oldLastPixelPoint.y - lastPixelPoint.y)) {
				for (int i = 0; i < pixelRoute.size(); i++) {
					pixelRoute.set(i, new Point(pixelRoute.get(i).x - (oldFirstPoint.x - firstPixelPoint.x),
							pixelRoute.get(i).y - (oldFirstPoint.y - firstPixelPoint.y)));
				}
			}
			// either there is no old point, ergo this is the first time we are
			// here,
			// or they have zoomed
			// if this is false it means the map is still and we don't need to
			// do anything
			else if ((oldLastPixelPoint == null) || (oldLastPixelPoint.x != lastPixelPoint.x)
					|| (oldLastPixelPoint.y != lastPixelPoint.y)) {
				if (pixelRoute != null)
					pixelRoute.clear();
				for (int i = 0; i < pointRoute.size(); i++) {
					// Point nextScreenCoords = new Point();
					pixelRoute.add(pj.toPixels(pointRoute.get(i), null));
				}
			}

			// draw the pixel route
			routePath.rewind();
			for (int i = 0; i < pixelRoute.size(); i++) {
				Point current = pixelRoute.get(i);
				if (i == 0)
					routePath.moveTo(current.x, current.y);
				else
					routePath.lineTo(current.x, current.y);
			}
			canvas.drawPath(routePath, this.mPaint);
		}
		
		
		/*
		 * // Draw the "End" marker: Location currentLastValidLocation =
		 * lastValidLocation; if (showEndMarker && currentLastValidLocation !=
		 * null) { canvas.save(); geoPoint =
		 * MyTracksUtils.getGeoPoint(currentLastValidLocation);
		 * mapView.getProjection().toPixels(geoPoint, pt); canvas.translate(pt.x
		 * - (markerWidth / 2), pt.y - markerHeight); endMarker.draw(canvas);
		 * canvas.restore(); }
		 * 
		 * // Draw the "Start" marker: for (int i = 0; i < points.size(); i++) {
		 * loc = points.get(i); if (loc.getLatitude() < 90) { locLon = (int)
		 * (loc.getLongitude() * 1E6); locLat = (int) (loc.getLatitude() * 1E6);
		 * geoPoint = new GeoPoint(locLat, locLon);
		 * mapView.getProjection().toPixels(geoPoint, pt); canvas.save();
		 * canvas.translate(pt.x - (markerWidth / 2), pt.y - markerHeight);
		 * startMarker.draw(canvas); canvas.restore(); break; } }
		 */
	}
	
	

}
