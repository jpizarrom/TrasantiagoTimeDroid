package org.opensatnav.android.services;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.util.HttpUserAgentHelper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import org.opensatnav.android.OpenSatNavConstants;
import org.opensatnav.android.services.routing.CloudmadeXMLHandler;
import org.opensatnav.android.services.routing.Route;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.telephony.*;
import android.util.Log;

/**
 * 
 * @author Kieran Fleming
 * 
 */

public class CloudmadeRouter implements Router {
	private URL url;
	private Route route;
	private String API_KEY = "d61f711ae6614b0abf60411953091da4";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opensatnav.services.Router#getRoute(org.andnav.osm.util.GeoPoint,
	 * org.andnav.osm.util.GeoPoint, java.lang.String, android.content.Context)
	 */
	public Route getRoute(GeoPoint from, GeoPoint to, String vehicle, Context context) {
		String token = getToken(context);
		route = new Route();
		route.from = from;
		route.to = to;
		route.vehicle = vehicle;
		//Make this nicer
		if(vehicle.equals("motorcar")) {
			vehicle = "car";
		}
		try {
			
			
			url = new URL("http://routes.cloudmade.com/d61f711ae6614b0abf60411953091da4/api/0.3/" + from.toDoubleString() + "," + to.toDoubleString() + "/" + vehicle + "/fastest.gpx?token=" + token);
			Log.v(OpenSatNavConstants.LOG_TAG, "Using URL " + url.toString());
		} catch (MalformedURLException e) {
			Log.e(OpenSatNavConstants.LOG_TAG, e.getMessage(), e);
		}
		
		try {
			URLConnection conn = url.openConnection();
			String userAgent = HttpUserAgentHelper.getUserAgent(context);
			if (userAgent != null)
				conn.setRequestProperty("User-Agent", userAgent);
			conn.setReadTimeout(30000);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()), 8192);
			StringBuilder gpxBuilder = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				gpxBuilder.append(line + "\n");
			}
			in.close();
			String gpx = gpxBuilder.toString();
			
			try {
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();

		        /* Get the XMLReader of the SAXParser we created. */
		        XMLReader xr = sp.getXMLReader();
		        /* Create a new ContentHandler and apply it to the XML-Reader*/
		        Log.v(OpenSatNavConstants.LOG_TAG, "About to create XML Handler");
		        CloudmadeXMLHandler cloudmadeHandler = new CloudmadeXMLHandler();
		        xr.setContentHandler(cloudmadeHandler);
		       
		        /* Parse the xml-data from our URL. */
		        xr.parse(new InputSource(new StringReader(gpx)));
		        Log.v(OpenSatNavConstants.LOG_TAG, "Parsing done");
		        /* Parsing has finished. */
		        route = cloudmadeHandler.route;
		        Log.v(OpenSatNavConstants.LOG_TAG, "Got route");
		        /* Our ExampleHandler now provides the parsed data to us. */
		        
			} catch (Exception e) {
	            /* Display any Error to the GUI. */
	           Log.e(OpenSatNavConstants.LOG_TAG, "Error:", e);
	           
	    }
			//} else {
			//	throw new IOException();
			//}

		} catch (Exception e) {
			Log.e(OpenSatNavConstants.LOG_TAG, "Error:", e);
			return null;
		}
		// Log.d("OSMROUTER", "Route created");
		return route;

	}
	public String getToken(Context context) {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    TelephonyManager mTelephonyMgr = 
            (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);  
 
        String imei = mTelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE 
        imei = md5(imei);
	    HttpPost httppost = new HttpPost("http://auth.cloudmade.com/token/" + API_KEY + "?userid=" + imei);
	    String result = "";
	    try {
	        
	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        HttpEntity entity = response.getEntity();
	        if(entity != null){
	          InputStream inputStream = entity.getContent();
	           result = convertStreamToString(inputStream);
	        }
	        Log.v(OpenSatNavConstants.LOG_TAG, "Got App Token: " + result);
	        return result;
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
		return result;
	} 
	
	private static String convertStreamToString(InputStream is) {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder stringBuilder = new StringBuilder();
	 
	    String line = null;
	    try {
	      while ((line = reader.readLine()) != null) {
	        stringBuilder.append(line + "\n");
	      }
	    } catch (IOException e) {
	      e.printStackTrace();
	    } finally {
	      try {
	        is.close();
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	    }
	    return stringBuilder.toString();
	}
	
	public String md5(String s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();
	        
	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i<messageDigest.length; i++)
	            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
	        return hexString.toString();
	        
	    } catch (NoSuchAlgorithmException e) {
	        return s;
	    }
	}
}

