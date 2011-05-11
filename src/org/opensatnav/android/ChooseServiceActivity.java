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

import org.andnav.osm.util.GeoPoint;
import org.opensatnav.android.util.FormatHelper;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ChooseServiceActivity extends ListActivity {


	@Override
	public void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		GeoPoint from = GeoPoint.fromDoubleString(getIntent().getStringExtra("fromLocation"), ',');
		setTitle("servicio");
		
		final LocationAdapter la = new LocationAdapter(from);
		setListAdapter(la);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long position) {
//				Intent data = getIntent();
////				data.putExtra("location", la.getLocation((int) position).toString());
//				setResult(RESULT_OK, data);
//				finish();

			}

		});
	}

	protected class LocationAdapter extends BaseAdapter {

		GeoPoint from;
		Bundle b = getIntent().getBundleExtra("locations");
		String[] locationInfo = b.getStringArray("info");
		String[] locationNames = b.getStringArray("names");
//		final int[] locationLats = b.getIntArray("latitudes");
//		final int[] locationLongs = b.getIntArray("longitudes");

		public LocationAdapter(GeoPoint from) {
			this.from = from;
		}

		@Override
		public int getCount() {
			return locationNames.length;
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
			LinearLayout mainView = new LinearLayout(ChooseServiceActivity.this);
			mainView.setOrientation(LinearLayout.VERTICAL);
			
			TextView placeView = new TextView(ChooseServiceActivity.this);
			TextView infoView = new TextView(ChooseServiceActivity.this);
			TextView distanceView = new TextView(ChooseServiceActivity.this);
			//add name
			String place = locationNames[position];
			// add unnamed text for places that need it
			if (place==null || place.length() == 0)
				place = (String) ChooseServiceActivity.this.getResources().getText(R.string.unnamed_place);
			// add location type
			String info = locationInfo[position];
//			info = info.substring(0,1).toUpperCase()+info.substring(1);
			// add distance away
//			String distance = 
//				new FormatHelper(getBaseContext()).formatDistanceFuzzy
//				(from.distanceTo(new GeoPoint(locationLats[position], locationLongs[position])))
//				+ " " + ChooseServiceActivity.this.getResources().getText(R.string.away);
			
			placeView.setText(place);
			placeView.setTextSize(20);
			placeView.setTextColor(Color.WHITE);
			infoView.setText(info);
//			distanceView.setText(distance);
			
			mainView.addView(placeView, 0);
			mainView.addView(infoView, 1);
			mainView.addView(distanceView, 2);
			
			return mainView;
		}

//		public GeoPoint getLocation(int position) {
//			return new GeoPoint(locationLats[position], locationLongs[position]);
//
//		}

	}

	
}
