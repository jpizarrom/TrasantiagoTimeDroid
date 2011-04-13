package org.opensatnav.android.util;

import org.opensatnav.android.R;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class BugReportHelper {
	private Context context;

	public BugReportHelper(Context context) {
		this.context = context;
	}

	public int getRevision() {
		int revision = -1;
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			revision = pi.versionCode;

		} catch (PackageManager.NameNotFoundException e) {
		}
		return revision;
	}

	public String getVersionName() {
		return context.getString(R.string.app_name) + " " + getVersion();
	}

	public String getVersion() {
		String version = "Unknown version";
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			version = pi.versionName;

		} catch (PackageManager.NameNotFoundException e) {
		}
		return version;
	}

}
