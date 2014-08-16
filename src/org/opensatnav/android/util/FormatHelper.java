package org.opensatnav.android.util;

import cl.droid.transantiago.R;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FormatHelper {
	DecimalFormat distanceFormat;
	DecimalFormat speedFormat;
	Resources r;
	String unitSystem;

	public FormatHelper(Context ctx) {
		r = ctx.getResources();
		unitSystem = PreferenceManager.getDefaultSharedPreferences(ctx)
				.getString("unit_system", "metric");
	}

	public String formatDistanceFuzzy(int metres) {
		distanceFormat = (DecimalFormat) NumberFormat.getInstance();
		distanceFormat.applyPattern("###,###.#");
		int rounded = 0;
		if (unitSystem.compareTo("metric") == 0) {
			//less than 1 km
			if (metres < 1000) {
				rounded = roundToNearest(metres, 50);
				return Integer.toString(rounded)
						+ r.getString(R.string.metres_abbreviation);
				// less than 10km
			} else if (metres < 10000) {
				rounded = roundToNearest(metres, 100);
				// round to 1 decimal point
				return distanceFormat.format(new Double(metres) / 1000)
						+ r.getString(R.string.kilometres_abbreviation);
			} else {
				// show only whole kms
				rounded = roundToNearest(metres, 1000);
				return distanceFormat.format(metres / 1000)
						+ r.getString(R.string.kilometres_abbreviation);
			}
		}
		if (unitSystem.compareTo("imperial") == 0) {
			int yards = (int) (metres * 1.0936133);
			//less that 1 mile
			if (yards < 1760) {
				rounded = roundToNearest(yards, 50);
				return Integer.toString(rounded)
						+ r.getString(R.string.yards_abbreviation);
				// less than 10 miles
			} else if (yards < 17600) {
				rounded = roundToNearest(yards, 176);
				// round to 1 decimal point
				return distanceFormat.format(new Double(yards) / 1760)
						+ r.getString(R.string.miles_abbreviation);
			} else {
				// show only whole miles
				rounded = roundToNearest(yards, 1760);
				return distanceFormat.format(yards / 1760)
						+ r.getString(R.string.miles_abbreviation);
			}
		} else
			return null;

	}

	public String formatSpeed(float metresPerSecond) {
		speedFormat = (DecimalFormat) NumberFormat.getInstance();
		speedFormat.applyPattern("###.##");
		if (unitSystem.compareTo("metric") == 0) {

			return speedFormat.format(metresPerSecond * 3600f / 1000f)
					+ r.getString(R.string.kmh_abbreviation);
		}
		if (unitSystem.compareTo("imperial") == 0) {
			return speedFormat.format(metresPerSecond * 3600f / 1760f)
					+ r.getString(R.string.mph_abbreviation);
		} else
			return null;
	}

	// round number to the nearest precision
	private int roundToNearest(int number, int precision) {
		return (number / precision) * precision;
	}
}
