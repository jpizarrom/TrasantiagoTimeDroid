package cl.droid.transantiago.activity;

import java.util.List;

import org.opensatnav.android.services.GeoCoder;
import org.osmdroid.util.GeoPoint;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import cl.droid.transantiago.R;
import cl.droid.transantiago.service.TransantiagoGeoCoder;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MapActivity extends FragmentActivity {
	private static final String MAP_FRAGMENT_TAG = "org.osmdroid.MAP_FRAGMENT_TAG";
	private ItemizedOverlay<OverlayItem> mItemizedOverlay;
	private SupportMapFragment mapFragment;
	private GoogleMap mMap;
	private List<Overlay> mapOverlays;
	private ImageButton busstopButton;
	private ProgressBar refButton;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.main);

		FragmentManager fm = this.getSupportFragmentManager();

		mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);

		mMap = mapFragment.getMap();

		LatLng latLng = new LatLng(-33.456472, -70.668911);
		mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

		/* Fetch busstop buttom */
		{
			{
				busstopButton = (ImageButton) findViewById(R.id.busstop_btn);

				busstopButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						MapActivity.this.showRefreshSpinner(true);
						onSearchOnMap();
					}
				});

			}

			{
				refButton = (ProgressBar) findViewById(R.id.busstop_refresh_progress);

			}
		}

		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				launch(marker.getTitle(), marker.getSnippet(),
						marker.getPosition());
			}
		});

	}

	private void showRefreshSpinner(boolean isRefreshing) {
		busstopButton.setVisibility(isRefreshing ? View.GONE : View.VISIBLE);
		refButton.setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
	}

	private void onSearchOnMap() {
		// mPreferenceHelper.setLoadstops();
		// if (!this.isOnline()){
		// Toast.makeText(this, this.getResources().getText(
		// R.string.error_no_inet_conn), Toast.LENGTH_LONG).show();
		// SatNavActivity.this.showRefreshSpinner(false);
		// } else
		// getLocations(" ");

		LatLng from = mMap.getCameraPosition().target;
		LatLngBounds curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
		PlacesTask placesTask = new PlacesTask();
		LatLng[] l = new LatLng[3];
		l[0] = from;
		l[1] = curScreen.northeast;
		l[2] = curScreen.southwest;
		placesTask.execute(l);
	}

	public void launch(final String paradero, final String description,
			LatLng latLng) {
		Intent intent = new Intent(MapActivity.this,
				cl.droid.transantiago.activity.TransChooseServiceActivity.class);
		GeoPoint from = new GeoPoint(latLng.latitude, latLng.longitude);
		intent.putExtra("fromLocation", from.toDoubleString());
		intent.putExtra("paradero", paradero);
		intent.putExtra("description", description);

		String urlstring = "http://m.ibus.cl/index.jsp?paradero=" + paradero
				+ "&servicio=&boton.x=0&boton.y=0";
		intent.putExtra("url", urlstring);
		startActivityForResult(intent, 0);
	}

	private class PlacesTask extends AsyncTask<LatLng[], Void, Bundle> {

		private GeoPoint from;

		@Override
		protected Bundle doInBackground(LatLng[]... params) {
			LatLng[] l = params[0];
			LatLng lfrom = l[0];
			LatLng northeast = l[1];
			LatLng southwest = l[2];

			from = new GeoPoint(lfrom.latitude, lfrom.longitude);
			String bbox = northeast.longitude + "," + northeast.latitude + ","
					+ southwest.longitude + "," + southwest.latitude;

			GeoCoder geoCoder = null;
			Bundle locations = (new TransantiagoGeoCoder()).query(" ", from,
					GeoCoder.IN_AREA, 50, MapActivity.this, bbox);
			return locations;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Bundle locations) {
			mMap.clear();

			if (locations != null && locations.containsKey("names")
					&& locations.getStringArray("names").length > 0) {
				Intent intent;

				intent = new Intent(
						MapActivity.this,
						cl.droid.transantiago.activity.TransChooseLocationServiceActivity.class);

				intent.putExtra("fromLocation", from.toDoubleString());
				intent.putExtra("locations", locations);

				if (locations.getInt("size") > 0) {
					int size = locations.getInt("size");
					Toast.makeText(
							MapActivity.this,
							String.format(MapActivity.this.getResources()
									.getText(
									// R.string.could_not_find_poi
											R.string.place_found).toString(),
									size, "paradero")
							// + " " + size
							, Toast.LENGTH_LONG).show();
					String[] locationInfo = locations.getStringArray("info");
					String[] locationNames = locations.getStringArray("names");
					final int[] locationLats = locations
							.getIntArray("latitudes");
					final int[] locationLongs = locations
							.getIntArray("longitudes");
					for (int i = 0; i < size; i++) {

						// Creating a marker
						MarkerOptions markerOptions = new MarkerOptions();

						// // Getting a place from the places list
						// HashMap<String, String> hmPlace = list.get(i);

						// Getting latitude of the place
						double lat = locationLats[i] / 1E6;

						// Getting longitude of the place
						double lng = locationLongs[i] / 1E6;

						// Getting name
						String name = locationNames[i];

						// // Getting vicinity
						// String vicinity = hmPlace.get("vicinity");

						LatLng latLng = new LatLng(lat, lng);

						// Setting the position for the marker
						markerOptions.position(latLng);

						// // Setting the title for the marker.
						// // This will be displayed on taping the marker
						markerOptions.title(name);
						markerOptions.snippet(locationInfo[i]);

						// Placing a marker on the touched position
						Marker m = mMap.addMarker(markerOptions);
					}
					// SatNavActivity.this.mOsmv.invalidate();
				}
			}
			if (locations != null && locations.containsKey("names")
					&& locations.getStringArray("names").length == 0)
				Toast.makeText(
						MapActivity.this,
						String.format(
								MapActivity.this.getResources()
										.getText(R.string.place_not_found)
										.toString(), "paradero"),
						Toast.LENGTH_LONG).show();
			if (locations == null)
				Toast.makeText(
						MapActivity.this,
						MapActivity.this.getResources()
								.getText(R.string.error_no_server_conn)
								.toString(), Toast.LENGTH_LONG).show();

			MapActivity.this.showRefreshSpinner(false);

		}
	}
}
