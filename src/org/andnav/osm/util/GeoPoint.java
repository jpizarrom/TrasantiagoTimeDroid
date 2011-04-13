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
// Created by plusminus on 21:28:12 - 25.09.2008
package org.andnav.osm.util;

import org.andnav.osm.util.constants.GeoConstants;
import org.andnav.osm.views.util.constants.MathConstants;

/**
 * 
 * @author Nicolas Gramlich
 *
 */
public class GeoPoint implements MathConstants, GeoConstants{
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	
	private int mLongitudeE6;
	private int mLatitudeE6;

	// ===========================================================
	// Constructors
	// ===========================================================
	
	public GeoPoint(final int aLatitudeE6, final int aLongitudeE6) {
		this.mLatitudeE6 = aLatitudeE6;
		this.mLongitudeE6 = aLongitudeE6;
	}
	
	public GeoPoint(double aLatitude, double aLongitude) {
		this.mLatitudeE6 = (int) (aLatitude * 1E6);
		this.mLongitudeE6 = (int) (aLongitude * 1E6);
	}
	
	public static GeoPoint fromDoubleString(final String s, final char spacer) {
		final int spacerPos = s.indexOf(spacer);
		return new GeoPoint((int) (Double.parseDouble(s.substring(0,
				spacerPos)) * 1E6), (int) (Double.parseDouble(s.substring(
				spacerPos + 1, s.length())) * 1E6));
	}
	
	public static GeoPoint fromIntString(final String s){
		final int commaPos = s.indexOf(',');
		return new GeoPoint(Integer.parseInt(s.substring(0,commaPos)),
				Integer.parseInt(s.substring(commaPos+1,s.length())));
	} 

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	public int getLongitudeE6() {
		return this.mLongitudeE6;
	}

	public int getLatitudeE6() {
		return this.mLatitudeE6;
	}
	
	public double getLatitude() {
		return this.mLatitudeE6 / 1E6;
	}
	
	public double getLongitude() {
		return this.mLongitudeE6 / 1E6;
	}
	
	
	public void setLongitudeE6(final int aLongitudeE6) {
		this.mLongitudeE6 = aLongitudeE6;
	}

	public void setLatitudeE6(final int aLatitudeE6) {
		this.mLatitudeE6 = aLatitudeE6;
	}
	
	public void setCoordsE6(final int aLatitudeE6, final int aLongitudeE6) {
		this.mLatitudeE6 = aLatitudeE6;
		this.mLongitudeE6 = aLongitudeE6;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	public String toString(){
		return new StringBuilder().append(this.mLatitudeE6).append(",").append(this.mLongitudeE6).toString();
	}

	public String toDoubleString() {
		return new StringBuilder().append(this.mLatitudeE6 / 1E6).append(",").append(this.mLongitudeE6  / 1E6).toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof GeoPoint))
			return false;
		GeoPoint g = (GeoPoint)obj;
		return g.mLatitudeE6 == this.mLatitudeE6 && g.mLongitudeE6 == this.mLongitudeE6;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	public int distanceTo(GeoPoint to) {
		float[] results = new float[1];
		computeDistanceAndBearing(this.getLatitude(), this.getLongitude(), to.getLatitude(), to.getLongitude(), results);
		return (int) results[0];
	}
	
	//
	private static void computeDistanceAndBearing(double lat1, double lon1,
	        double lat2, double lon2, float[] results) {
	        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
	        // using the "Inverse Formula" (section 4)

	        int MAXITERS = 20;
	        // Convert lat/long to radians
	        lat1 *= Math.PI / 180.0;
	        lat2 *= Math.PI / 180.0;
	        lon1 *= Math.PI / 180.0;
	        lon2 *= Math.PI / 180.0;

	        double a = 6378137.0; // WGS84 major axis
	        double b = 6356752.3142; // WGS84 semi-major axis
	        double f = (a - b) / a;
	        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

	        double L = lon2 - lon1;
	        double A = 0.0;
	        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
	        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

	        double cosU1 = Math.cos(U1);
	        double cosU2 = Math.cos(U2);
	        double sinU1 = Math.sin(U1);
	        double sinU2 = Math.sin(U2);
	        double cosU1cosU2 = cosU1 * cosU2;
	        double sinU1sinU2 = sinU1 * sinU2;

	        double sigma = 0.0;
	        double deltaSigma = 0.0;
	        double cosSqAlpha = 0.0;
	        double cos2SM = 0.0;
	        double cosSigma = 0.0;
	        double sinSigma = 0.0;
	        double cosLambda = 0.0;
	        double sinLambda = 0.0;

	        double lambda = L; // initial guess
	        for (int iter = 0; iter < MAXITERS; iter++) {
	            double lambdaOrig = lambda;
	            cosLambda = Math.cos(lambda);
	            sinLambda = Math.sin(lambda);
	            double t1 = cosU2 * sinLambda;
	            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
	            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
	            sinSigma = Math.sqrt(sinSqSigma);
	            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
	            sigma = Math.atan2(sinSigma, cosSigma); // (16)
	            double sinAlpha = (sinSigma == 0) ? 0.0 :
	                cosU1cosU2 * sinLambda / sinSigma; // (17)
	            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
	            cos2SM = (cosSqAlpha == 0) ? 0.0 :
	                cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

	            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
	            A = 1 + (uSquared / 16384.0) * // (3)
	                (4096.0 + uSquared *
	                 (-768 + uSquared * (320.0 - 175.0 * uSquared)));
	            double B = (uSquared / 1024.0) * // (4)
	                (256.0 + uSquared *
	                 (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
	            double C = (f / 16.0) *
	                cosSqAlpha *
	                (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
	            double cos2SMSq = cos2SM * cos2SM;
	            deltaSigma = B * sinSigma * // (6)
	                (cos2SM + (B / 4.0) *
	                 (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
	                  (B / 6.0) * cos2SM *
	                  (-3.0 + 4.0 * sinSigma * sinSigma) *
	                  (-3.0 + 4.0 * cos2SMSq)));

	            lambda = L +
	                (1.0 - C) * f * sinAlpha *
	                (sigma + C * sinSigma *
	                 (cos2SM + C * cosSigma *
	                  (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

	            double delta = (lambda - lambdaOrig) / lambda;
	            if (Math.abs(delta) < 1.0e-12) {
	                break;
	            }
	        }

	        float distance = (float) (b * A * (sigma - deltaSigma));
	        results[0] = distance;
	        if (results.length > 1) {
	            float initialBearing = (float) Math.atan2(cosU2 * sinLambda,
	                cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
	            initialBearing *= 180.0 / Math.PI;
	            results[1] = initialBearing;
	            if (results.length > 2) {
	                float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
	                    -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
	                finalBearing *= 180.0 / Math.PI;
	                results[2] = finalBearing;
	            }
	        }
	    }

	

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
