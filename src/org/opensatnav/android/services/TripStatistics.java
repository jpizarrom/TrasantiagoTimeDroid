package org.opensatnav.android.services;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.andnav.osm.views.util.Util;
import org.opensatnav.android.TripStatisticsController;
import org.opensatnav.android.util.FormatHelper;
import android.content.Context;
import android.location.Location;
import android.util.Log;

public class TripStatistics {

	private int pointsReceivedCount;
	private long tripStartTimeMilisec;
	private long lastPointTimeMilisec;
	private float tripDistanceMeters;
	private Context ctx;
	
	// Previous Point
	private Location lastLocatPoint = null;
	
	private Collection<TripStatisticsListener> listeners = null;
	
	private DecimalFormat formatter;

	public TripStatistics(Context ctx) {
		initializeStats();
		this.ctx = ctx;
	}
	
	public TripStatistics(TripStatisticsListener firstListener,Context ctx) {
		this(ctx);
		
		
		// Tried to do this from where the service is started, but 
		// onCreate() doesn't run until after some time later. 
		addTripStatsListener(firstListener);
	}
	
	private void initializeStats() {
		pointsReceivedCount = 0;
		tripStartTimeMilisec = -1;
		lastPointTimeMilisec = -1;
		tripDistanceMeters = 0;
		formatter = new DecimalFormat();
	}
	
	public void addTripStatsListener(TripStatisticsListener listener) {
		if( listeners == null ) {
			listeners = new ArrayList<TripStatisticsListener>();
		}
		listeners.add(listener);
		
		listener.tripStatisticsChanged(this);  
	}
	
	public void removeTripStatsListener(TripStatisticsListener listener) {
		if( listeners.contains(listener) ) {
			listeners.remove(listener);
		}
	}

	public void removeAllTripStatsListeners() {
		listeners = null;
	}

	public void addNewLocationPoint(Location newLocation, Location oldLocation) {
		pointsReceivedCount++;
		if( tripStartTimeMilisec == -1 ) {
			tripStartTimeMilisec = newLocation.getTime();
		}
		lastPointTimeMilisec = newLocation.getTime();

		if(lastLocatPoint != null && Util.isValidSpeed(lastPointTimeMilisec, newLocation.getSpeed(), oldLocation.getTime(), oldLocation.getSpeed())) {
			tripDistanceMeters += newLocation.distanceTo(lastLocatPoint);
		}
		
		lastLocatPoint = newLocation;
		
		if( pointsReceivedCount > 1 ) {
			callAllListeners(); // Can't deliver with just one point.
		}
	}
	
	private void callAllListeners() {
		if( listeners != null ) {
			for( TripStatisticsListener l : listeners) {
				l.tripStatisticsChanged(this);
			}
		} else {
			Log.v("TripStatistics", "No listeners for TripStats");
		}
	}

	/** Returns the aver trip speed in m/s */
	public float getAverageTripSpeed() {
		if( pointsReceivedCount == 0 ) {
			return 0f;
		} else {
			return getTripDistance() / (getTripTime() / 1000);
		}
	}

	/** Return total trip time in milisec */
	public long getTripTime() {
		if( pointsReceivedCount == 0 ) {
			return 0;
		} else {
			return lastPointTimeMilisec - tripStartTimeMilisec;
		}
	}

	/** Returns total trip distance in meters */
	public float getTripDistance() {
		if( pointsReceivedCount == 0 ) {
			return 0;
		} else {
			return tripDistanceMeters;
		}
	}

	/* Note: the instantenous speed is newPoint.getSpeed()  */
	public float getInstantSpeed() {
		if( pointsReceivedCount == 0 ) {
			return 0;
		} else {
			return lastLocatPoint.getSpeed();
		}
	}

	public void resetStatistics() {
		initializeStats();
		callAllListeners();
	}

	public String getAverageTripSpeedString() {
		return getSpeedString(getAverageTripSpeed());
	}

	public String getInstantSpeedString() {
		return getSpeedString(getInstantSpeed());
	}

	private String getSpeedString(float speed) {
		String speedString = "";
		speedString = 
			new FormatHelper(ctx).formatSpeed(speed);

		return speedString;
	}

	public String getTripTimeString() {
		int tripTimeSec = Math.round(getTripTime() / 1000f);
		formatter.applyLocalizedPattern("#0");
		int hr = tripTimeSec / 3600;
		String hrStr = formatter.format(hr);
		
		formatter.applyLocalizedPattern("00");
		int min = (tripTimeSec - hr * 3600) / 60;
		String minStr = formatter.format(min);
		
		int sec = tripTimeSec % 60;
		String secStr = formatter.format(sec);
		return hrStr + ":" + minStr + ":" + secStr;
	}

	public String getTripDistanceString() {
		String distance = "";
		distance = 
			new FormatHelper(ctx).formatDistance((int)getTripDistance());

		return distance;
	}

	public static class TripStatisticsStrings {
		public String averSpeed;
		public String instSpeed;
		public String tripDistance;
		public String tripDuration;
	}
}
