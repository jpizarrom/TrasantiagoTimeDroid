package cl.droid.transantiago.activity;

import org.opensatnav.android.OpenSatNavConstants;

import cl.droid.transantiago.R;
import cl.droid.transantiago.R.id;
import cl.droid.transantiago.R.layout;
import cl.droid.transantiago.R.string;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class ServiceActivity extends Activity {
	final Activity activity = this;
	int first = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web);
		WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        final String url = getIntent().getStringExtra("url");
        String params = getIntent().getStringExtra("params");
        
        webView.setVisibility(WebView.INVISIBLE);
        webView.setWebChromeClient(new WebChromeClient() {
        	
            public void onProgressChanged(WebView view, int progress)
            {
                activity.setTitle("Loading..."+first);
                activity.setProgress(progress * 100);
 
                if(progress == 100){
                	if(first<1){
//                		Log.i(OpenSatNavConstants.LOG_TAG, view.getUrl());
                		first++;
                		Log.i(OpenSatNavConstants.LOG_TAG, url);
                		view.loadUrl(url);
                		}else
                			view.setVisibility(WebView.VISIBLE);
                    activity.setTitle(R.string.app_name);
                }
//                Log.i(OpenSatNavConstants.LOG_TAG, view.getUrl());
            }
        });
        Log.i(OpenSatNavConstants.LOG_TAG, url);
        webView.loadUrl("http://m.ibus.cl");
	}

}
