/* 
This file is part of OpenSatNav.

    OpenSatNav is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenSatNav is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenSatNav.  If not, see <http://www.gnu.org/licenses/>.
*/
// Created by plusminus on 17:53:07 - 25.09.2008
package org.andnav.osm.views.util;

import org.andnav.osm.util.BoundingBoxE6;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.opensatnav.android.OpenSatNavConstants;


/**
 * 
 * @author Nicolas Gramlich
 *
 */
public class Util implements OpenStreetMapViewConstants{
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	
	public static int[] getMapTileFromCoordinates(final int aLat, final int aLon, final int zoom, final int[] reuse) {
		return getMapTileFromCoordinates(aLat / 1E6, aLon / 1E6, zoom, reuse);
	}
	
	public static int[] getMapTileFromCoordinates(final double aLat, final double aLon, final int zoom, final int[] aUseAsReturnValue) {
		final int[] out = (aUseAsReturnValue != null) ? aUseAsReturnValue : new int[2];

		out[MAPTILE_LATITUDE_INDEX] = (int) Math.floor((1 - Math.log(Math.tan(aLat * Math.PI / 180) + 1 / Math.cos(aLat * Math.PI / 180)) / Math.PI) / 2 * (1 << zoom));
		out[MAPTILE_LONGITUDE_INDEX] = (int) Math.floor((aLon + 180) / 360 * (1 << zoom));

		return out;
	}
	
	// Conversion of a MapTile to a BoudingBox
	
	public static BoundingBoxE6 getBoundingBoxFromMapTile(final int[] aMapTile, final int zoom) {
		final int y = aMapTile[MAPTILE_LATITUDE_INDEX];
		final int x = aMapTile[MAPTILE_LONGITUDE_INDEX];
		return new BoundingBoxE6(tile2lat(y, zoom), tile2lon(x + 1, zoom), tile2lat(y + 1, zoom), tile2lon(x, zoom));
	}
	
	private static double tile2lon(int x, int aZoom) {
		return (x / Math.pow(2.0, aZoom) * 360.0) - 180;
	}

	private static double tile2lat(int y, int aZoom) {
		final double n = Math.PI - ((2.0 * Math.PI * y) / Math.pow(2.0, aZoom));
		return 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
	}
	
	 /**
	   * Checks to see if this is a valid speed.
	   * 
	   * @param updateTime The time at the current reading
	   * @param speed The current speed
	   * @param lastLocationTime The time at the last location
	   * @param lastLocationSpeed Speed at the last location
	   * @param speedBuffer A buffer of recent readings
	   * @return True if this is likely a valid speed
	   */
	  public static boolean isValidSpeed(long updateTime, double speed,
	      long lastLocationTime, double lastLocationSpeed
	      ) {

	    // We don't want to count 0 towards the speed.
	    if (speed == 0) {
	      return false;
	    }

	    // We are now sure the user is moving.
	    long timeDifference = updateTime - lastLocationTime;

	    // There are a lot of noisy speed readings.
	    // Do the cheapest checks first, most expensive last.
	    // The following code will ignore unlikely to be real readings.
	    // - 128 m/s seems to be an internal android error code.
	    if (Math.abs(speed - 128) < 1) {
	      return false;
	    }

	    // Another check for a spurious reading. See if the path seems physically
	    // likely. Ignore any speeds that imply accelaration greater than 2g's
	    // Really who can accelerate faster?
	    double speedDifference = Math.abs(lastLocationSpeed - speed);
	    if (speedDifference > OpenSatNavConstants.MAX_ACCELERATION * timeDifference) {
	      return false;
	    }

	  return true;
	  }

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
