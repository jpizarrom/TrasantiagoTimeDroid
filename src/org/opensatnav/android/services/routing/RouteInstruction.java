package org.opensatnav.android.services.routing;

import java.util.ArrayList;

import org.andnav.osm.util.GeoPoint;

public class RouteInstruction {
	public Integer latE6;
	public Integer lngE6;
	public String description; //Description of instruction, ie, "Turn left at x street"
	public Double length; //this is "distance" in the XML, but "length" in cloudmade docs. Measured in metres
	public int time; //this is the time to traverse the current instruction
	public int offset; //I think this relates to the GPX point in the previously given list. 
	public String distanceText; //This is the human readable distance, ie "3.1m". Not too sure if we need this
	public String direction; //TODO: make this a lookup?
	public Double azimuth;
	
	public String turnType; //Human readable string representing type of turn
	public Double turnAngle; //Angle of turn;
	public ArrayList<RouteInstructionPrompt> routeInstructionPrompts;
	
	
	public RouteInstruction() {
		routeInstructionPrompts = new ArrayList<RouteInstructionPrompt>();
		routeInstructionPrompts.add(new RouteInstructionPrompt(2000));
		routeInstructionPrompts.add(new RouteInstructionPrompt(500));
		routeInstructionPrompts.add(new RouteInstructionPrompt(100));
	}
	
	
	public double getLatitude() {
		return latE6 / 1E6;
	}
	public double getLongitude() {
		return lngE6 / 1E6;
	}
	
	public GeoPoint getGeoPoint() {
		return new GeoPoint(latE6, lngE6);
	}


	public void removePromptsHigherThan(int lowerMetresFromInstruction) {
		for(RouteInstructionPrompt currentRIP : routeInstructionPrompts) {
			if (currentRIP.metresFromInstruction >= lowerMetresFromInstruction) {
				currentRIP.basBeenSaid = true;
			}
		}
		
	}
	
	public String humanTurnType(){
		return CloudmadeXMLHandler.convertCloudmadeTurnInstructionToHumanReadableString(turnType);
	}
	
	
}
