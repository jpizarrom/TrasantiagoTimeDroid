/**
 *  Kitchen Timer
 *  Copyright (C) 2010 Roberto Leinardi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package cl.droid.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import cl.droid.transantiago.R;
import cl.droid.transantiago.activity.HomeActivity;
import cl.droid.transantiago.service.TransantiagoGeoCoder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

public class Utils {
    // ===========================================================
    // Constants
    // ===========================================================
    public static final int IO_BUFFER_SIZE = 8 * 1024;

    public static final String PREF_CHANGELOG = "changelog";
    public static final String PREF_APP_VERSION = "app.version";
    
    public static final String PREF_EULA = "eula";
    public static final String PREF_EULA_ACCEPTED = "eula.accepted";
    
	/**
	 * 
	 * @param totalSeconds
	 * @param timer
	 * @return
	 */
	public static String formatTime(long totalSeconds, int timer) {
		if (timer == 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			return sdf.format(new Date(totalSeconds * 1000));
		} else {

			String seconds = Integer.toString((int) (totalSeconds % 60));
			String minutes = Integer.toString((int) (totalSeconds / 60));
			if (seconds.length() < 2) {
				seconds = "0" + seconds;
			}
			if (minutes.length() < 2) {
				minutes = "0" + minutes;
			}
			return minutes + ":" + seconds;
		}
	}

	public static float dp2px(int dip, Context context){
		float scale = context.getResources().getDisplayMetrics().density;
		return dip * scale + 0.5f;
	}

	public static View dialogWebView(Context context, String fileName) {
		View view = View.inflate(context, R.layout.dialog_webview, null);
		//		TextView textView = (TextView) view.findViewById(R.id.message);
		//		textView.setMovementMethod(LinkMovementMethod.getInstance());
		//		CharSequence cs =  readTextFile(context, fileName);
		//
		//		SpannableString s = new SpannableString(Html.fromHtml(cs.toString()));
		//		Linkify.addLinks(s, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
		//		textView.setText(s);
		WebView web = (WebView) view.findViewById(R.id.wv_dialog);
		web.loadUrl("file:///android_asset/"+fileName);
		return view;
	}

	public static CharSequence readTextFile(Context context, String fileName) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
			String line;
			StringBuilder buffer = new StringBuilder();
			while ((line = in.readLine()) != null) buffer.append(line).append('\n');
			return buffer;
		} catch (IOException e) {
			Log.e("readTextFile", "Error readind file " + fileName, e);
			return "";
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}

	public static int getAppVersion(Activity activity, String TAG) {
	    int currentVersion;
	    try {
	        PackageInfo pi = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
	        currentVersion = pi.versionCode;
	    } catch (NameNotFoundException e){
	        Log.e(TAG, "Package name not found", e);
	        return -1;
	    }
	    return currentVersion;
	}
	
	public static boolean isSdPresent() {
		return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}

	public static void donate(Context mContext) {
		Intent intent = new Intent( Intent.ACTION_VIEW,
				Uri.parse("market://search?q=Donation%20pub:%22Roberto%20Leinardi%22") );
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}

	 /**
     * Copy the content of the input stream into the output stream, using a temporary
     * byte array buffer whose size is defined by {@link #IO_BUFFER_SIZE}.
     *
     * @param in The input stream to copy from.
     * @param out The output stream to copy to.
     *
     * @throws IOException If any error occurs during the copy.
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

    /**
     * Closes the specified stream.
     *
     * @param stream The stream to close.
     */
    public static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                android.util.Log.e("IO", "Could not close stream", e);
            }
        }
    }
    
    /**
     * Return the difference between two dates
     */
	public static long dateDiff(long timestamp) {
		Date now = new Date();
		long diff = now.getTime() - timestamp;
		return diff;
	}
	
	private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }
 
    public static String MD5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException  {
        MessageDigest md;
        md = MessageDigest.getInstance("MD5");
        byte[] md5hash = new byte[32];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        md5hash = md.digest();
        return convertToHex(md5hash);
    }
    
    public static void uploadStats(Activity activity, PreferenceHelper mPreferenceHelper) {
		Log.d("Testing", "Sending app statistics");

		// gather all of the device info
		String app_version = "";
		try {
			try {
				PackageInfo pi = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
				app_version = pi.versionName;
			} 
			catch (NameNotFoundException e) {
				app_version = "N/A";
			}

			TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
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

			String device_language = activity.getResources().getConfiguration().locale.getLanguage();
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