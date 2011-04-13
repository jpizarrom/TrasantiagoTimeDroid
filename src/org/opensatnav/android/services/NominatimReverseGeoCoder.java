package org.opensatnav.android.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

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

public class NominatimReverseGeoCoder implements ReverseGeoCoder{
	private String url;
	URL encodedURL;
	
	@Override
	public Bundle query(GeoPoint location, Context context) {
		Bundle addressDetails = new Bundle();
		url = new String("http://nominatim.openstreetmap.org/reverse?"
				+ "lat="
				+ location.getLatitude()
				+ "&lon="
				+ location.getLongitude()
				+ "&format=xml"
				);
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
			 NodeList addressNode = xml.item(0).getChildNodes();
			 if(addressNode.getLength()==0)
				 addressNode = xml.item(1).getChildNodes();
			 NodeList addressParts = addressNode.item(2).getChildNodes();
			 for (int i = 0; i < addressParts.getLength(); i++) {
				 addressDetails.putString(addressParts.item(i).getNodeName(), addressParts.item(i).getFirstChild().getNodeValue());
				 Log.d("REVERSE",addressParts.item(i).getNodeName()+" => "+addressParts.item(i).getFirstChild().getNodeValue());
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
	return addressDetails;
	}

}
