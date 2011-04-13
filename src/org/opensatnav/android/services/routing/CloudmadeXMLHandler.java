package org.opensatnav.android.services.routing;


import org.andnav.osm.util.GeoPoint;
import org.opensatnav.android.OpenSatNavConstants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.location.Location;
import android.util.Log;

public class CloudmadeXMLHandler extends DefaultHandler{

	private boolean inMainExtensions = false;
	private boolean inRouteInstruction = false;
	
	private boolean inRouteDistance = false;
	private boolean inRouteTime = false;
	private boolean inRouteStart = false;
	private boolean inRouteEnd = false;
	
	private boolean inRouteInstructionExtensions = false;
	private boolean inRouteInstructionDescription = false;
	private boolean inRouteInstructionDistance = false;
	private boolean inRouteInstructionTime = false;
	private boolean inRouteInstructionOffset = false;
	private boolean inRouteInstructionDistanceText = false;
	private boolean inRouteInstructionDirection = false;
	private boolean inRouteInstructionAzimuth = false;
	private boolean inRouteInstructionTurn = false;
	private boolean inRouteInstructionTurnAngle = false;
	
	private RouteInstruction currentRouteInstruction;
		
		public Route route;
		
		// ===========================================================
        // Fields
        // ===========================================================
       
        
       
        // ===========================================================
        // Methods
        // ===========================================================
        @Override
        public void startDocument() throws SAXException {
        	Log.v(OpenSatNavConstants.LOG_TAG, "startDocument()");
                route = new Route();
        }
 
        @Override
        public void endDocument() throws SAXException {
                //We need to add a final instruction for "You have reached your destination"
        	currentRouteInstruction = new RouteInstruction();
        	currentRouteInstruction.latE6 = route.routeGPX.get(route.routeGPX.size() -1).getLatitudeE6();
        	currentRouteInstruction.lngE6 = route.routeGPX.get(route.routeGPX.size() -1).getLongitudeE6();
        	currentRouteInstruction.description = "You have arrived at your destination";
        	currentRouteInstruction.offset = route.routeGPX.size() -1;
        	currentRouteInstruction.turnType = "You have arrived at your destination";
        	//TODO: Would be nice to have a time etc.
        	Double totalDistance = 0.0;
        	float[] results = new float[1];
        	for(int offsetID = route.routeInstructions.get(route.routeInstructions.size() -1).offset;offsetID<route.routeGPX.size() - 1;offsetID++){
				Location.distanceBetween(route.routeGPX.get(offsetID).getLatitude(), route.routeGPX.get(offsetID).getLongitude(), route.routeGPX.get(offsetID + 1).getLatitude(), route.routeGPX.get(offsetID + 1).getLongitude(), results);
				totalDistance = totalDistance + results[0];
			}
        	currentRouteInstruction.length = totalDistance;
        	currentRouteInstruction.time = 1;
        	route.routeInstructions.add(currentRouteInstruction);
        	
        }
 
        /** Gets be called on opening tags like:
         * <tag>
         * Can provide attribute(s), when xml was like:
         * <tag attribute="attributeValue">*/
        @Override
        public void startElement(String namespaceURI, String localName,
        		String qName, Attributes atts) throws SAXException {

        	if (localName.equals("wpt")) {
        		// Extract an Attribute
        		
        		Float lat = Float.parseFloat(atts.getValue("lat"));
        		Float lon = Float.parseFloat(atts.getValue("lon"));
        		
        		route.routeGPX.add(new GeoPoint((int) (lat * 1000000), (int)(lon * 1000000)));
        		if(route.firstMaxSet == false) {
        			route.maxLatE6 = (int) (lat * 1E6);
        			route.minLatE6 = (int) (lat * 1E6);
        			
        			route.maxLngE6 = (int) (lon * 1E6);
        			route.minLngE6 = (int) (lon * 1E6);
        		} else {
        			
        			if (lat* 1E6 > route.maxLatE6 ) {
        				route.maxLatE6 = (int) (lat * 1E6);
        			}
        			if (lat* 1E6 < route.minLatE6 ) {
        				route.minLatE6 = (int) (lat * 1E6);
        			}
        			if (lon* 1E6 > route.maxLngE6 ) {
        				route.maxLngE6 = (int) (lat * 1E6);
        			}
        			if (lon* 1E6 < route.minLngE6 ) {
        				route.minLngE6 = (int) (lat * 1E6);
        			}
        			
        		}
        	}

        	if (localName.equals("rtept")) {
        		inRouteInstruction = true;
        		currentRouteInstruction = new RouteInstruction();
        		Float lat = Float.parseFloat(atts.getValue("lat"));
        		Float lon = Float.parseFloat(atts.getValue("lon"));
        		currentRouteInstruction.latE6 = (int) (lat * 1000000);
        		currentRouteInstruction.lngE6 = (int) (lon * 1000000);
        	}

        	if (inRouteInstruction == false && localName.equals("extensions")) {
        		inMainExtensions = true;
        	}



        	if (inRouteInstruction) {
        		if (localName.equals("desc")) {
        			inRouteInstructionDescription = true;
        		}
        		if (localName.equals("extensions")) {
        			inRouteInstructionExtensions = true;
        		}
        	}
        	if (inRouteInstructionExtensions) {
        		if (localName.equals("distance")) {
        			inRouteInstructionDistance = true;
        		}
        		if (localName.equals("time")) {
        			inRouteInstructionTime = true;
        		}
        		if (localName.equals("offset")) {
        			inRouteInstructionOffset = true;
        		}
        		if (localName.equals("distance-text")) {
        			inRouteInstructionDistanceText = true;
        		}
        		if (localName.equals("direction")) {
        			inRouteInstructionDirection = true;
        		}
        		if (localName.equals("azimuth")) {
        			inRouteInstructionAzimuth = true;
        		}
        		if (localName.equals("turn")) {
        			inRouteInstructionTurn = true;
        		}
        		if (localName.equals("turn-angle")) {
        			inRouteInstructionTurnAngle = true;
        		}


        	}
        }
       
        /** Gets be called on closing tags like:
         * </tag> */
        @Override
        public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {
        	if (inMainExtensions && localName.equals("extensions")) {
        		inMainExtensions = false;
        	}
        	if (localName.equals("rtept")) {
        		route.routeInstructions.add(currentRouteInstruction);
        		Log.v(OpenSatNavConstants.LOG_TAG, "Added new route instruction, that makes " + route.routeInstructions.size());
        		Log.v(OpenSatNavConstants.LOG_TAG, "Instruction is " + currentRouteInstruction.description + " and turntype " + currentRouteInstruction.turnType);
        		inRouteInstruction = false;
        	}

        	if (inRouteInstruction) {
        		if (localName.equals("desc")) {
        			inRouteInstructionDescription = false;
        		}
        		if (localName.equals("extensions")) {
        			inRouteInstructionExtensions = false;
        		}
        	}
        	if (inRouteInstructionExtensions) {
        		if (localName.equals("distance")) {
        			inRouteInstructionDistance = false;
        		}
        		if (localName.equals("time")) {
        			inRouteInstructionTime = false;
        		}
        		if (localName.equals("offset")) {
        			inRouteInstructionOffset = false;
        		}
        		if (localName.equals("distance-text")) {
        			inRouteInstructionDistanceText = false;
        		}
        		if (localName.equals("direction")) {
        			inRouteInstructionDirection = false;
        		}
        		if (localName.equals("azimuth")) {
        			inRouteInstructionAzimuth = false;
        		}
        		if (localName.equals("turn")) {
        			inRouteInstructionTurn = false;
        		}
        		if (localName.equals("turn-angle")) {
        			inRouteInstructionTurnAngle = false;
        		}
        	}
        }

        /** Gets be called on the following structure:
         * <tag>characters</tag> */
        @Override
        public void characters(char ch[], int start, int length) {
        	if (inRouteDistance) {
        		route.totalDistance = Double.valueOf(new String(ch, start, length));
        	}
        	if (inRouteTime) {
        		route.totalTime = Double.valueOf(new String(ch, start, length));
        	}
        	if (inRouteStart) {
        		route.startName = new String(ch, start, length);
        	}
        	if (inRouteEnd) {
        		route.endName = new String(ch, start, length);
        	}
        	
        	if (inRouteInstructionDescription) {
        		currentRouteInstruction.description = new String(ch, start, length);
           	}
        	if (inRouteInstructionDistance) {
        		currentRouteInstruction.length = Double.valueOf(new String(ch, start, length));
        		Log.v(OpenSatNavConstants.LOG_TAG, "Just parsed " + currentRouteInstruction.length);
        	}
        	if (inRouteInstructionTime) {
        		currentRouteInstruction.time = Integer.parseInt(new String(ch, start, length));
        	}
        	if (inRouteInstructionOffset) {
        		currentRouteInstruction.offset = Integer.parseInt(new String(ch, start, length));
        	}
        	if (inRouteInstructionDistanceText) {
        		currentRouteInstruction.distanceText = new String(ch, start, length);
        	}
        	if (inRouteInstructionDirection) {
        		currentRouteInstruction.direction = new String(ch, start, length);
        	}
        	if (inRouteInstructionAzimuth) {
        		currentRouteInstruction.azimuth = Double.valueOf(new String(ch, start, length));
        	}
        	if (inRouteInstructionTurn) {
        		currentRouteInstruction.turnType = new String(ch, start, length);
        	}
        	if (inRouteInstructionTurnAngle) {
        		currentRouteInstruction.turnAngle = Double.valueOf(new String(ch, start, length));
        	}



        }
        
        public static String convertCloudmadeTurnInstructionToHumanReadableString(String cloudmadeCharacters) {
        	if(cloudmadeCharacters.equals("C")) {
        		return "Continue";
        	} else if (cloudmadeCharacters.equals("TL")){
        		return "Turn Left";
        	} else if (cloudmadeCharacters.equals("TSLL")) {
        		return "Turn Slight Left";
        	} else if (cloudmadeCharacters.equals("TSHL")) {
        		return "Turn Sharp Left";
        	} else if (cloudmadeCharacters.equals("TR")) {
        		return "Turn Right";
        	} else if (cloudmadeCharacters.equals("TSSR")) {
        		return "Turn Slight Right";
        	} else if (cloudmadeCharacters.equals("TSHR")) {
        		return "Turn Sharp Right";
        	} else if (cloudmadeCharacters.equals("TU")) {
        		return "Turn Around";
        	} else if (cloudmadeCharacters.contains("EXIT")) {
        		//It's a roundabout!
        		return "Take the " + convertNumberToSpokenString(Integer.parseInt(cloudmadeCharacters.replace("EXIT", ""))) + " exit at the roundabout";
        	}
        	
        	{
        		return "";
        	}
        	
        	
        }
        
        private static String convertNumberToSpokenString(int exitNumber) {
        	switch (exitNumber) {
        	case 1: return "first"; 
        	case 2: return "second";
        	case 3: return "third";
        	case 4: return "fourth";
        	case 5: return "fifth";
        	case 6: return "sixth";
        	case 7: return "seventh";
        	case 8: return "eigth";
        	case 9: return "ninth";
        	case 10: return "tenth";
        	case 11: return "eleventh";
        	default: return "exit " + exitNumber;
        			
        	
        	
        	}	
        }
}
