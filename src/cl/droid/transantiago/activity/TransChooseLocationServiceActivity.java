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

import org.opensatnav.android.services.GeoCoder;
import org.opensatnav.android.util.FormatHelper;
import org.osmdroid.util.GeoPoint;

import cl.droid.transantiago.MySuggestionProvider;
import cl.droid.transantiago.R;
import cl.droid.transantiago.R.layout;
import cl.droid.transantiago.R.string;
import cl.droid.transantiago.service.TransantiagoGeoCoder;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class TransChooseLocationServiceActivity extends ListActivity {

	protected Bundle locations;
	protected GeoPoint from;
	protected String to;
	protected int selectedPoi = -1;
	protected ProgressDialog progress;
	
	private ListView mListView;
	@Override
	public void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stops_list);
		Intent intent = getIntent();
//		Toast.makeText(TransChooseLocationServiceActivity.this, "onCreate", Toast.LENGTH_LONG).show();
		setTitle(this.getResources().getText(R.string.choose_location_busstop));
		
//		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // from click on search results
//            Dictionary.getInstance().ensureLoaded(getResources());
//            String word = intent.getDataString();
//            launchServices(word);
//            Dictionary.Word theWord = Dictionary.getInstance().getMatches(word).get(0);
//            launchWord(theWord);
//            finish();
//        }else 
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
//            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
//                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
//            suggestions.saveRecentQuery(query, null);
            
            from = new GeoPoint(0,0);
            launchServices(query.toUpperCase());
//            mTextView.setText(getString(R.string.search_results, query));
//            WordAdapter wordAdapter = new WordAdapter(Dictionary.getInstance().getMatches(query));
//            mList.setAdapter(wordAdapter);
//            mList.setOnItemClickListener(wordAdapter);
//            finish();
        } else {
		
		from = GeoPoint.fromDoubleString(getIntent().getStringExtra("fromLocation"), ',');
		final LocationAdapter la = new LocationAdapter(from);
//		mListView = (ListView)this.findViewById(android.R.id.list);
		setListAdapter(la);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, final long position) {
				progress = ProgressDialog.show(
						TransChooseLocationServiceActivity.this, TransChooseLocationServiceActivity.this.getResources().getText(
								R.string.please_wait), TransChooseLocationServiceActivity.this.getResources().getText(
								R.string.searching), true, true);
				final String paradero = la.getParadero((int)position);
				
				to = la.getLocation((int) position).toString();
				launchServices(paradero);

			}

		});
        }
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Intent intent = getIntent();
//		intent.putExtra("location", to);
		setResult(RESULT_OK, intent);
		finish();
	}
	private void launchServices(final String paradero) {
		Intent intent = new Intent(TransChooseLocationServiceActivity.this,
				cl.droid.transantiago.activity.TransChooseServiceActivity.class);
		intent.putExtra("fromLocation", from.toDoubleString());
		//intent.putExtra("locations", locations);
		intent.putExtra("paradero", paradero);
		//
		String urlstring = "http://m.ibus.cl/index.jsp?paradero="+paradero+"&servicio=&boton.x=0&boton.y=0";
		//Log.i(OpenSatNavConstants.LOG_TAG, urlstring);
		intent.putExtra("url", urlstring);
		startActivity(intent);
		finish();
		
	}
	protected class LocationAdapter extends BaseAdapter {

		GeoPoint from;
		Bundle b = getIntent().getBundleExtra("locations");
		String[] locationInfo = b.getStringArray("info");
		String[] locationNames = b.getStringArray("names");
		final int[] locationLats = b.getIntArray("latitudes");
		final int[] locationLongs = b.getIntArray("longitudes");

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
			LinearLayout mainView = new LinearLayout(TransChooseLocationServiceActivity.this);
			mainView.setOrientation(LinearLayout.VERTICAL);
			
			TextView placeView = new TextView(TransChooseLocationServiceActivity.this);
			TextView infoView = new TextView(TransChooseLocationServiceActivity.this);
			TextView distanceView = new TextView(TransChooseLocationServiceActivity.this);
			//add name
			String place = locationNames[position];
			// add unnamed text for places that need it
			if (place==null || place.length() == 0)
				place = (String) TransChooseLocationServiceActivity.this.getResources().getText(R.string.unnamed_place);
			// add location type
			String info = locationInfo[position];
//			info = info.substring(0,1).toUpperCase()+info.substring(1);
			// add distance away
			String distance = 
				new FormatHelper(getBaseContext()).formatDistanceFuzzy
				(from.distanceTo(new GeoPoint(locationLats[position], locationLongs[position])))
				+ " " + TransChooseLocationServiceActivity.this.getResources().getText(R.string.away);
			
			placeView.setText(place);
			placeView.setTextSize(20);
			placeView.setTextColor(Color.WHITE);
			infoView.setText(info);
			distanceView.setText(distance);
			
			mainView.addView(placeView, 0);
			mainView.addView(infoView, 1);
			mainView.addView(distanceView, 2);
			
			return mainView;
		}

		public GeoPoint getLocation(int position) {
			return new GeoPoint(locationLats[position], locationLongs[position]);

		}
		public String getParadero(int position) {
			return locationNames[position];

		}

	}

	
}
