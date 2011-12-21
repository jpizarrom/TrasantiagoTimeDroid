/**
 * Copyright (C) 2011 Luis Saavedra
 *
 * This file is part of contactos-cl.
 *
 * contactos-cl is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * contactos-cl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with contactos-cl.  If not, see <http://www.gnu.org/licenses/>.
 */
package cl.droid.misc;

import cl.droid.transantiago.R;
import cl.droid.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;

public class Changelog {
	private static final String TAG = "Changelog";

	public static boolean show(Activity activity) {
		SharedPreferences preferences = activity.getSharedPreferences(Utils.PREF_CHANGELOG, Activity.MODE_PRIVATE);
		int prefVersion = preferences.getInt(Utils.PREF_APP_VERSION, 0);
		boolean eula = preferences.getBoolean(Utils.PREF_EULA_ACCEPTED, false);

		int currentVersion = Utils.getAppVersion(activity, TAG);
		if (currentVersion==-1)
		    return false;

//		if (prefVersion != 0) {
			if (currentVersion > prefVersion || prefVersion == 0 || eula == false) {
				preferences.edit().putBoolean(Utils.PREF_EULA_ACCEPTED, false).commit();
				showChangelogDialog(activity, preferences);
			}
//		}
		preferences.edit().putInt(Utils.PREF_APP_VERSION, currentVersion).commit();
		return true;
	}

	protected static void showChangelogDialog(final Activity activity, final SharedPreferences preferences) {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	    builder.setIcon(android.R.drawable.ic_dialog_info);
	    builder.setTitle(R.string.changelog_title);
	    builder.setCancelable(true);
	    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				accept(preferences);
			}
		});
	    	    
	    builder.setView(Utils.dialogWebView(activity, activity.getString(R.string.changelog_filename)));
	    builder.create().show();
	}
	private static void accept(SharedPreferences preferences) {
		preferences.edit().putBoolean(Utils.PREF_EULA_ACCEPTED, true).commit();
	}
	private static void refuse(Activity activity) {
		activity.finish();
	}
}