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
package org.opensatnav.android;

import java.io.IOException;
import java.util.ArrayList;

import net.wigle.wigleandroid.ZoomButtonsController;

import org.opensatnav.android.services.GeoCoder;
import org.opensatnav.android.util.BugReportExceptionHandler;
import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxyImpl;
import org.osmdroid.constants.OpenStreetMapConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayControlView;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import cl.droid.transantiago.MySuggestionProvider;
import cl.droid.transantiago.R;
import cl.droid.transantiago.activity.HomeActivity;
import cl.droid.transantiago.activity.TransChooseLocationServiceActivity;
import cl.droid.transantiago.service.TransantiagoGeoCoder;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.BaseColumns; //import android.speech.tts.TextToSpeech;
import android.provider.SearchRecentSuggestions;
//import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.method.ScrollingMovementMethod;
import android.text.method.SingleLineTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.BadTokenException;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ZoomControls;

public class SatNavActivity extends Activity implements
		OpenStreetMapConstants, OnSharedPreferenceChangeListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int MENU_ZOOMIN_ID = Menu.FIRST;
	private static final int MENU_ZOOMOUT_ID = MENU_ZOOMIN_ID + 1;
	private static final int MENU_RENDERER_ID = MENU_ZOOMOUT_ID + 1;
	private static final int MENU_TOGGLE_FOLLOW_MODE = MENU_RENDERER_ID + 1;
	private static final int MENU_FIND_POIS = MENU_TOGGLE_FOLLOW_MODE + 1;
	private static final int MENU_DIRECTIONS_TOGGLE = MENU_FIND_POIS + 1;
	private static final int MENU_PREFERENCES = MENU_DIRECTIONS_TOGGLE + 1;
	private static final int MENU_ABOUT = MENU_PREFERENCES + 1;
	private static final int DIRECTIONS_OPTIONS = MENU_ABOUT + 1;
	private static final int MENU_CONTRIBUTE = DIRECTIONS_OPTIONS + 1;
	private static final int MENU_TRIP_STATS = MENU_CONTRIBUTE + 1;
	private static final int MENU_TRANS_TOGGLE = MENU_TRIP_STATS + 1;
	private static final int MENU_SEARCH = MENU_TRANS_TOGGLE + 1;
	
	private static final int MENU_LAST_ID = MENU_SEARCH + 1;

	private static final int SELECT_POI = 0;
	private static final int CONTRIBUTE = SELECT_POI + 1;
	private static final int UPLOAD_NOW = 10;
	private static final int TRACE_TOGGLE = UPLOAD_NOW + 1;
	private static final int DELETE_TRACKS = TRACE_TOGGLE + 1;
	private static final int NEW_WAYPOINT = DELETE_TRACKS + 1;
	private static final int CLEAR_OLD_TRACES = NEW_WAYPOINT + 1;
	private static final int MY_DATA_CHECK_CODE = 600;

	// ===========================================================
	// Fields
	// ===========================================================

	private MapView mOsmv;
	private ZoomControls zoomControls;
	private MyLocationOverlay mMyLocationOverlay;
//	private SimpleLocationOverlay mMyLocationOverlay;
	private ScaleBarOverlay mScaleBarOverlay;
	private ItemizedIconOverlay<OverlayItem> mItemizedOverlay;
	private ResourceProxy mResourceProxy;
	private PopupControls popup;
	Button busstopButton;
	Button refButton;
	
//	private static TripStatisticsController mTripStatsController;

	/**
	 * The currently selected track (or null if nothing selected).
	 */
//	private Track selectedTrack;

	/**
	 * The id of the currently recording track.
	 */
	private long recordingTrackId = -1;

	/**
	 * Id of the last location that was seen when reading tracks from the
	 * provider. This is used to determine which locations are new compared to
	 * the last time the mapOverlay was updated.
	 */
//	private long lastSeenLocationId = -1;

	/**
	 * Does the user want to share the current track.
	 */
//	private boolean shareRequested = false;

	/**
	 * From the shared preferences:
	 */
	private int minRequiredAccuracy = OpenSatNavConstants.DEFAULT_MIN_REQUIRED_ACCURACY;

	/**
	 * Utilities to deal with the database.
	 */
//	private IProviderUtils providerUtils;

	/**
	 * Are we recording?
	 */
	boolean isRecording;

	/**
	 * The connection to the track recording service.
	 */

	/**
	 * A thread with a looper. Post to updateTrackHandler to execute Runnables
	 * on this thread.
	 */
//	private final HandlerThread updateTrackThread = new HandlerThread(
//			"updateTrackThread");

	/** Handler for updateTrackThread */
//	private Handler updateTrackHandler;

//	private ContentObserver observer;
//	private ContentObserver waypointObserver;

	/** Handler for callbacks to the UI thread */
//	private final Handler uiHandler = new Handler();

	/**
	 * Singleton instance
	 */
	private static SatNavActivity instance = null;

//	private SendToOSMDialog sendToOSMDialog;

//	private OpenStreetMapViewDirectedLocationOverlay mMyLocationOverlay;
//	protected RouteOverlay routeOverlay;
//	protected OpenStreetMapViewTraceOverlay traceOverlay;
//	protected OpenStreetMapViewTouchResponderOverlay mTouchResponder;
	protected boolean autoFollowing = true;
	protected boolean viewingTripStatistics = false;

	protected Location currentLocation;

	protected SharedPreferences prefs;
//	private SharedPreferences mPrefs;
//	protected Route route = new Route();
//	protected RouteInstructionsService routeInstructionsService;

	private RelativeLayout layout;

	// ===========================================================
	// Constructors
	// ===========================================================
	public static SatNavActivity getInstance() {
		return instance;
	}
//	@Widget
	public class PopupControls extends LinearLayout {
		private final Button mClose;
	    private final Button mLaunch;
	    public final TextView title;
	    public final TextView description;
	    public OverlayItem item;
		public PopupControls(Context context) {
//			super(context);
			this(context, null);
			// TODO Auto-generated constructor stub
		}
		public PopupControls(Context context, AttributeSet attrs) {
	        super(context, attrs);
	        setFocusable(false);
	        
	        LayoutInflater inflater = (LayoutInflater) context
	                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        inflater.inflate(R.layout.popup, this, // we are the parent
	                true);
	        
	        mClose = (Button) findViewById(R.id.btn_close);
	        mLaunch = (Button) findViewById(R.id.btn_launch);
	        title = (TextView) findViewById(R.id.title);
	        description = (TextView) findViewById(R.id.description);
	        
//	        int mInputType = EditorInfo.TYPE_NULL;
//	        mInputType |= EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
//	        description.setInputType(mInputType);
	        description.setMovementMethod(new ScrollingMovementMethod());
//	        description.setVerticalScrollBarEnabled(true);
//	        description.setTransformationMethod(SingleLineTransformationMethod.getInstance());
//	        description.setMaxLines(Integer.MAX_VALUE);	
//	        description.setHorizontallyScrolling(false);	
//	        description.setTransformationMethod(null);
//	        description.setSingleLine(false);

//	        description.setInputType() |= EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
	        
	        item = null;
	        
	        mClose.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View v) {
//					Toast.makeText(
//					SatNavActivity.this,
//					"mZoomIn", Toast.LENGTH_LONG).show();
					popup.setVisibility(View.GONE);
			    }
			});
	        mLaunch.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View v) {
//					Toast.makeText(
//					SatNavActivity.this,
//					"mZoomOut", Toast.LENGTH_LONG).show();
//					popup.setVisibility(View.GONE);
					SatNavActivity.this.launch(title.getText().toString());
			    }
			});
	    }
		}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		BugReportExceptionHandler.register(this);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
//		final RelativeLayout rl = new RelativeLayout(this);
		((TextView) findViewById(R.id.title_text)).setText("Map");
		
//		Toast.makeText(
//				SatNavActivity.this,
//				"SatNavActivity onCreate"
//				, Toast.LENGTH_LONG).show();
		
		mResourceProxy = new ResourceProxyImpl(getApplicationContext());
		
//		LayoutInflater inflater = (LayoutInflater)this.getSystemService
//	      (Context.LAYOUT_INFLATER_SERVICE);
//		LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.map, null);
		final RelativeLayout rl = (RelativeLayout) findViewById(R.id.map_rl);
//		if (titlebar != null)
//			rl.addView(titlebar);
		layout = rl;
		
////		 TitleBar Search
//		findViewById(R.id.titlebar_btn_search_map).setOnClickListener(new View.OnClickListener() {
//		    public void onClick(View v) {
//		    	onSearchOnMap();
////		    	onSearchRequested();
//		    }
//		});
		findViewById(R.id.titlebar_btn_mylocation).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	mMyLocationOverlay.enableMyLocation();
		    	mMyLocationOverlay.enableFollowLocation();
		    }
		});
		// Set the title
		((TextView) findViewById(R.id.title_text)).setText("Map");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		instance = this;
//		Object[] retainables = (Object[]) getLastNonConfigurationInstance();
		
//		this.autoFollowing = prefs.getBoolean(PREFS_SHOW_LOCATION, false);
		
//		Intent svc = new Intent(this, RouteInstructionsService.class);
//		startService(svc);

		this.mOsmv = new MapView(this, 256)
//		OpenStreetMapView(this, OpenStreetMapRendererInfo
//				.getFromPrefName(prefs.getString("map_style", "mapnik"))) 
		{	
			@Override
			public boolean onTouchEvent(MotionEvent event) {
//				// switches to 'planning mode' as soon as you scroll anywhere
				if (event.getAction() == MotionEvent.ACTION_MOVE
//						&& SatNavActivity.this.autoFollowing == true
						) {
//					SatNavActivity.this.autoFollowing = false;
//					SatNavActivity.this.displayToast(R.string.planning_mode_on);
						mMyLocationOverlay.disableMyLocation();
						mMyLocationOverlay.disableFollowLocation();
				}
//				updateZoomButtons();
				return super.onTouchEvent(event);
			}
		};
		rl.addView(this.mOsmv, new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
//		providerUtils = IProviderUtils.Factory.get(this);
		mOsmv.getController().setZoom(prefs.getInt(PREFS_ZOOM_LEVEL, 14));
		mOsmv.scrollTo(prefs.getInt(PREFS_SCROLL_X, -823161), prefs.getInt(PREFS_SCROLL_Y, 413748));

//		if (mLocationHandler.getFirstLocation() != null)
//			this.mOsmv.setMapCenter(TypeConverter
//					.locationToGeoPoint(mLocationHandler.getFirstLocation()));

		/* Scale Bar Overlay */
		{
			this.mScaleBarOverlay = new ScaleBarOverlay(this);
			this.mOsmv.getOverlays().add(mScaleBarOverlay);
			// Scale bar tries to draw as 1-inch, so to put it in the top center, set x offset to
			// half screen width, minus half an inch.
			this.mScaleBarOverlay.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels
					/ 2 - getResources().getDisplayMetrics().xdpi / 2, 10);
		}
		
		/* SingleLocation-Overlay */
		{
			/*
			 * Create a static Overlay showing a single location. (Gets updated
			 * in onLocationChanged(Location loc)!
			 */
//			if(retainables != null)
//				this.mMyLocationOverlay = (OpenStreetMapViewDirectedLocationOverlay) retainables[1];
//			else
//				this.mMyLocationOverlay = new OpenStreetMapViewDirectedLocationOverlay(
//					this);
//			this.mOsmv.getOverlays().add(mMyLocationOverlay);

			this.mMyLocationOverlay = new MyLocationOverlay(this.getBaseContext(), this.mOsmv){	
			};
			this.mOsmv.setBuiltInZoomControls(true);
			this.mOsmv.setMultiTouchControls(true);
			this.mOsmv.getOverlays().add(this.mMyLocationOverlay);
			
		}

		/* Other overlays */
		/* Itemized Overlay */
		{
			final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
			/* OnTapListener for the Markers, shows a simple Toast. */
			this.mItemizedOverlay = new ItemizedIconOverlay<OverlayItem>(items,
					this.getResources().getDrawable(R.drawable.blue_pin_hi_48),
					new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
						@Override
						public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
//							Toast.makeText(
//									SatNavActivity.this,
//									"Item '" + item.mTitle + "' (index=" + index
//											+ ") got single tapped up", Toast.LENGTH_LONG).show();
							popup.title.setText(item.mTitle);
							popup.description.setText(item.mDescription);
							popup.setVisibility(View.VISIBLE);
							return true; // We 'handled' this event.
						}

						@Override
						public boolean onItemLongPress(final int index, final OverlayItem item) {
//							Toast.makeText(
//									SatNavActivity.this,
//									"Item '" + item.mTitle + "' (index=" + index
//											+ ") got long pressed", Toast.LENGTH_SHORT).show();							
//							launch(item.mTitle);
							return true;
						}
					}, mResourceProxy);
//			this.mItemizedOverlay.setFocusItemsOnTap(true);
			this.mOsmv.getOverlays().add(this.mItemizedOverlay);
			
//			AttributeSet attrs = null;
//			ItemizedOverlayControlView mItemizedOverlayControlView = new ItemizedOverlayControlView(this.getBaseContext(), attrs);
//			layout.addView(mItemizedOverlayControlView);
		}
		
//		traceOverlay = new OpenStreetMapViewTraceOverlay(this);// Buggy, so
																// taken out for
																// the moment
		// this.mOsmv.getOverlays().add(traceOverlay);

//		if (retainables != null)
//			routeOverlay = (RouteOverlay) retainables[2];
//		else
//			routeOverlay = new RouteOverlay(this);
//		this.mOsmv.getOverlays().add(routeOverlay);
//		routeInstructionsService = new RouteInstructionsService(this,
//				routeOverlay, mOsmv);
//		mTouchResponder = new OpenStreetMapViewTouchResponderOverlay(
//				routeInstructionsService);
//		this.mOsmv.getOverlays().add(mTouchResponder);

		/* ZoomControls */
		{
//			zoomControls = new ZoomControls(this);
////			// by default we are zoomed in to the max
//			zoomControls.setIsZoomInEnabled(true);
//			zoomControls.setOnZoomOutClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					SatNavActivity.this.mOsmv.getController().zoomOut();
////					updateZoomButtons();
//				}
//			});
//			zoomControls.setOnZoomInClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					SatNavActivity.this.mOsmv.getController().zoomIn();
////					updateZoomButtons();
//				}
//			});
			
//			final RelativeLayout.LayoutParams zoomParams = new RelativeLayout.LayoutParams(
//					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
//					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
//			zoomParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//			zoomParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//			rl.addView(zoomControls, zoomParams);

		}
		/* POPUP */
		{
			popup = new PopupControls(this);
			final RelativeLayout.LayoutParams zoomParams = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			zoomParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			zoomParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			popup.setVisibility(View.GONE);
			rl.addView(popup, zoomParams);
//			rl.addView(popup);
		}

		/* Fetch busstop buttom */
		{
			busstopButton = new Button(this);
//			busstopButton.setText("Para");
			busstopButton.setBackgroundResource(R.drawable.parada_cercana_64);
			busstopButton.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View v) {
			    	SatNavActivity.this.showRefreshSpinner(true);
			    	onSearchOnMap();
			    }
			});
			{
			final RelativeLayout.LayoutParams zoomParams = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			zoomParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			zoomParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			rl.addView(busstopButton, zoomParams);
			}
			{
			final RelativeLayout.LayoutParams zoomParams = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			zoomParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			zoomParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			refButton = new Button(this);
			refButton.setBackgroundResource(R.drawable.ic_menu_refresh);
//			zoomParams.addRule(RelativeLayout.LEFT_OF, busstopButton.getId());
			refButton.setVisibility(View.GONE);
			rl.addView(refButton, zoomParams);
//			rl.addView(popup);
		
			}
		}

		// Recorded Trace Overlay
//		updateTrackThread.start();
//		updateTrackHandler = new Handler(updateTrackThread.getLooper());

		// Register observer for the track point provider:
		Log.d(OpenSatNavConstants.LOG_TAG, "INIT OF THE CONTENTOBSERVER");
//		Handler contentHandler = new Handler();
//		observer = new ContentObserver(contentHandler) {
//			@Override
//			public void onChange(boolean selfChange) {
//				Log.d(OpenSatNavConstants.LOG_TAG,
//						"MyTracksMap: ContentObserver.onChange");
//				// Check for any new locations and append them to the currently
//				// recording track:
//
//				if (isRecording) {
//					// No track is being recorded. We should not be here.
//					Log.v(OpenSatNavConstants.LOG_TAG,
//							"Not recording, so didn't update map view");
//					return;
//				}
////				if (selectedTrack == null
////						|| selectedTrack.getId() != recordingTrackId) {
////					Log.v(OpenSatNavConstants.LOG_TAG,
////							"No track or something else selected?");
////					// No track, or one other than the recording track is
////					// selected,
////					// don't bother.
////					return;
////				}
//				Log
//						.v(OpenSatNavConstants.LOG_TAG,
//								"About to update trace view");
//				// Update can potentially be lengthy, put it in its own thread:
//				updateTrackHandler.post(updateTrackRunnable);
//				super.onChange(selfChange);
//			}
//		};

//		waypointObserver = new ContentObserver(contentHandler) {
//			@Override
//			public void onChange(boolean selfChange) {
//				Log.d(OpenSatNavConstants.LOG_TAG,
//						"MyTracksMap: ContentObserver.onChange waypoints");
////				if (selectedTrack == null) {
////					return;
////				}
////				updateTrackHandler.post(restoreWaypointsRunnable);
//				super.onChange(selfChange);
//			}
//		};

		// Read shared preferences and register change listener:
		SharedPreferences preferences = getSharedPreferences(
				OpenSatNavConstants.SETTINGS_NAME, 0);
		if (preferences != null) {
			minRequiredAccuracy = preferences.getInt(
					OpenSatNavConstants.MIN_REQUIRED_ACCURACY,
					OpenSatNavConstants.DEFAULT_MIN_REQUIRED_ACCURACY);
			recordingTrackId = preferences.getLong(
					OpenSatNavConstants.RECORDING_TRACK, -1);
			long selectedTrackId = preferences.getLong(
					OpenSatNavConstants.SELECTED_TRACK, -1);
			if (selectedTrackId >= 0) {
				setSelectedTrack(selectedTrackId);
			}

			preferences.registerOnSharedPreferenceChangeListener(this);
		}

//		// Trip statistics
		//mTripStatsController = new TripStatisticsController(SatNavActivity.this);
//		mTripStatsController.addViewTo(rl);
//		// for after configuration change like keyboard open/close
//		TripStatistics.TripStatisticsStrings data = null;
//		if (retainables != null)
//			data = (TripStatistics.TripStatisticsStrings) retainables[0];
//
//		if (data != null) {
//
//			mTripStatsController.setAllStats(data);
//
//		}
//		this.setContentView(rl);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	private void updateZoomButtons() {
//		if (SatNavActivity.this.mOsmv.canZoomIn()) {
//			zoomControls.setIsZoomInEnabled(true);
//		} else {
//			zoomControls.setIsZoomInEnabled(false);
//		}
//		if (SatNavActivity.this.mOsmv.canZoomOut()) {
//			zoomControls.setIsZoomOutEnabled(true);
//		} else {
//			zoomControls.setIsZoomOutEnabled(false);
//		}
	}

//	@Override
//	public void onLocationChanged(Location newLocation) {
//		if (newLocation != null) {
////			this.mMyLocationOverlay.setLocation(TypeConverter
////					.locationToGeoPoint(newLocation));
////			this.mMyLocationOverlay.setBearing(newLocation.getBearing());
////			this.mMyLocationOverlay.setSpeed(newLocation.getSpeed());
////			this.mMyLocationOverlay.setAccuracy(newLocation.getAccuracy());
//
//			// TODO: Change this!
////			this.mTouchResponder.setLocation(TypeConverter
////					.locationToGeoPoint(newLocation));
//
////			if (autoFollowing) {
////				this.mOsmv.setMapCenter(TypeConverter
////						.locationToGeoPoint(newLocation));
////			} else {
////				// tell the viewer that it should redraw
////				SatNavActivity.this.mOsmv.postInvalidate();
////			}
//
//			if (OpenSatNavConstants.DEBUGMODE)
//				Log.v(OpenSatNavConstants.LOG_TAG, "Accuracy: "
//						+ newLocation.getAccuracy());
//			currentLocation = newLocation;
//
//			if (isRecording) {
//				updateTrackHandler.post(updateTrackRunnable);
//			}
//		}
//	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		
//		pMenu.add(0, R.id.search, 0, R.string.stops_on_map)
//    	.setIcon(android.R.drawable.ic_search_category_default)
//    	.setAlphabeticShortcut(SearchManager.MENU_KEY);
		
//		MenuItem transMenuItem = pMenu.add(0, MENU_TRANS_TOGGLE,
//				Menu.NONE, R.string.get_trans);
		
//		MenuItem directionsMenuItem = pMenu.add(0, MENU_DIRECTIONS_TOGGLE,
//				Menu.NONE, R.string.get_directions);
//		directionsMenuItem.setIcon(android.R.drawable.ic_menu_directions);
//		MenuItem contributeMenuItem = pMenu.add(0, MENU_CONTRIBUTE, Menu.NONE,
//				R.string.menu_contribute);
//		contributeMenuItem.setIcon(android.R.drawable.ic_menu_edit);
//		MenuItem toggleAutoFollowMenuItem = pMenu.add(0,
//				MENU_TOGGLE_FOLLOW_MODE, Menu.NONE, R.string.planning_mode);
//		toggleAutoFollowMenuItem.setIcon(android.R.drawable.ic_menu_mapmode);
//		MenuItem tripStatsMenuItem = pMenu.add(0, MENU_TRIP_STATS, Menu.NONE,
//				R.string.menu_show_trip_stats);
//		tripStatsMenuItem.setIcon(android.R.drawable.ic_menu_recent_history);
		
//		MenuItem prefsMenuItem = pMenu.add(0, MENU_PREFERENCES, Menu.NONE,
//				R.string.preferences);
//		prefsMenuItem.setIcon(android.R.drawable.ic_menu_preferences);
		
		// Put overlay items next
		this.mOsmv.getOverlayManager().onCreateOptionsMenu(pMenu, MENU_LAST_ID, mOsmv);
//		this.mMyLocationOverlay.onCreateOptionsMenu(pMenu, MENU_LAST_ID, mOsmv);
		
		MenuItem aboutMenuItem = pMenu.add(0, MENU_ABOUT, Menu.NONE,
				R.string.about);
		aboutMenuItem.setIcon(android.R.drawable.ic_menu_info_details);
		
		return true;
	}

	private void onSearchOnMap(){
		from = (GeoPoint) this.mOsmv.getMapCenter();
		if (!this.isOnline()){
			Toast.makeText(this, this.getResources().getText(
				R.string.error_no_inet_conn), Toast.LENGTH_LONG).show();
		} else
			getLocations(" ");
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
//		case R.id.search:
//			onSearchRequested();
//			Toast.makeText(this, "onSearchRequested", Toast.LENGTH_LONG).show();
//			return true;
//		case R.id.titlebar_btn_search_map:
//		case MENU_TRANS_TOGGLE:
//			onSearchOnMap();
//			return true;
//		case MENU_DIRECTIONS_TOGGLE:
//			if (!this.isOnline()){
//				Toast.makeText(this, this.getResources().getText(
//					R.string.error_no_inet_conn), Toast.LENGTH_LONG).show();
//				return true;
//			}
//			if (routeInstructionsService.currentlyRouting == false) {
//				if (currentLocation != null) {
//					Intent intent = new Intent(this,
//							org.opensatnav.android.GetDirectionsActivity.class);
//					intent.setData(Uri.parse(currentLocation.getLatitude()
//							+ "," + currentLocation.getLongitude()));
//
//					intent.putExtra("bbox", 
//							this.mOsmv.getDrawnBoundingBoxE6().getLonWestE6() / 1E6
//							+ "," + this.mOsmv.getDrawnBoundingBoxE6().getLatSouthE6() / 1E6
//							+ "," + this.mOsmv.getDrawnBoundingBoxE6().getLonEastE6() / 1E6
//							+ "," + this.mOsmv.getDrawnBoundingBoxE6().getLatNorthE6() / 1E6
//							);
//					intent.putExtra("lat", 
//							this.mOsmv.getMapCenter().getLatitude()
//							);
//					intent.putExtra("lon", 
//							this.mOsmv.getMapCenter().getLongitude()
//							);
//					
//					startActivityForResult(intent, DIRECTIONS_OPTIONS);
//
//					SatNavActivity.this.mOsmv.postInvalidate();
//				} else
//					Toast.makeText(this, R.string.start_directions_failed,
//							Toast.LENGTH_LONG).show();
//			} else {
//				// we are already routing, clear the route
//
//				Intent svc = new Intent(this, RouteInstructionsService.class);
//				stopService(svc);
//				routeInstructionsService.stopRouting();
//				SatNavActivity.this.mOsmv.postInvalidate();
//
//			}

//			return true;
//		case MENU_CONTRIBUTE:
//
//			Intent intentContribute = new Intent(this,
//					org.opensatnav.android.ContributeActivity.class);
//			startActivityForResult(intentContribute, CONTRIBUTE);
//
//			return true;
		case MENU_RENDERER_ID:
			this.mOsmv.invalidate();
			return true;
//		case MENU_TOGGLE_FOLLOW_MODE:
////			if (this.autoFollowing) {
//			if (prefs.getBoolean(PREFS_SHOW_LOCATION, false)){
//				this.autoFollowing = false;
//				Toast.makeText(this, R.string.planning_mode_on,
//						Toast.LENGTH_SHORT).show();
//				
//				prefs.edit().putBoolean(PREFS_SHOW_LOCATION,true);
//				this.mMyLocationOverlay.enableFollowLocation();
//			} else {
//				this.autoFollowing = true;
//				Toast.makeText(this, R.string.navigation_mode_on,
//						Toast.LENGTH_SHORT).show();
//				
//				prefs.edit().putBoolean(PREFS_SHOW_LOCATION,false);
//				this.mMyLocationOverlay.disableFollowLocation();
//			}
//			return true;
		case MENU_PREFERENCES:
			Intent intent = new Intent(this,
					org.opensatnav.android.ConfigurationActivity.class);
			startActivityForResult(intent, MENU_PREFERENCES);

			return true;
		case MENU_ABOUT:
			Intent intent1 = new Intent(this, org.openintents.about.About.class);
			startActivityForResult(intent1, MENU_ABOUT);

			return true;
//		case MENU_TRIP_STATS:
//			viewingTripStatistics = true;
//			showTripStatistics(true);
//
//			return true;
		}
		return this.mOsmv.getOverlayManager().onOptionsItemSelected(item, MENU_LAST_ID, mOsmv);
	}

	
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}

	public void launch(final String paradero) {
		Intent intent = new Intent(SatNavActivity.this,
				cl.droid.transantiago.activity.TransChooseServiceActivity.class);
		intent.putExtra("fromLocation", from.toDoubleString());
		//intent.putExtra("locations", locations);
		intent.putExtra("paradero", paradero);
		//
		String urlstring = "http://m.ibus.cl/index.jsp?paradero="+paradero+"&servicio=&boton.x=0&boton.y=0";
		////						Log.i(OpenSatNavConstants.LOG_TAG, urlstring);
		intent.putExtra("url", urlstring);
		startActivityForResult(intent,0);
	}
	
	protected Bundle locations;
	protected Boolean backgroundThreadComplete = true;
	protected ProgressDialog progress;
	protected GeoPoint from;
	protected String bbox;
	
	public void getLocations(final String toText) {
		if (toText.length() != 0) {
			backgroundThreadComplete = false;

//			progress = ProgressDialog.show(
//					SatNavActivity.this, this.getResources().getText(
//							R.string.please_wait), this.getResources().getText(
//							R.string.searching), true, true);

			bbox= this.mOsmv.getBoundingBox().getLonWestE6() / 1E6
			+ "," + this.mOsmv.getBoundingBox().getLatSouthE6() / 1E6
			+ "," + this.mOsmv.getBoundingBox().getLonEastE6() / 1E6
			+ "," + this.mOsmv.getBoundingBox().getLatNorthE6() / 1E6
			;
			
			final Handler handler = new Handler() {
				// threading stuff - this actually handles the stuff after the
				// thread has completed (code below)
				@Override
				public void handleMessage(Message msg) {
					if (progress!=null && progress.isShowing())
						try {
							progress.dismiss();
							backgroundThreadComplete = true;
						} catch (IllegalArgumentException e) {
							// if orientation change, thread continue but the dialog cannot be dismissed without exception
						}
						
					if (locations != null && locations.containsKey("names") && locations.getStringArray("names").length > 0 ) {
						Intent intent;
//						if (selectedPoi == -1){
//							intent = new Intent(this,
//								org.opensatnav.android.ChooseLocationActivity.class);
////								org.opensatnav.android.TransChooseLocationServiceActivity.class);
////								org.opensatnav.android.ChooseServiceActivity.class);
//						}else{ 
							intent = new Intent(SatNavActivity.this,
//									org.opensatnav.android.ChooseLocationActivity.class);
									cl.droid.transantiago.activity.TransChooseLocationServiceActivity.class);
//									org.opensatnav.android.ChooseServiceActivity.class);
//						}
						intent.putExtra("fromLocation", from.toDoubleString());
						intent.putExtra("locations", locations);
//						startActivityForResult(intent, MENU_TRANS_TOGGLE);
						
						if (locations.getInt("size")>0){
							int size = locations.getInt("size");
							Toast
							.makeText(
									SatNavActivity.this,
									String.format(
											SatNavActivity.this
												.getResources()
												.getText(
//													R.string.could_not_find_poi
													R.string.place_found).toString(),
											size,
											"paradero")
//											+ " " + size
											,
									Toast.LENGTH_LONG).show();
							String[] locationInfo = locations.getStringArray("info");
							String[] locationNames = locations.getStringArray("names");
							final int[] locationLats = locations.getIntArray("latitudes");
							final int[] locationLongs = locations.getIntArray("longitudes");
							for (int i=0; i<size; i++){
								((ItemizedIconOverlay<OverlayItem>)SatNavActivity.this.mItemizedOverlay).addItem(
										new OverlayItem(locationNames[i], locationInfo[i], new GeoPoint(locationLats[i],
												locationLongs[i]))
										);
								
							}
							SatNavActivity.this.mOsmv.invalidate();
						}
					} 
//					else {
//						if (selectedPoi == -1) { // text search
//							// specific case for UK postcodes
//							String currentLocale = GetDirectionsActivity.this.getResources().getConfiguration().locale.getCountry();
//							if ((currentLocale.compareTo("GB") == 0) && (UKPostCodeValidator.isPostCode(toText.trim()))) {
//								UKPostCodeValidator.showFreeThePostCodeDialog(GetDirectionsActivity.this);
//							} else {
//								String text = String
//										.format(
//												GetDirectionsActivity.this
//														.getResources()
//														.getText(
//																R.string.place_not_found)
//														.toString(), toText);
//								Toast.makeText(GetDirectionsActivity.this,
//										text, Toast.LENGTH_LONG).show();
//							}
//						} else { // poi search
//							String stringValue = getResources().getStringArray(
//									R.array.poi_types)[selectedPoi];
//							Toast
//									.makeText(
//											GetDirectionsActivity.this,
//											GetDirectionsActivity.this
//													.getResources()
//													.getText(
//															R.string.could_not_find_poi)
//													+ " " + stringValue,
//											Toast.LENGTH_LONG).show();
//						}
//					}
					if (locations != null && locations.containsKey("names") && locations.getStringArray("names").length == 0)
						Toast
						.makeText(
								SatNavActivity.this,
								String.format(
										SatNavActivity.this
											.getResources()
											.getText(
//												R.string.could_not_find_poi
												R.string.place_not_found).toString(),
										"paradero")
//										+ " " + stringValue
										,
								Toast.LENGTH_LONG).show();
					if (locations == null)
						Toast.makeText(SatNavActivity.this,
								SatNavActivity.this
								.getResources()
								.getText(
//									R.string.could_not_find_poi
									R.string.error_no_server_conn).toString(),
								Toast.LENGTH_LONG).show();
					SatNavActivity.this.showRefreshSpinner(false);
				}
			};
			new Thread(new Runnable() {
				public void run() {
//					progress = ProgressDialog.show(
//							SatNavActivity.this, SatNavActivity.this.getResources().getText(
//									R.string.please_wait), SatNavActivity.this.getResources().getText(
//									R.string.searching), true, true);
					
					// put long running operations here
					GeoCoder geoCoder = null;

					
//					geoCoder = new PlanoturGeoCoder();
//					geoCoder = new NominatimGeoCoder();
						
					
//					if (selectedPoi == -1) { // text search, rank results within an area
//						locations = geoCoder.query(toText, from, GeoCoder.IN_AREA, 25,
//								SatNavActivity.this);
//					}
//					else if (selectedPoi == -2){
//						String slat = String.valueOf(getIntent().getDoubleExtra("lat",0.0));
//						String slon = String.valueOf(getIntent().getDoubleExtra("lon",0.0));
						locations = (new TransantiagoGeoCoder()).query(toText, from, GeoCoder.IN_AREA, 25,
								SatNavActivity.this, bbox );
//						locations = (new TransantiagoGeoCoder()).query(toText, from, GeoCoder.IN_AREA, 25,
//								GetDirectionsActivity.this, slat, slon);
//						}
//					else {  //POI search, just find the nearest matching POI
//					locations = geoCoder.query(toText, from, GeoCoder.FROM_POINT, 25,
//							SatNavActivity.this);
//					}
					// ok, we are done
					handler.sendEmptyMessage(0);
				}
			}).start();

		}
	}
	/** Display trip statistics */
	public void showTripStatistics(boolean show) {
//		if (show) {
//			mTripStatsController.setVisible(true);
//			mOsmv.setVisibility(View.GONE);
//			zoomControls.setVisibility(View.GONE);
//		} else {
//			mTripStatsController.setVisible(false);
//			mOsmv.setVisibility(View.VISIBLE);
//			zoomControls.setVisibility(View.VISIBLE);
//		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
//		MenuItem navigationMenu = menu.findItem(MENU_DIRECTIONS_TOGGLE);

//		if (routeInstructionsService.currentlyRouting == false) {
//			navigationMenu.setTitle(R.string.get_directions).setIcon(
//					android.R.drawable.ic_menu_directions);
//		} else {
//			navigationMenu.setTitle(R.string.clear_directions).setIcon(
//					android.R.drawable.ic_menu_close_clear_cancel);
//		}

//		MenuItem followMenu = menu.findItem(MENU_TOGGLE_FOLLOW_MODE);
////		if (!(this.autoFollowing)) {
//		if (!prefs.getBoolean(PREFS_SHOW_LOCATION, false)){
//
//			// this weird style is required to set multiple attributes on
//			// the item
//
//			followMenu.setTitle(R.string.navigation_mode).setIcon(
//					android.R.drawable.ic_menu_mylocation);
//		} else {
//
//			followMenu.setTitle(R.string.planning_mode).setIcon(
//					android.R.drawable.ic_menu_mapmode);
//		}
		this.mOsmv.getOverlayManager().onPrepareOptionsMenu(menu, MENU_LAST_ID, mOsmv);
		return super.onPrepareOptionsMenu(menu);
//		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if ((requestCode == DIRECTIONS_OPTIONS) || (requestCode == SELECT_POI)) {
			if (resultCode == RESULT_OK) {
				GeoPoint to = GeoPoint.fromIntString(data.getStringExtra("to"));
				String vehicle = data.getStringExtra("vehicle");
//				if (currentLocation != null) {
//					routeInstructionsService.refreshRoute(TypeConverter
//							.locationToGeoPoint(currentLocation), to, vehicle);
//
//				}

			}
			/*
			 * if (requestCode == MY_DATA_CHECK_CODE) { if (resultCode ==
			 * TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) { // success, create
			 * the TTS instance //routeInstructionsService.mTts = new
			 * TextToSpeech(this, this); } else { // missing data, install it
			 * Intent installIntent = new Intent(); installIntent.setAction(
			 * TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
			 * startActivity(installIntent); } }
			 */

		}
		Log.v(OpenSatNavConstants.LOG_TAG, "Code is " + requestCode);
		Log.v(OpenSatNavConstants.LOG_TAG, "resultCode is " + resultCode);
//		if (resultCode == ContributeConstants.SEND_TO_OSM_DIALOG) {
//			Log.v(OpenSatNavConstants.LOG_TAG, "Sending to OSM");
//			shareRequested = false;
//			showDialogSafely(ContributeConstants.DIALOG_SEND_TO_OSM);
//		}

	}

//	@Override
//	public void onSaveInstanceState(Bundle savedInstanceState) {
//
//		savedInstanceState.putInt("zoomLevel", this.mOsmv.getZoomLevel());
//		savedInstanceState.putBoolean("autoFollowing", autoFollowing);
//		Log.v(OpenSatNavConstants.LOG_TAG, "Put " + this.mOsmv.getZoomLevel()
//				+ " into zoomlevel");
//		savedInstanceState.putBoolean("viewTripStatistics",
//				viewingTripStatistics);
//		savedInstanceState.putInt("mLatitudeE6", this.mOsmv
//				.getMapCenter().getLatitudeE6());
//		savedInstanceState.putInt("mLongitudeE6", this.mOsmv
//				.getMapCenter().getLongitudeE6());
//
//		super.onSaveInstanceState(savedInstanceState);
//	}

//	@Override
//	public void onRestoreInstanceState(Bundle savedInstanceState) {
//		super.onRestoreInstanceState(savedInstanceState);
//
//		autoFollowing = savedInstanceState.getBoolean("autoFollowing");
//		this.mOsmv.getController().setZoom(savedInstanceState.getInt("zoomLevel"));
////		if (this.mOsmv.canZoomIn()) {
////			zoomControls.setIsZoomInEnabled(true);
////			if (!this.mOsmv.canZoomOut())
////				zoomControls.setIsZoomOutEnabled(false);
////		}
////		this.mOsmv.setMapCenter(savedInstanceState.getInt("mLatitudeE6"),
////				savedInstanceState.getInt("mLongitudeE6"));
//
//		viewingTripStatistics = savedInstanceState
//				.getBoolean("viewTripStatistics");
//		if (viewingTripStatistics) {
//			showTripStatistics(true);
//		}
//	}

	private void displayToast(String msg) {
		Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
	}

	private void displayToast(int stringReference) {
		displayToast((String) getText(stringReference));
	}

	@Override
	public void onResume() {
		// Called when the current activity is being displayed or re-displayed
		// to the user.
		Log.d(OpenSatNavConstants.LOG_TAG, "Resuming OSN");

		// Make sure any updates that might have happened are propagated to the
		// Map overlay:
//		observer.onChange(false);
//		waypointObserver.onChange(false);
		instance = this;

		// mTripStatsController = new
		// TripStatisticsController(SatNavActivity.this);
		// mTripStatsController.addViewTo(rl);
		// for after configuration change like keyboard open/close

//		mTripStatsController.start();
//		registerContentObservers();
		// registerLocationAndSensorListeners();
		super.onResume();
		
		final String tileSourceName = 
//			prefs.getString("map_style",
			prefs.getString(PREFS_TILE_SOURCE,
				TileSourceFactory.DEFAULT_TILE_SOURCE.name());
		try {
			final ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
			mOsmv.setTileSource(tileSource);
		} catch (final IllegalArgumentException ignore) {
		}
		if (prefs.getBoolean(PREFS_SHOW_LOCATION, false)) {
			this.mMyLocationOverlay.enableMyLocation();
		}
		if (prefs.getBoolean(PREFS_SHOW_COMPASS, false)) {
			this.mMyLocationOverlay.enableCompass();
		}

		// While this activity was paused the user may have deleted the selected
		// track. In that case the map overlay needs to be cleared:
		// Track track = mapOverlay.getSelectedTrack();
		// if (track != null && !providerUtils.trackExists(track.getId())) {
		// // The recording track must have been deleted meanwhile.
		// mapOverlay.setSelectedTrack(null);
		// mapView.invalidate();
		// }
	}

	@Override
	protected void onStop() {
		super.onStop();
		instance = null;
//		stopTripStatsController();

	}

	private void stopTripStatsController() {
//		if (mTripStatsController != null) {
//			mTripStatsController.stop();
//		}
	}

	protected void onPause() {
//		Toast.makeText(
//				SatNavActivity.this,
//				mOsmv.getScrollX() + ", " +mOsmv.getScrollY()
//				+ " - " + mOsmv.getZoomLevel()
////				"Item '" + item.mTitle + "' (index=" + index
////						+ ") got single tapped up"
//				, Toast.LENGTH_LONG).show();
		// Called when activity is going into the background, but has not (yet)
		// been
		// killed. Shouldn't block longer than approx. 2 seconds.
		Log.d(OpenSatNavConstants.LOG_TAG, "Pausing OSN");
//		stopTripStatsController();
//		unregisterContentObservers();
		instance = null;

		final SharedPreferences.Editor edit = prefs.edit();
		edit.putString(PREFS_TILE_SOURCE, mOsmv.getTileProvider().getTileSource().name());
		edit.putInt(PREFS_SCROLL_X, mOsmv.getScrollX());
		edit.putInt(PREFS_SCROLL_Y, mOsmv.getScrollY());
		edit.putInt(PREFS_ZOOM_LEVEL, mOsmv.getZoomLevel());
		edit.putBoolean(PREFS_SHOW_LOCATION,mMyLocationOverlay.isMyLocationEnabled());
		edit.putBoolean(PREFS_SHOW_COMPASS, mMyLocationOverlay.isCompassEnabled());
		edit.commit();
		
		this.mMyLocationOverlay.disableMyLocation();
		this.mMyLocationOverlay.disableCompass();
		
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		Log.v(OpenSatNavConstants.LOG_TAG, "onDestroy()");
//		this.mOsmv.closeDB();
		// routeInstructionsService.stopTTS()
//		Intent svc = new Intent(this, RouteInstructionsService.class);
//		stopService(svc);
		instance = null;

		super.onDestroy();
	}

//	@Override
//	public Object onRetainNonConfigurationInstance() {
//		Object[] retainables = new Object[3];
//		retainables[0] = mTripStatsController.getAllStatistics();
//		retainables[1] = mMyLocationOverlay;
//		retainables[2] = routeOverlay;
//		return retainables;
//	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			if (viewingTripStatistics) {
//				viewingTripStatistics = false;
//				showTripStatistics(false);
//				return true;
//			} else {
//				TripStatisticsService.stop(this);
//				return super.onKeyDown(keyCode, event);
//			}
//		}
		return super.onKeyDown(keyCode, event);
	}

	void setViewingTripStats(boolean flag) {
		viewingTripStatistics = flag;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			java.lang.String key) {
		if (key.contentEquals("map_style")) {
//			SatNavActivity.this.mOsmv
//					.setRenderer(OpenStreetMapRendererInfo
//							.getFromPrefName(sharedPreferences.getString(key,
//									"mapnik")));
			final String tileSourceName = sharedPreferences.getString(key,
									"mapnik");
			try {
				final ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
				mOsmv.setTileSource(tileSource);
			} catch (final IllegalArgumentException ignore) {
			}
		}
	}

	// All the new stuff is here - this really needs to go somewhere else!
	/**
	 * A runnable that updates the track from the provider (looking for points
	 * added after "lastSeenLocationId".
	 */
	private final Runnable updateTrackRunnable = new Runnable() {
		@Override
		public void run() {
//			Log.v(OpenSatNavConstants.LOG_TAG, "updateTrackRunnable Start");
//
//			if (selectedTrack == null) {
//				return;
//			}
//			Cursor cursor = null;
//			try {
//				cursor = providerUtils.getLocationsCursor(recordingTrackId,
//						lastSeenLocationId + 1,
//						OSMConstants.MAX_DISPLAYED_TRACK_POINTS
//								- selectedTrack.getLocations().size(), true);
//				if (cursor != null) {
//					if (cursor.moveToLast()) {
//						final int idColumnIdx = cursor
//								.getColumnIndexOrThrow(BaseColumns._ID);
//						do {
//							lastSeenLocationId = cursor.getLong(idColumnIdx);
//							Location location = providerUtils
//									.createLocation(cursor);
//							if (location != null) {
//								selectedTrack.addLocation(location);
//							}
//						} while (cursor.moveToPrevious());
//					}
//				}
//			} catch (RuntimeException e) {
//				Log.w(OpenSatNavConstants.LOG_TAG,
//						"Caught an unexpected exception: ", e);
//			} finally {
//				if (cursor != null) {
//					cursor.close();
//				}
//				mOsmv.postInvalidate();
//
//			}
		}
	};

	/**
	 * A runnable that restores all track points from the provider.
	 */
	private Runnable restoreTrackRunnable = new Runnable() {
		@Override
		public void run() {
//			if (selectedTrack == null) {
//				return;
//			}
//			lastSeenLocationId = providerUtils.getTrackPoints(selectedTrack,
//					OSMConstants.MAX_DISPLAYED_TRACK_POINTS);
//			mOsmv.postInvalidate();
		}
	};

	/**
	 * A runnable that restores all waypoints from the provider.
	 */
	private final Runnable restoreWaypointsRunnable = new Runnable() {
		@Override
		public void run() {
//			if (selectedTrack == null) {
//				return;
//			}
//
//			Cursor cursor = null;
//			traceOverlay.clearWaypoints();
//			try {
//				// We will silently drop extra waypoints to make the app
//				// responsive.
//				// TODO: Try to only load the waypoints in the view port.
//				cursor = providerUtils.getWaypointsCursor(
//						selectedTrack.getId(), 0,
//						OSMConstants.MAX_DISPLAYED_WAYPOINTS_POINTS);
//				if (cursor != null) {
//					if (cursor.moveToFirst()) {
//						do {
//							Waypoint wpt = providerUtils.createWaypoint(cursor);
//							traceOverlay.addWaypoint(wpt);
//						} while (cursor.moveToNext());
//					}
//				}
//			} catch (RuntimeException e) {
//				Log.w(OpenSatNavConstants.LOG_TAG,
//						"Caught an unexpected exception.", e);
//			} finally {
//				if (cursor != null) {
//					cursor.close();
//				}
//			}
//			mOsmv.postInvalidate();
		}
	};

	/**
	 * A runnable intended to be posted to the updateTrackThread after the
	 * selected track changes. It will post to the ui thread to update the
	 * screen elements and move the map to show the selected track.
	 */
	private final Runnable setSelectedTrackRunnable = new Runnable() {
		@Override
		public void run() {
//			uiHandler.post(new Runnable() {
//				public void run() {
//					showTrack(selectedTrack);
//					traceOverlay.setSelectedTrack(selectedTrack);
//					mOsmv.invalidate();
//					/*
//					 * busyPane.setVisibility(View.GONE); updateOptionsButton();
//					 */
//				}
//			});
		}
	};

//	public void showTrack(Track track) {
//		if (track == null || mOsmv == null || track.getNumberOfPoints() < 2) {
//			return;
//		}
//		int latSpanE6 = track.getTop() - track.getBottom();
//		int lonSpanE6 = track.getRight() - track.getLeft();
//		if (latSpanE6 > 0 && latSpanE6 < 180E6 && lonSpanE6 > 0
//				&& lonSpanE6 < 180E6) {
//			autoFollowing = false;
//			GeoPoint center = new GeoPoint(track.getBottom() + latSpanE6 / 2,
//					track.getLeft() + lonSpanE6 / 2);
//			if (MyTracksUtils.isValidGeoPoint(center)) {
//				mOsmv.getController().zoomToSpan(latSpanE6, lonSpanE6);
//			}
//		}
//	}

	/**
	 * Sets the selected track and zoom and pan the map so that it is visible.
	 * 
	 * @param trackId
	 *            a given track id
	 */
	public void setSelectedTrack(final long trackId) {
//		Log.v(OpenSatNavConstants.LOG_TAG, "SatNavActivity.setSelectedTrack("
//				+ trackId + ")");
//		if (selectedTrack != null && selectedTrack.getId() == trackId) {
//			// Selected track did not change, nothing to do.
//			traceOverlay.setSelectedTrack(selectedTrack);
//			mOsmv.invalidate();
//
//			return;
//		}
//		if (trackId < 0) {
//			// Remove selection.
//			selectedTrack = null;
//			traceOverlay.setSelectedTrack(null);
//			traceOverlay.clearWaypoints();
//
//			mOsmv.invalidate();
//			return;
//		}
//
//		selectedTrack = providerUtils.getTrack(trackId);
//		updateTrackHandler.post(restoreTrackRunnable);
//		updateTrackHandler.post(restoreWaypointsRunnable);
//		updateTrackHandler.post(setSelectedTrackRunnable);
	}

	/**
	 * Just like showDialog, but will catch a BadTokenException that sometimes
	 * (very rarely) gets thrown. This might happen if the user hits the "back"
	 * button immediately after sending tracks to google.
	 * 
	 * @param id
	 *            the dialog id
	 */
	public void showDialogSafely(final int id) {
		runOnUiThread(new Runnable() {
			public void run() {
				try {
					showDialog(id);
				} catch (BadTokenException e) {
					Log.w(OpenSatNavConstants.LOG_TAG,
							"Could not display dialog with id " + id, e);
				} catch (IllegalStateException e) {
					Log.w(OpenSatNavConstants.LOG_TAG,
							"Could not display dialog with id " + id, e);
				}
			}
		});
	}

	/**
	 * Registers the content observer for the map overlay.
	 */
	private void registerContentObservers() {
//		getContentResolver().registerContentObserver(
//				TrackPointsColumns.CONTENT_URI, false /* notifyForDescendents */,
//				observer);
		// getContentResolver().registerContentObserver(
		// WaypointsColumns.CONTENT_URI, false /* notifyForDescendents */,
		// waypointObserver);
	}

	/**
	 * Unregisters the content observer for the map overlay.
	 */
	private void unregisterContentObservers() {
//		getContentResolver().unregisterContentObserver(observer);
		// getContentResolver().unregisterContentObserver(waypointObserver);
	}

	/*
	 * @Override public void onInit(int status) { // TODO Auto-generated method
	 * stub
	 * 
	 * }
	 */
	public void sendtoosm(String title, String visibility) {
//		try {
//			String username = prefs.getString(
//					getString(R.string.pref_username_key), null);
//
//			String password = prefs.getString(
//					getString(R.string.pref_password_key), null);
//
//			if (username == null || password == null) {
//
//				displayToast(R.string.contribute_error_enter_osm_login_details);
//
//			} else {
//
//				// ARG! This is a hack!
//				SharedPreferences preferences = getSharedPreferences(
//						OpenSatNavConstants.SETTINGS_NAME, 0);
//				if (preferences != null) {
//					long selectedTrackId = preferences.getLong(
//							OpenSatNavConstants.SELECTED_TRACK, -1);
//					if (selectedTrackId >= 0) {
//						setSelectedTrack(selectedTrackId);
//					}
//
//				}
//				OSMUploader.uploadAsync(this.getApplicationContext(),
//						selectedTrack.getLocations(), username, password,
//						title, visibility);
//				String resultsTextFormat = getString(R.string.contribute_track_uploaded);
//				String resultsText = String.format(resultsTextFormat, title);
//				displayToast(resultsText);
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		/*
		 * case DIALOG_PROGRESS: progressDialog = new ProgressDialog(this);
		 * progressDialog.setIcon(android.R.drawable.ic_dialog_info);
		 * progressDialog.setTitle(getString(R.string.progress_title));
		 * progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		 * progressDialog.setMessage(""); progressDialog.setMax(100);
		 * progressDialog.setProgress(10); return progressDialog; case
		 * DIALOG_IMPORT_PROGRESS: importProgressDialog = new
		 * ProgressDialog(this);
		 * importProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
		 * importProgressDialog.setTitle(getString(R.string.progress_title));
		 * importProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		 * importProgressDialog.setMessage(
		 * getString(R.string.import_progress_message)); return
		 * importProgressDialog; case DIALOG_WRITE_PROGRESS: writeProgressDialog
		 * = new ProgressDialog(this);
		 * writeProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
		 * writeProgressDialog.setTitle(getString(R.string.progress_title));
		 * writeProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		 * writeProgressDialog.setMessage(
		 * getString(R.string.write_progress_message)); return
		 * writeProgressDialog;
		 */
//		case ContributeConstants.DIALOG_SEND_TO_OSM:
//			sendToOSMDialog = new SendToOSMDialog(this);
//			return sendToOSMDialog;
			/*
			 * case DIALOG_SEND_TO_GOOGLE_RESULT: AlertDialog.Builder builder =
			 * new AlertDialog.Builder(this);
			 * builder.setIcon(android.R.drawable.ic_dialog_info);
			 * builder.setTitle("Title"); builder.setMessage("Message");
			 * builder.setPositiveButton(getString(R.string.ok), null);
			 * builder.setNeutralButton(getString(R.string.share_map), new
			 * DialogInterface.OnClickListener() { public void
			 * onClick(DialogInterface dialog, int which) {
			 * shareLinkToMyMap(sendToMyMapsMapId); dialog.dismiss(); } });
			 * sendToGoogleResultDialog = builder.create(); return
			 * sendToGoogleResultDialog; case DIALOG_CHART_SETTINGS:
			 * chartSettingsDialog = new ChartSettingsDialog(this); return
			 * chartSettingsDialog;
			 */
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);

		switch (id) {
//		case ContributeConstants.DIALOG_SEND_TO_OSM:
//			// resetSendToGoogleStatus();
//			break;
		/*
		 * case DIALOG_SEND_TO_GOOGLE_RESULT: boolean success =
		 * sendToMyMapsSuccess && sendToDocsSuccess;
		 * sendToGoogleResultDialog.setTitle( success ? R.string.success :
		 * R.string.error); sendToGoogleResultDialog.setIcon(success ?
		 * android.R.drawable.ic_dialog_info :
		 * android.R.drawable.ic_dialog_alert);
		 * sendToGoogleResultDialog.setMessage(getMapsResultMessage());
		 * 
		 * boolean canShare = sendToMyMapsMapId != null; View share =
		 * sendToGoogleResultDialog.findViewById(android.R.id.button3); if
		 * (share != null) { share.setVisibility(canShare ? View.VISIBLE :
		 * View.GONE); } break; case DIALOG_CHART_SETTINGS:
		 * Log.d(MyTracksConstants.TAG, "MyTracks.onPrepare chart dialog");
		 * chartSettingsDialog.setup(chartActivity); break;
		 */
		}
	}

	private void showRefreshSpinner(boolean isRefreshing) {
		busstopButton.setVisibility(isRefreshing ? View.GONE : View.VISIBLE);
		refButton.setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
	}
}
