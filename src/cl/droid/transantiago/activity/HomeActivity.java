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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends ActionBarActivity {
	private static final int MENU_SEARCH = 0;
	private PreferenceHelper mPreferenceHelper;
	private Context mContext;
	private ShareActionProvider mShareActionProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		mContext = getBaseContext();
		mPreferenceHelper = new PreferenceHelper(mContext);
		
		boolean appUnchanged = Changelog.show(this);
//		((TextView) findViewById(R.id.title_text)).setText("Home");
//		 TitleBar Search
//		android.R.color.tertiary_text_light
//		findViewById(R.id.titlebar_btn_search).setOnClickListener(new View.OnClickListener() {
//		    public void onClick(View v) {
//		    	onSearchRequested();
//		    }
//		});
		
//		findViewById(R.id.titlebar_btn_share).setOnClickListener(new View.OnClickListener() {
//		    public void onClick(View v) {
//		    	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
//		    	emailIntent.setType("text/plain");
//		    	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.recommendation_subject));
//		    	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.recommendation_body));
//		    	startActivity(emailIntent);
//		    }
//		});
		
		// Nearby
		findViewById(R.id.home_btn_nearby).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	startActivity(new Intent(HomeActivity.this, MapActivity.class));
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
//				Intent intent = new Intent(HomeActivity.this, org.openintents.about.About.class);
//				startActivity(intent);
		    }
		});
		
		checkStats();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.options_menu, menu);
//        menu.add(0, R.id.search, 0, android.R.string.search_go)
//    	.setIcon(android.R.drawable.ic_dialog_map)
//    	.setAlphabeticShortcut(SearchManager.MENU_KEY);
	        
	  MenuItem menuItem = menu.findItem(R.id.menu_share);
	  mShareActionProvider = (ShareActionProvider)
	            MenuItemCompat.getActionProvider(menuItem);
	    mShareActionProvider.setShareIntent(getDefaultIntent());

      return super.onCreateOptionsMenu(menu);

	}
	private Intent getDefaultIntent() {
	    Intent intent = new Intent(Intent.ACTION_SEND);
	    intent.setType("text/plain");
	    intent.putExtra(Intent.EXTRA_SUBJECT, "TransDroid");
	    intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=cl.droid.transantiago");
	    return intent;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
//			case R.id.search:
//					onSearchRequested();
////				Toast.makeText(HomeActivity.this, "onSearchRequested", Toast.LENGTH_LONG).show();
//				return true;
			case R.id.menu_about:
//				Intent intent1 = new Intent(this, org.openintents.about.About.class);
////				startActivityForResult(intent1, R.id.menu_about);
//				startActivity(intent1);
				return true;
			case R.id.menu_preferences:
				Intent intent = new Intent(this,
						ConfigurationActivity.class);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
		
//    @Override
    public boolean onSearchRequested() {
//    	if (!this.isOnline()){
//			Toast.makeText(this, this.getResources().getText(
//				R.string.error_no_inet_conn), Toast.LENGTH_LONG).show();
//			return false;
//		}else
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
		if (mPreferenceHelper.isSendStatsEnabled()) 
		{
			long statsTimestamp = mPreferenceHelper.getStatsTimestamp();
	        long timeDiff = Utils.dateDiff(statsTimestamp);
	        			
			// Only once a week
	        if (timeDiff > 60*60*24*7*1000) 
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
		Utils.uploadStats(this,mPreferenceHelper);

	}
	
}
