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
package cl.droid.transantiago.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.opensatnav.android.services.GeoCoder;
import org.osmdroid.util.GeoPoint;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SearchRecentSuggestions;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cl.droid.transantiago.MySuggestionProvider;
import cl.droid.transantiago.R;
import cl.droid.transantiago.service.TransantiagoGeoCoder;
import cl.droid.utils.PreferenceHelper;

public class TransChooseServiceActivity extends ListActivity {

	protected ProgressDialog progress;
	Bundle b;
	String[] locationInfo;
	String[] locationNames;
	ImageView ads;
	protected GeoPoint from;
	BitmapFactory.Options bmOptions;
	LocationAdapter la;
	String paradero;
	String description;
	private ListView mListView;
	private Context mContext;
	private PreferenceHelper mPreferenceHelper;

	@Override
	public void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.services_list);

		mContext = getBaseContext();
		mPreferenceHelper = new PreferenceHelper(mContext);
		mPreferenceHelper.setLoadstop();

		ads = (ImageView) this.findViewById(R.id.ads);
		// Refresh title button
		// findViewById(R.id.title_btn_refresh).setOnClickListener(new
		// View.OnClickListener() {
		// public void onClick(View v) {
		// TransChooseServiceActivity.this.launch(paradero, description);
		// }
		// });

		bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = 1;

		from = GeoPoint.fromDoubleString(
				getIntent().getStringExtra("fromLocation"), ',');
		paradero = getIntent().getStringExtra("paradero");

		locationNames = null;

		// Set the title
		// ((TextView) findViewById(R.id.title_text)).setText(paradero);
		if (getIntent().hasExtra("description")) {
			description = getIntent().getStringExtra("description");
			((TextView) findViewById(R.id.description)).setText(description);
		} else {
			description = "";
		}
		la = new LocationAdapter(from);
		mListView = getListView();

		this.launch(paradero, description);

	}

	protected class LocationAdapter extends BaseAdapter {

		GeoPoint from;

		public LocationAdapter(GeoPoint from) {
			this.from = from;
		}

		@Override
		public int getCount() {
			if (locationNames != null)
				return locationNames.length;
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout mainView = new LinearLayout(
					TransChooseServiceActivity.this);
			mainView.setOrientation(LinearLayout.VERTICAL);

			TextView placeView = new TextView(TransChooseServiceActivity.this);
			TextView infoView = new TextView(TransChooseServiceActivity.this);
			TextView distanceView = new TextView(
					TransChooseServiceActivity.this);
			// add name
			String place = locationNames[position];
			// add unnamed text for places that need it
			if (place == null || place.length() == 0)
				place = (String) TransChooseServiceActivity.this.getResources()
						.getText(R.string.unnamed_place);
			// add location type
			String info = locationInfo[position];

			placeView.setText(place);
			placeView.setTextSize(20);
			infoView.setText(info);

			mainView.addView(placeView, 0);
			mainView.addView(infoView, 1);

			return mainView;
		}

		public String getServicio(int position) {
			return locationNames[position];

		}

	}

	private Bitmap loadImage(final String URL,
			final BitmapFactory.Options options) {
		final Handler handler = new Handler() {
		};
		new Thread(new Runnable() {
			public void run() {
				InputStream in = null;
				try {
					in = OpenHttpConnection(URL);
					final Bitmap bitmap = BitmapFactory.decodeStream(in, null,
							options);
					in.close();

					handler.post(new Runnable() {
						public void run() {
							if (ads != null && bitmap != null) {
								ads.setImageBitmap(bitmap);
							}
						}
					});
				} catch (IOException e1) {
					handler.sendEmptyMessage(0);
				} catch (Exception e1) {
					handler.sendEmptyMessage(0);
				}
				// return bitmap;
			}
		}).start();
		return null;
	}

	private InputStream OpenHttpConnection(String strURL) throws IOException {
		InputStream inputStream = null;
		URL url = new URL(strURL);
		URLConnection conn = url.openConnection();

		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				inputStream = httpConn.getInputStream();
			}
		} catch (Exception ex) {
		}
		return inputStream;
	}

	protected Bundle locations;

	public void launch(final String paradero, final String description) {
		// final String paradero = item.mTitle;
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
				TransChooseServiceActivity.this,
				MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
		suggestions.saveRecentQuery(paradero, null);

		showRefreshSpinner(true);
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (progress != null && progress.isShowing())
					try {
						progress.dismiss();
						// backgroundThreadComplete = true;
					} catch (IllegalArgumentException e) {
						// if orientation change, thread continue but the dialog
						// cannot be dismissed without exception
					}
				if (locations != null && locations.containsKey("names")
						&& locations.getStringArray("names").length > 0) {
					locationInfo = locations.getStringArray("info");
					locationNames = locations.getStringArray("names");
					setListAdapter(la);

					if (locations.containsKey("ads"))
						loadImage(locations.getString("ads"), bmOptions);

				} else if (locations != null && locations.containsKey("names")
						&& locations.getStringArray("names").length == 0)
					Toast.makeText(
							TransChooseServiceActivity.this,
							String.format(TransChooseServiceActivity.this
									.getResources().getText(
									// R.string.could_not_find_poi
											R.string.place_not_found)
									.toString(), "paradero")
							// + " " + stringValue
							, Toast.LENGTH_LONG).show();
				if (locations == null)
					Toast.makeText(
							TransChooseServiceActivity.this,
							TransChooseServiceActivity.this.getResources()
									.getText(
									// R.string.could_not_find_poi
											R.string.error_no_server_conn)
									.toString(), Toast.LENGTH_LONG).show();

				// TransChooseLocationServiceActivity.this.finish();
				showRefreshSpinner(false);
			}
		};
		new Thread(new Runnable() {
			public void run() {
				TransantiagoGeoCoder geoCoder = null;

				geoCoder = new TransantiagoGeoCoder();
				locations = geoCoder.queryService(paradero, from,
						GeoCoder.IN_AREA, 25, TransChooseServiceActivity.this);
				handler.sendEmptyMessage(0);

			}
		}).start();

	}

	private void showRefreshSpinner(boolean isRefreshing) {
		// findViewById(R.id.title_btn_refresh).setVisibility(isRefreshing ?
		// View.GONE : View.VISIBLE);
		// findViewById(R.id.title_refresh_progress).setVisibility(isRefreshing
		// ? View.VISIBLE : View.GONE);
	}

}
