package org.opensatnav.android.services.routing.overlay;


import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.overlay.OpenStreetMapViewRouteOverlay;
import org.opensatnav.android.SatNavActivity;
import org.opensatnav.android.services.routing.Route;

import android.graphics.Canvas;

public class RouteOverlay extends OpenStreetMapViewOverlay {
	public OpenStreetMapViewRouteOverlay routeOverlay;
	//public NavigationHUD navigationHUD;
	public Route route;
	public double distanceToNextPoint;
	public Boolean isCurrentlyRouting;
	
	/*public RouteOverlay(SatNavActivity satNavActivity,
			Route route) {
		this.route = route;
		routeOverlay = new OpenStreetMapViewRouteOverlay(satNavActivity, route.routeGPX);
		navigationHUD = new NavigationHUD(satNavActivity.getApplicationContext());
		navigationHUD.currentInstruction = route.routeInstructions.get(0);
		distanceToNextPoint = route.routeInstructions.get(0).length;
	}*/
	
	public RouteOverlay(SatNavActivity satNavActivity) {
		isCurrentlyRouting = false;
		routeOverlay = new OpenStreetMapViewRouteOverlay(satNavActivity);
		//navigationHUD = new NavigationHUD(satNavActivity.getApplicationContext());
	}
	
	public void showRoute(Route route) {
		isCurrentlyRouting = true;
		this.route = route;
		routeOverlay.setRoute(route.routeGPX);
		//navigationHUD.nextRouteInstruction = route.routeInstructions.get(0);
		distanceToNextPoint = route.routeInstructions.get(0).length;
	}
	

	

	@Override
	protected void onDraw(Canvas c, OpenStreetMapView osmv) {
		// TODO Auto-generated method stub
		if (isCurrentlyRouting) {
			routeOverlay.onDraw(c, osmv);
		}
	}

	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {
		//if (isCurrentlyRouting) {
		//	navigationHUD.distanceToNextPoint = distanceToNextPoint;
		//	navigationHUD.onDrawFinished(c, osmv);
		//}
	}
	
}
