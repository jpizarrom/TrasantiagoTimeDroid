package org.opensatnav.android.services.routing.overlay;

import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.opensatnav.android.services.routing.RouteInstruction;
import cl.droid.transantiago.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;


public class NavigationHUD extends OpenStreetMapViewOverlay {
	
	
	
	
	Bitmap slight_left_turn;
	Bitmap left_turn;
	Bitmap sharp_left_turn;
	Bitmap slight_right_turn;
	Bitmap right_turn;
	Bitmap sharp_right_turn;
	Bitmap straight_on;
	Double distanceToNextPoint;

	public RouteInstruction nextRouteInstruction;
	
	
	public NavigationHUD(Context c) {
		slight_left_turn = BitmapFactory.decodeResource(c.getResources(), R.drawable.turn_left_25_white);
		left_turn = BitmapFactory.decodeResource(c.getResources(), R.drawable.turn_left_45_white);
		sharp_left_turn = BitmapFactory.decodeResource(c.getResources(), R.drawable.turn_left_90_white);
		
		slight_right_turn = BitmapFactory.decodeResource(c.getResources(), R.drawable.turn_right_25_white);
		right_turn = BitmapFactory.decodeResource(c.getResources(), R.drawable.turn_right_45_white);
		sharp_right_turn = BitmapFactory.decodeResource(c.getResources(), R.drawable.turn_right_90_white);
		
		straight_on = BitmapFactory.decodeResource(c.getResources(), R.drawable.turn_straight_white);
	}
	
	@Override
	protected void onDraw(Canvas c, OpenStreetMapView osmv) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {
		Paint textPaint = new Paint();
		textPaint.setTextSize(20);
		Paint backgroundPaint = new Paint();
		backgroundPaint.setARGB(100, 100, 100, 100);
		c.drawRect(0, 0, 320, 70, backgroundPaint);
		c.drawText(distanceToNextPoint.intValue() + "m", 60, 40, textPaint);
		c.drawRect(0, 70, 320, 90, backgroundPaint);
		Paint instructionPaint = new Paint();
		instructionPaint.setTextSize(13);
		c.drawText(nextRouteInstruction.description, 5, 80, instructionPaint);
		
		Paint bitmapPaint = new Paint();
		
		c.drawBitmap(getBitmapForTurn(nextRouteInstruction.turnType), 3, 3, bitmapPaint);
	}
	
	
	private Bitmap getBitmapForTurn(String turnType) {
		if (turnType.equals("C")) {
			return straight_on;
		} else if (turnType.equals("TL")) {
			return left_turn;
		} else if (turnType.equals("TSLL")) {
			return slight_left_turn;
		} else if (turnType.equals("TSHL")) {
			return sharp_left_turn;
		} else if (turnType.equals("TR")) {
			return right_turn;
		} else if (turnType.equals("TSLR")) {
			return slight_right_turn;
		} else if (turnType.equals("TSHR")) {
			return sharp_right_turn;
		} else {
			return straight_on;
		}
	}
	
	
}
