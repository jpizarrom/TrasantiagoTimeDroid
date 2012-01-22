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
import cl.droid.utils.PreferenceHelper;

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

	/**
	 * Singleton instance
	 */
	private static SatNavActivity instance = null;


//	protected Location currentLocation;

	protected SharedPreferences prefs;

	private RelativeLayout layout;
	private Context mContext;
	private PreferenceHelper mPreferenceHelper;

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
	        
	        description.setMovementMethod(new ScrollingMovementMethod());
	        
	        item = null;
	        
	        mClose.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View v) {
					popup.setVisibility(View.GONE);
			    }
			});
	        mLaunch.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View v) {
					SatNavActivity.this.launch(title.getText().toString());
			    }
			});
	    }
		}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		BugReportExceptionHandler.register(this);
		super.onCreate(savedInstanceState);
		
		mContext = getBaseContext();
		mPreferenceHelper = new PreferenceHelper(mContext);
		
		setContentView(R.layout.map);

		((TextView) findViewById(R.id.title_text)).setText("Map");
			
		mResourceProxy = new ResourceProxyImpl(getApplicationContext());
		
		final RelativeLayout rl = (RelativeLayout) findViewById(R.id.map_rl);

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

		mOsmv.getController().setZoom(prefs.getInt(PREFS_ZOOM_LEVEL, 14));
		mOsmv.scrollTo(prefs.getInt(PREFS_SCROLL_X, -823161), prefs.getInt(PREFS_SCROLL_Y, 413748));


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

			this.mMyLocationOverlay = new MyLocationOverlay(this.getBaseContext(), this.mOsmv){	
			};
			this.mOsmv.setBuiltInZoomControls(true);
			this.mOsmv.setMultiTouchControls(true);
			this.mOsmv.getOverlays().add(this.mMyLocationOverlay);
			
		}

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

		// Read shared preferences and register change listener:
		SharedPreferences preferences = getSharedPreferences(
				OpenSatNavConstants.SETTINGS_NAME, 0);
		if (preferences != null) {
			preferences.registerOnSharedPreferenceChangeListener(this);
		}

	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		
		MenuItem prefsMenuItem = pMenu.add(0, MENU_PREFERENCES, Menu.NONE,
				R.string.preferences);
		prefsMenuItem.setIcon(android.R.drawable.ic_menu_preferences);
		
		// Put overlay items next
		this.mOsmv.getOverlayManager().onCreateOptionsMenu(pMenu, MENU_LAST_ID, mOsmv);
//		this.mMyLocationOverlay.onCreateOptionsMenu(pMenu, MENU_LAST_ID, mOsmv);
		
		MenuItem aboutMenuItem = pMenu.add(0, MENU_ABOUT, Menu.NONE,
				R.string.about);
		aboutMenuItem.setIcon(android.R.drawable.ic_menu_info_details);
		
		return true;
	}

	private void onSearchOnMap(){
		mPreferenceHelper.setLoadstops();
		
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

		case MENU_RENDERER_ID:
			this.mOsmv.invalidate();
			return true;

		case MENU_PREFERENCES:
			Intent intent = new Intent(this,
					org.opensatnav.android.ConfigurationActivity.class);
			startActivityForResult(intent, MENU_PREFERENCES);

			return true;
		case MENU_ABOUT:
			Intent intent1 = new Intent(this, org.openintents.about.About.class);
			startActivityForResult(intent1, MENU_ABOUT);

			return true;

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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.mOsmv.getOverlayManager().onPrepareOptionsMenu(menu, MENU_LAST_ID, mOsmv);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onResume() {
		// Called when the current activity is being displayed or re-displayed
		// to the user.
		Log.d(OpenSatNavConstants.LOG_TAG, "onResume OSN");

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
	}

	protected void onPause() {
		// Called when activity is going into the background, but has not (yet)
		// been
		// killed. Shouldn't block longer than approx. 2 seconds.
		Log.d(OpenSatNavConstants.LOG_TAG, "onPause OSN");

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

	private void showRefreshSpinner(boolean isRefreshing) {
		busstopButton.setVisibility(isRefreshing ? View.GONE : View.VISIBLE);
		refButton.setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
	}
}
