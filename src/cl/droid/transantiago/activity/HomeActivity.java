package cl.droid.transantiago.activity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.opensatnav.android.SatNavActivity;
import cl.droid.transantiago.R;
import cl.droid.transantiago.service.TransantiagoGeoCoder;
import cl.droid.utils.Changelog;
import cl.droid.utils.PreferenceHelper;
import cl.droid.utils.Utils;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {
	private static final int MENU_SEARCH = 0;
	private PreferenceHelper mPreferenceHelper;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		mContext = getBaseContext();
		mPreferenceHelper = new PreferenceHelper(mContext);
		
		boolean appUnchanged = Changelog.show(this);
		((TextView) findViewById(R.id.title_text)).setText("Home");
//		 TitleBar Search
//		android.R.color.tertiary_text_light
//		findViewById(R.id.titlebar_btn_search).setOnClickListener(new View.OnClickListener() {
//		    public void onClick(View v) {
//		    	onSearchRequested();
//		    }
//		});
		
		findViewById(R.id.titlebar_btn_share).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		    	emailIntent.setType("text/plain");
		    	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.recommendation_subject));
		    	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.recommendation_body));
		    	startActivity(emailIntent);
		    }
		});
		
		// Nearby
		findViewById(R.id.home_btn_nearby).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	startActivity(new Intent(HomeActivity.this, SatNavActivity.class));
		    }
		});
		
		// Search
		findViewById(R.id.home_btn_search).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
//		    	Toast.makeText(HomeActivity.this, "home_btn_search", Toast.LENGTH_LONG).show();
//		    	HomeActivity.this.startSearch(null, false, Bundle.EMPTY, false);
		    	onSearchRequested();
		    }
		});
		
		// About
		findViewById(R.id.home_btn_about).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, org.openintents.about.About.class);
				startActivity(intent);
		    }
		});
		
		checkStats();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		super.onCreateOptionsMenu(menu);
		 MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.options_menu, menu);
//        menu.add(0, R.id.search, 0, android.R.string.search_go)
//    	.setIcon(android.R.drawable.ic_dialog_map)
//    	.setAlphabeticShortcut(SearchManager.MENU_KEY);
		
        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.search:
					onSearchRequested();
//				Toast.makeText(HomeActivity.this, "onSearchRequested", Toast.LENGTH_LONG).show();
				return true;
			case R.id.menu_about:
				Intent intent1 = new Intent(this, org.openintents.about.About.class);
//				startActivityForResult(intent1, R.id.menu_about);
				startActivity(intent1);
				return true;
		}
//		return super.onOptionsItemSelected(item);
		return false;
	}
	
//    @Override
    public boolean onSearchRequested() {
    	if (!this.isOnline()){
			Toast.makeText(this, this.getResources().getText(
				R.string.error_no_inet_conn), Toast.LENGTH_LONG).show();
			return false;
		}else
			return super.onSearchRequested();
    }
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
    /**
     * Check last time stats were sent, and send again if time greater than a week
     */
	private void checkStats() {	
//		if (mPreferenceHelper.isSendStatsEnabled()) 
		{
			long statsTimestamp = mPreferenceHelper.getStatsTimestamp();
	        long timeDiff = Utils.dateDiff(statsTimestamp);
	        
//	        if (LOGV) Log.v(TAG, "Lasts stats date was " + timeDiff + "ms ago" );
			
			// Only once a week
//			if (timeDiff > 604800000) {
//	        if (timeDiff > 60*60*24*7*1000) {
//	        if (timeDiff > 60*3*1000) 
	        {
				new Thread() {
					public void run() {
						uploadStats();
					}
				}.start();
				mPreferenceHelper.setStatsTimestamp();
			}
		}
	}
	private void uploadStats() {
		Log.d("Testing", "Sending app statistics");

		// gather all of the device info
		String app_version = "";
		try {
			try {
				PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
				app_version = pi.versionName;
			} 
			catch (NameNotFoundException e) {
				app_version = "N/A";
			}

			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String device_uuid = tm.getDeviceId();
			String device_id = "00000000000000000000000000000000";
			if (device_uuid != null) {
				device_id = Utils.MD5(device_uuid);
			}
			
			String mobile_country_code = tm.getNetworkCountryIso();
			String mobile_network_number = tm.getNetworkOperator();
			int network_type = tm.getNetworkType();
	
			// get the network type string
			String mobile_network_type = "N/A";
			switch (network_type) {
			case 0:
				mobile_network_type = "TYPE_UNKNOWN";
				break;
			case 1:
				mobile_network_type = "GPRS";
				break;
			case 2:
				mobile_network_type = "EDGE";
				break;
			case 3:
				mobile_network_type = "UMTS";
				break;
			case 4:
				mobile_network_type = "CDMA";
				break;
			case 5:
				mobile_network_type = "EVDO_0";
				break;
			case 6:
				mobile_network_type = "EVDO_A";
				break;
			case 7:
				mobile_network_type = "1xRTT";
				break;
			case 8:
				mobile_network_type = "HSDPA";
				break;
			case 9:
				mobile_network_type = "HSUPA";
				break;
			case 10:
				mobile_network_type = "HSPA";
				break;
			}
	
			String device_version = android.os.Build.VERSION.RELEASE;
	
			if (device_version == null) {
				device_version = "N/A";
			}
			
			String device_model = android.os.Build.MODEL;
			
			if (device_model == null) {
				device_model = "N/A";
			}

			String device_language = getResources().getConfiguration().locale.getLanguage();
//			String home_function = mPreferenceHelper.defaultLaunchActivity();
//			String welcome_message = String.valueOf(mPreferenceHelper.isWelcomeQuoteEnabled());
			String loadstops = String.valueOf(mPreferenceHelper.getLoadstops());
			mPreferenceHelper.resetLoadstops();
			String loadstop = String.valueOf(mPreferenceHelper.getLoadstop());
			mPreferenceHelper.resetLoadstop();
			
			// post the data
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(TransantiagoGeoCoder.urlbase+"/stats/send");
			post.setHeader("Content-Type", "application/x-www-form-urlencoded");
	
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("device_id", device_id));
			pairs.add(new BasicNameValuePair("app_version", app_version));
//			pairs.add(new BasicNameValuePair("home_function", home_function));
//			pairs.add(new BasicNameValuePair("welcome_message", welcome_message));
			pairs.add(new BasicNameValuePair("device_model", device_model));
			pairs.add(new BasicNameValuePair("device_version", device_version));
			pairs.add(new BasicNameValuePair("device_language", device_language));
			pairs.add(new BasicNameValuePair("mobile_country_code", mobile_country_code));
			pairs.add(new BasicNameValuePair("mobile_network_number", mobile_network_number));
			pairs.add(new BasicNameValuePair("mobile_network_type",	mobile_network_type));

			pairs.add(new BasicNameValuePair("transdroid_loadstops", loadstops));
			pairs.add(new BasicNameValuePair("transdroid_loadstop", loadstop));

			try {
				post.setEntity(new UrlEncodedFormEntity(pairs));
			} 
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			try {
				HttpResponse response = client.execute(post);
				response.getStatusLine().getStatusCode();
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
