package org.opensatnav.android.services.routing;
import java.text.DecimalFormat;

import org.andnav.osm.views.OpenStreetMapView;
import org.opensatnav.android.OpenSatNavConstants;
import org.opensatnav.android.SatNavActivity;
import org.opensatnav.android.services.NoLocationProvidersException;
import org.opensatnav.android.services.routing.overlay.RouteOverlay;
import org.opensatnav.android.util.FormatHelper;

import android.location.Location;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
public class RouteInstructionServiceTTS extends RouteInstructionsService implements OnInitListener  {
	public TextToSpeech mTts;
	private Boolean ttsReady = false;
	
	public RouteInstructionServiceTTS(SatNavActivity satNavActivity,
			RouteOverlay routeOverlay, OpenStreetMapView mOsmv) {
		super(satNavActivity, routeOverlay, mOsmv);
	}

	public void stopTTS() {
		if (mTts != null) {
			mTts.shutdown();
		}
		
	}
	
	public void startRoute(Route route) throws NoLocationProvidersException {
		super.startRoute(route);
		speakText("Navigation Active");
		speakText(route.routeInstructions.get(currentInstructionID).description);
		
	}
	
	public void speakText(String text) {

		if (ttsReady) {
			mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
		}
	}
	
	public void checkForRouteInstructionPrompt(int metresToNextInstruction) {
		RouteInstructionPrompt rip;
		
		for(int i = 0; i < currentInstruction.routeInstructionPrompts.size(); i++) {
			rip = currentInstruction.routeInstructionPrompts.get(i);
			if (rip.basBeenSaid == false && rip.metresFromInstruction > metresToNextInstruction) {
				rip.basBeenSaid = true;
				//Say Instruction!
				currentInstruction.removePromptsHigherThan(metresToNextInstruction); //Dont say multiple prompts, remove prompts for current instruction higher than current one.
				//See how long next instruction will take
				FormatHelper fmt = new FormatHelper(context);
				if (route.routeInstructions.size() > currentInstructionID + 1) {
					
					RouteInstruction nextRouteInstruction = route.routeInstructions.get(currentInstructionID + 1);
					if(nextRouteInstruction.time < ttsMinimumTimeToNextInstruction) {
						//Include the instruction after
						DecimalFormat df = new DecimalFormat("#");
						
						speakText("In " + fmt.formatDistanceFuzzy(metresToNextInstruction) + " " + currentInstruction.humanTurnType() + ", and then in " + fmt.formatDistanceFuzzy(currentInstruction.length.intValue()) + " " +  nextRouteInstruction.humanTurnType());

					} else {
						//Don't include the instruction after
						speakText("In " + fmt.formatDistanceFuzzy(metresToNextInstruction)  + " " +  currentInstruction.humanTurnType());
					}
				}else {
					//Don't include the instruction after
					speakText("In " + fmt.formatDistanceFuzzy(metresToNextInstruction)  + " " +  currentInstruction.humanTurnType());
				}
				

				
			}
		}
	}
	
	
	
	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		ttsReady = true;
	}
	
	@Override
	public void onLocationChanged(Location currentLocation) {
		if (currentlyRouting) {
		Log.v(OpenSatNavConstants.LOG_TAG, "RIC Running onLocationChanged");
		mostRecentLocation = currentLocation;
		if (!gettingRoute) {
				
				//Bit problematic this
			//if(!isOnRoute(currentLocation)) {
			//	speakText("Off route, getting new route");
			//	refreshRoute(TypeConverter.locationToGeoPoint(currentLocation), route.to, route.vehicle);
			//	return;
			//}

			updateIDs(currentLocation);
			
			float distanceToNextPoint = distanceToNextPoint(currentLocation);
			//routeOverlay.distanceToNextPoint = distanceToNextPoint;
			Log.v(OpenSatNavConstants.LOG_TAG, "gettingRoute is " + gettingRoute);
			
			updateWidget(distanceToNextPoint);

			
		}
		}
	}
	
	
	
	
}
