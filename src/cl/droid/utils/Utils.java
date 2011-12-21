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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cl.droid.misc.Log;
import cl.droid.transantiago.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
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
}