package org.andnav.osm.views.overlay;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.opensatnav.android.OpenSatNavConstants;
import org.opensatnav.android.R;
import org.opensatnav.android.services.routing.RouteInstructionsService;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;

public class OpenStreetMapViewTouchResponderOverlay extends OpenStreetMapViewOverlay {
	public RouteInstructionsService routeInstructionsController;
	public GeoPoint currentLocation;
	public OpenStreetMapViewTouchResponderOverlay(RouteInstructionsService routeInstructionsController) {
		this.routeInstructionsController = routeInstructionsController;
	}
	
	@Override
	protected void onDraw(Canvas c, OpenStreetMapView osmv) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public boolean onLongPress(MotionEvent e, OpenStreetMapView osmv) {
		OpenStreetMapViewProjection proj = osmv.getProjection();
		
		GeoPoint selectedGeoPoint = proj.fromPixels(e.getX(), e.getY());
		Log.v(OpenSatNavConstants.LOG_TAG, "Felt a long press" + selectedGeoPoint.toDoubleString());
		createContextMenu(selectedGeoPoint, osmv);
		return true;
	}
	
	public void createContextMenu(final GeoPoint to, OpenStreetMapView osmv) {
		Builder builder = new AlertDialog.Builder(osmv.getContext());
		Resources resources = osmv.getResources();
		String[] res = new String[]{
				resources.getString(R.string.navigate_contextmenu_navigate),
				//resources.getString(R.string.navigate_contextmenu_aroundpoint),

		};
		builder.setIcon(R.drawable.direction_arrow);
		builder.setItems(res, new DialogInterface.OnClickListener(){
			

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.v(OpenSatNavConstants.LOG_TAG, "Pressed " + which);    
					if(which == 0){
						
						
						routeInstructionsController.refreshRoute(currentLocation, to, "car");
                    } else if(which == 1){
                           
                    }
			}
		});
		builder.create().show();
		
	}

	public void setLocation(GeoPoint currentLocation) {
		this.currentLocation = currentLocation;
		
	}
}
