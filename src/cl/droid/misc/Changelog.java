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
import android.content.SharedPreferences;

public class Changelog {
	private static final String TAG = "Changelog";

	public static boolean show(Activity activity) {
		SharedPreferences preferences = activity.getSharedPreferences(Utils.PREF_CHANGELOG, Activity.MODE_PRIVATE);
		int prefVersion = preferences.getInt(Utils.PREF_APP_VERSION, 0);

		int currentVersion = Utils.getAppVersion(activity, TAG);
		if (currentVersion==-1)
		    return false;

//		if (prefVersion != 0) {
			if (currentVersion > prefVersion || prefVersion == 0) {
				showChangelogDialog(activity);
//			}
		}
		preferences.edit().putInt(Utils.PREF_APP_VERSION, currentVersion).commit();
		return true;
	}

	protected static void showChangelogDialog(Activity activity) {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	    builder.setIcon(android.R.drawable.ic_dialog_info);
	    builder.setTitle(R.string.changelog_title);
	    builder.setCancelable(true);
	    builder.setPositiveButton(R.string.ok, null);
	    builder.setView(Utils.dialogWebView(activity, activity.getString(R.string.changelog_filename)));
	    builder.create().show();
	}
}