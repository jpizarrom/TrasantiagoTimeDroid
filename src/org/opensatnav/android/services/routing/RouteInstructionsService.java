package org.opensatnav.android.services.routing;

import java.text.DecimalFormat;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.TypeConverter;
import org.andnav.osm.views.OpenStreetMapView;
import org.opensatnav.android.OpenSatNavConstants;
import cl.droid.transantiago.R;
import org.opensatnav.android.WidgetProvider;
import org.opensatnav.android.services.CloudmadeRouter;
import org.opensatnav.android.services.LocationHandler;
import org.opensatnav.android.services.NoLocationProvidersException;
import org.opensatnav.android.services.Router;
import org.opensatnav.android.services.routing.overlay.RouteOverlay;
import org.opensatnav.android.services.routing.util.DistancePoint;
import org.opensatnav.android.util.FormatHelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class RouteInstructionsService extends Service implements LocationListener{
	
	public Route route;
	public RouteOverlay routeOverlay;
	public int currentInstructionID;
	public RouteInstruction currentInstruction;
	public RouteInstruction nextRouteInstruction;
	public int currentGPXID;
	public static LocationHandler mLocationHandler;
	public Boolean currentlyRouting = false;
	public boolean gettingRoute = false;
	public Time latestRouteReceived;
	public Context context;
	
	public Location mostRecentLocation;
	private NotificationManager mNM;
	//Service related stuff:
	public class LocalBinder extends Binder {
		RouteInstructionsService getService() {
            return RouteInstructionsService.this;
        }
    }
	
	@Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        super.onCreate();
        // Display a notification about us starting.  We put an icon in the status bar.
        
    }
	
	
	
	@Override
	public synchronized void onDestroy() {
		Log.v(OpenSatNavConstants.LOG_TAG, "RIC onDestroy()");
		
		stopRouting();
		super.onDestroy();
	}
	
	
	
	private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = OpenSatNavConstants.LOG_TAG;

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        Intent newIntent = new Intent(context, RouteInstructionsService.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                newIntent, 0);
        
        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(context, getText(R.string.app_desc),
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.app_name, notification);
    }

   
	
	
	//If the next instruction is less than ttsMinimumTimeToNextInstruction it will mention it, ie "Turn left in 200m and then turn right after 700meters"
	protected final int ttsMinimumTimeToNextInstruction = 100; //seconds
	public OpenStreetMapView mOsmv;
	private static final float MAXIMUM_DISTANCE_FROM_ROUTE = 5; //metres
	public RouteInstructionsService() {
		
	}

	public RouteInstructionsService(Context context, RouteOverlay routeOverlay, OpenStreetMapView mOsmv) {
		Log.v(OpenSatNavConstants.LOG_TAG, "Creating RIC");
		mLocationHandler = new LocationHandler((LocationManager) context.getSystemService(Context.LOCATION_SERVICE), this, context);

		this.mOsmv = mOsmv;
		this.context = context;
		this.routeOverlay = routeOverlay;
		latestRouteReceived = new Time();
		latestRouteReceived.set(0, 0, 0, 0, 0, 1970);
		
		
	}

	public void startRoute(Route route) throws NoLocationProvidersException {
		this.route = route;
		//showNotification();
		
		currentInstructionID = 0;
		updateCurrentInstruction(0);
		currentGPXID = 0;
		currentlyRouting = true;
		this.routeOverlay.showRoute(route);
		//this.routeOverlay.routeOverlay.setNextWayPoint(route.routeInstructions.get(currentInstructionID).getGeoPoint());

		mLocationHandler.start();
		if (route.routeInstructions.size() -1 > currentInstructionID) {
			incrementCurrentInstruction();
		}
	}

	public float distanceToNextPoint(Location currentLocation) {
		
			float totalDistance = 0;
			float[] results = new float[1];
			//First work out distance between current location and next GPX point in GPX route
			totalDistance = totalDistance + distanceToNextGPXPoint(currentLocation);
			
			//Then, for each offset between the point coming up and the GPX point for the offset at the next instruction, add on the distance
			for(int offsetID = currentGPXID + 1;offsetID<currentInstruction.offset;offsetID++){
				Location.distanceBetween(route.routeGPX.get(offsetID).getLatitude(), route.routeGPX.get(offsetID).getLongitude(), route.routeGPX.get(offsetID + 1).getLatitude(), route.routeGPX.get(offsetID + 1).getLongitude(), results);
				totalDistance = totalDistance + results[0];
			}
			return totalDistance;
		
		
	}

	public void updateCurrentInstruction(Integer newInstructionID) {
		currentInstructionID = newInstructionID;
		currentInstruction = route.routeInstructions.get(currentInstructionID);

		//routeOverlay.navigationHUD.nextRouteInstruction = currentInstruction;
		//this.routeOverlay.routeOverlay.setNextWayPoint(currentInstruction.getGeoPoint());
		//speakText(currentInstruction.description);
	}

	

	/**
	 * This method, assuming no prior knowledge of the situation, calculates the next waypoint.
	 * It does this by using a binary-chop type method.
	 * @param currentLocation
	 * @return the id of the next point
	 */
	private int nextGPXPointGivenCurrentPosition(Location currentLocation) {
		return linearGPXPointSearch(currentLocation);
	}

	private int linearGPXPointSearch(Location currentLocation) {
		int first = 0;
		int upto = route.routeGPX.size() - 1;
		int currentBest = 0;

		float distance = distanceBetween(currentLocation, route.routeGPX.get(first));
		float newDistance;
		for (int id = 0; id <= upto; id++) {

			newDistance = distanceBetween(currentLocation, route.routeGPX.get(id));
			if( newDistance < distance) {
				distance = newDistance;
				currentBest = id;
			}

		}
		//Now to check if its before or after the closest point

		//If currentBest is 0, user has just set off
		if(currentBest == 0) {
			return 1; //1 is next point after 0
		}
		//If its the last one, user is about to hit destination
		if(currentBest == upto) {
			return upto;
		}

		//Else, check the distances between the one before and one after
		float higherDistance = distanceBetween(currentLocation, route.routeGPX.get(currentBest + 1));
		float lowerDistance = distanceBetween(currentLocation, route.routeGPX.get(currentBest + 1));

		//If they are the same, that means the route tells the user to turn around. We therefore have no way of knowing the correct waypoint without 
		//knowing the user's current orientation, as they could be going towards the turn around or away from it.
		if (higherDistance == lowerDistance) {
			//Guess to go for the upper one
			return currentBest + 1;
		}
		if (higherDistance > lowerDistance) {
			//That means its somewhere between the lower point and the currentBest. Next one is therefore the currentBest
			return currentBest;
		}
		if (higherDistance < lowerDistance) {
			//That means its somewhere between the upper point and the currentBest. 
			return currentBest + 1;
		}
		return currentBest; //Java complains
	}
	
	
	
	
	

	/**
	 * This tries a binary search type approach. It doesn't work very well
	 * @param currentLocation
	 * @return id of the next gpx point
	 */
	private int binaryGPXPointSearch(Location currentLocation) {
		int first = 0;
		int upto = route.routeGPX.size() - 1;

		while (upto - first > 1) {
			int mid = (first + upto) / 2;  // Compute mid point.
			float distanceFromLower = distanceBetween(currentLocation, route.routeGPX.get(first));

			float distanceFromUpper = distanceBetween(currentLocation, route.routeGPX.get(upto));
			if (distanceFromLower < distanceFromUpper) {
				upto = mid;     // repeat search in bottom half.
			} else if (distanceFromLower > distanceFromUpper) {
				first = mid + 1;  // Repeat search in top half.
			} else {
				return upto; //This should pretty much never happen.
			}
		}
		return upto;
	}



	private float distanceToNextGPXPoint(Location currentLocation) {
		float[] results = new float[1];
		Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), route.routeGPX.get(currentGPXID).getLatitude(), route.routeGPX.get(currentGPXID).getLongitude(), results);

		return results[0];
	}

	public void navigateTo(final GeoPoint to, final String vehicle) {
		if (mostRecentLocation != null) {
			refreshRoute(TypeConverter.locationToGeoPoint(mostRecentLocation), to, vehicle);
		} else {
			Log.v(OpenSatNavConstants.LOG_TAG, "No most recent location");
		}
	}

	public void refreshRoute(final GeoPoint from, final GeoPoint to,
			final String vehicle) {
		Log.v(OpenSatNavConstants.LOG_TAG, "Called refreshroute");
		latestRouteReceived.second += 1;
		Time now = new Time();
		now.setToNow();
		if (!gettingRoute && latestRouteReceived.before(now)) {
			gettingRoute = true;
			final ProgressDialog progress = ProgressDialog.show(
					context, context.getResources().getText(
							R.string.please_wait), context.getResources().getText(
									R.string.getting_route), true, true,
									new OnCancelListener() {
						@Override
						// if the user cancels move the time up anyway so it
						// doesn't popup again
						public void onCancel(DialogInterface dialog) {
							latestRouteReceived.setToNow();
						}
					});
			final Handler handler = new Handler() {
				// threading stuff - this actually handles the stuff after the
				// thread has completed (code below)
				@Override
				public void handleMessage(Message msg) {
					if (route != null) {


						try {
							startRoute(route);
							mOsmv.invalidate();
						} catch (NoLocationProvidersException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						Toast.makeText(
								context,
								context.getResources().getText(
										R.string.directions_not_found),
										Toast.LENGTH_LONG).show();
					}
					if (progress.isShowing())
						try {
							progress.dismiss();
						} catch (IllegalArgumentException e) {
							// if orientation change, thread continue but the
							// dialog cannot be dismissed without exception
						}
						gettingRoute = false;

						latestRouteReceived.setToNow();
				}
			};
			new Thread(new Runnable() {
				public void run() {
					// put long running operations here
					Router router = new CloudmadeRouter();
					if (to != null)
						route = router.getRoute(from, to, vehicle,
								context);
					// ok, we are done
					handler.sendEmptyMessage(0);
				}
			}).start();
		}


	}

	private double distanceFromRoute(Location currentLocation) {

		//Edge condition: First point on route.
		if (currentGPXID == 0) {
			return distanceBetween(currentLocation, route.routeGPX.get(currentGPXID));
		}

		GeoPoint lastPoint = route.routeGPX.get(currentGPXID - 1);
		GeoPoint nextPoint = route.routeGPX.get(currentGPXID);


		return DistancePoint.distanceToSegment(TypeConverter.locationToGeoPoint(currentLocation), lastPoint, nextPoint);

	}


	@Override
	public void onLocationChanged(Location currentLocation) {
		if (currentlyRouting) {
		Log.v(OpenSatNavConstants.LOG_TAG, "RIC Running onLocationChanged");
		mostRecentLocation = currentLocation;
		if (!gettingRoute) {
				
				//Bit problematic this
			//if(!isOnRoute(currentLocation)) {
			//	speakText("Off route, getting new route");
			//	refreshRoute(TypeConverter.locationToGeoPoint(currentLocation), route.to, route.vehicle);
			//	return;
			//}

			//updateIDs(currentLocation);
			
			//float distanceToNextPoint = distanceToNextPoint(currentLocation);
			//routeOverlay.distanceToNextPoint = distanceToNextPoint;
			//Log.v(OpenSatNavConstants.LOG_TAG, "gettingRoute is " + gettingRoute);
			
			//updateWidget(distanceToNextPoint);

			
		}
		}
	}
	
	public void updateIDs(Location currentLocation) {
		int newGPXID = nextGPXPointGivenCurrentPosition(currentLocation);

		if (newGPXID != currentGPXID) {
			currentGPXID = newGPXID;
			//routeOverlay.routeOverlay.setNextGPXPoint(route.routeGPX.get(currentGPXID));
			//See whether a next instruction actually exists


			for(int i = 1; i < route.routeInstructions.size() -1; i++) {
				if (route.routeInstructions.get(i).offset > currentGPXID) {
					//Offset is larger, so we haven't reached that point in the route yet
					updateCurrentInstruction(i);
					break;
				} 
			}


			
		}
	}
	
	public void updateWidget(Float distanceToNextPoint) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);

		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		final int N = appWidgetIds.length;
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];
			RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);



			updateViews.setTextViewText(R.id.distanceLeft, String.valueOf(distanceToNextPoint)); 

			appWidgetManager.updateAppWidget(appWidgetId, updateViews);
		}
	}

	

	private void incrementCurrentInstruction() {
		currentInstructionID++;

		updateCurrentInstruction(currentInstructionID);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	


	private float distanceBetween(Location one, GeoPoint two) {
		float[] results = new float[1];
		Location.distanceBetween(one.getLatitude(), one.getLongitude(), two.getLatitude(), two.getLongitude(), results);
		return results[0];
	}

	private Boolean isOnRoute(Location currentLocation) {

		//We need to draw a "line" between the last GPX-point and the next GPX-point
		if( distanceFromRoute(currentLocation) > MAXIMUM_DISTANCE_FROM_ROUTE + currentLocation.getAccuracy()) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void stopRouting() {
		Log.v(OpenSatNavConstants.LOG_TAG, "RIC Stop Routing");
		currentlyRouting = false;
		route = null;
		if (routeOverlay != null) {
			routeOverlay.isCurrentlyRouting = false;
		}
		
		if (mLocationHandler != null) {
			
			mLocationHandler.stop();
			Log.v(OpenSatNavConstants.LOG_TAG, "RIC Stopped location handler");
		}
	}

	
}
