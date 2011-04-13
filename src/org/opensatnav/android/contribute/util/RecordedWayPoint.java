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

package org.opensatnav.android.contribute.util;


public class RecordedWayPoint extends RecordedGeoPoint {
	private String wayPointName;
	private String wayPointDescription;
	public RecordedWayPoint(int latitudeE6, int longitudeE6, long aTimeStamp, String wayPointName, String wayPointDescription) {
		super(latitudeE6, longitudeE6, aTimeStamp);
		this.wayPointName = wayPointName;
		this.wayPointDescription = wayPointDescription;
		// TODO Auto-generated constructor stub
	}
	
	public RecordedWayPoint(RecordedGeoPoint recordedGeoPoint, String name,	String description) {
		super(recordedGeoPoint.getLatitudeE6(), recordedGeoPoint.getLongitudeE6(), recordedGeoPoint.getTimeStamp());
		this.setWayPointName(name);
		this.setWayPointDescription(description);
		
	}

	public String getWayPointName() {
		return this.wayPointName;
	}
	
	public String getWayPointDescription() {
		return this.wayPointDescription;
	}
	
	public void setWayPointName(String wayPointName) {
		this.wayPointName = wayPointName;
	}
	
	public void setWayPointDescription(String wayPointDescription) {
		this.wayPointDescription = wayPointDescription;
	}
	
}