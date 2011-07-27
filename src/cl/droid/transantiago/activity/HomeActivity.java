package cl.droid.transantiago.activity;

import org.opensatnav.android.SatNavActivity;

import cl.droid.transantiago.R;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class HomeActivity extends Activity {
	private static final int MENU_SEARCH = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
//		 TitleBar Search
//		android.R.color.tertiary_text_light
		findViewById(R.id.home_titlebar_btn_search).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	onSearchRequested();
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
}
