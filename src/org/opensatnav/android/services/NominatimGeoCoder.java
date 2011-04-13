package org.opensatnav.android.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.util.HttpUserAgentHelper;
import org.opensatnav.android.OpenSatNavConstants;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class NominatimGeoCoder implements GeoCoder {
	private String url;
	private ArrayList<String> locationNames;
	private ArrayList locationLatitudes;
	private ArrayList locationLongitudes;
	private ArrayList<String> locationInfo;
	URL encodedURL;
	

	@Override
	public Bundle query(String query, GeoPoint userLocation, int mode, int maxResults,
			Context context) {
		locationNames = new ArrayList<String>();
		locationLatitudes = new ArrayList<int[]>();
		locationLongitudes = new ArrayList<int[]>();
		locationInfo = new ArrayList<String>();
					
			// see http://wiki.openstreetmap.org/wiki/Nominatim for URL available parameters
			
			String language = Locale.getDefault().getLanguage();
			ReverseGeoCoder revGeoCoder = new NominatimReverseGeoCoder();
			Bundle locationData = revGeoCoder.query(userLocation, context);
			url = new String("http://nominatim.openstreetmap.org/search?"	
					+ "format=xml"
					+ "&addressdetails=1"
					+ "&accept-language=" + language);
			if(mode==FROM_POINT) {
				String locationDescription = "";
				if(locationData.containsKey("village"))
					locationDescription += locationData.getString("village")+", ";
				if(locationData.containsKey("town"))
					locationDescription += locationData.getString("town")+", ";
				if(locationData.containsKey("suburb"))
					locationDescription += locationData.getString("suburb")+", ";
				if(locationData.containsKey("city"))
					locationDescription += locationData.getString("city")+", ";
				if(locationData.containsKey("country"))
					locationDescription += locationData.getString("country");
				url += "&q="+URLEncoder.encode(query+" near "+locationDescription);
			}
			else if (mode == IN_AREA) {
				String locationDescription = "";
				if(locationData.containsKey("town"))
					locationDescription += locationData.getString("town")+", ";
				if(locationData.containsKey("city"))
					locationDescription += locationData.getString("city")+", ";
				if(locationData.containsKey("country"))
					locationDescription += locationData.getString("country");
				url += "&q="+URLEncoder.encode(query+", "+locationDescription);
			}
			Log.i(OpenSatNavConstants.LOG_TAG, url);		
	    try {		
	    	encodedURL = new URL(url);
		} catch (MalformedURLException e) {
			Log.e(OpenSatNavConstants.LOG_TAG, e.getMessage(), e);
		}
		try {
			URLConnection urlConn = encodedURL.openConnection();
			String userAgent = HttpUserAgentHelper.getUserAgent(context);
			if (userAgent != null)
				urlConn.setRequestProperty("User-Agent", userAgent);
			urlConn.setReadTimeout(60000);
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(urlConn.getInputStream());
			NodeList xml = doc.getChildNodes();
			// if we have at least 1 result
			//android 2.2 starts at item(0) while previous versions start at item(1) so we deal with both here
			if ((xml.item(0).getChildNodes().item(1) != null) || (xml.item(1).getChildNodes().item(1) != null)) {
				 NodeList places = xml.item(0).getChildNodes();
				 if(places.getLength()==0)
				    places = xml.item(1).getChildNodes();
				 for (int i = 1; i < places.getLength(); i++) {
					NamedNodeMap attributes = places.item(i).getAttributes();
					Log.d("NOMINATIMGEOCODER", "found location: "
							+ attributes.getNamedItem("display_name").getNodeValue());
					locationNames.add(places.item(i).getChildNodes().item(0).getFirstChild().getNodeValue());
					// convert to integer (E6 format)
					locationLatitudes.add((int) (Float.parseFloat(attributes
							.getNamedItem("lat").getNodeValue()) * 1000000));
					locationLongitudes.add((int) (Float.parseFloat(attributes
							.getNamedItem("lon").getNodeValue()) * 1000000));
					//moreInfo is the second line, it displays the type followed by the full address
					String moreInfo = null;
					NodeList locationDetails = places.item(i).getChildNodes();
					if (attributes.getNamedItem("type") != null)
						moreInfo = attributes.getNamedItem("type").getNodeValue();
					StringBuilder sb = new StringBuilder(moreInfo);
					for(int j=1;j<locationDetails.getLength(); j++){
						if(!locationDetails.item(j).getNodeName().equals("country_code")){
							sb.append(", ");
							sb.append(locationDetails.item(j).getFirstChild().getNodeValue());
						}
					}
					if (moreInfo != null)
						locationInfo.add(sb.toString());
					else
						locationInfo.add(" ");
				}
			}
			// no results
			else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("OSMGEOCODER", "Network timeout");
			return null;
		}
		Bundle bundle = new Bundle();
		// should have done this better - didn't know Java had issues like this!
		int[] latArray = new int[locationLatitudes.size()];
		int[] lonArray = new int[locationLatitudes.size()];
		String[] nameArray = new String[locationNames.size()];
		String[] infoArray = new String[locationInfo.size()];

		System.arraycopy(locationNames.toArray(), 0, nameArray, 0,
				locationNames.size());
		System.arraycopy(locationInfo.toArray(), 0, infoArray, 0, locationInfo
				.size());
		for (int i = 0; i < locationLatitudes.size(); i++)
			latArray[i] = (Integer) locationLatitudes.get(i);
		for (int i = 0; i < locationLatitudes.size(); i++)
			lonArray[i] = (Integer) locationLongitudes.get(i);

		bundle.putStringArray("names", nameArray);
		bundle.putIntArray("latitudes", latArray);
		bundle.putIntArray("longitudes", lonArray);
		bundle.putStringArray("info", infoArray);
		return bundle;

	}

}
