package cl.droid.transantiago.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opensatnav.android.services.GeoCoder;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class TransantiagoGeoCoder implements GeoCoder {
	private static final String TAG = "TransantiagoGeoCoder";

	public final static String urlbase = "http://dev.planotur.cl/transdroid/v1";
	private ArrayList<String> locationNames;
	private ArrayList locationLatitudes;
	private ArrayList locationLongitudes;
	private ArrayList<String> locationInfo;
	private ArrayList<String> locationParaderos;
	private String ads;
	private String q = "";

	URL encodedURL;

	public Bundle query(String query, GeoPoint from, int mode, int maxResults,
			Context context, String bbox) {
		Log.i(TAG, bbox);
		this.q = "&bbox=" + bbox;
		return this.query(query, from, mode, maxResults, context);
	}

	public Bundle query(String query, GeoPoint from, int mode, int maxResults,
			Context context, String lat, String lon) {
		this.q = "&lat=" + lat + "&lon=" + lon;
		this.q += "&tolerance=0.005";
		return this.query(query, from, mode, maxResults, context);
	}

	@Override
	public Bundle query(String query, GeoPoint from, int mode, int maxResults,
			Context context) {
		locationNames = new ArrayList<String>();
		locationLatitudes = new ArrayList<int[]>();
		locationLongitudes = new ArrayList<int[]>();
		locationInfo = new ArrayList<String>();
		locationParaderos = new ArrayList<String>();

		InputStream in = null;
		OutputStream out = null;

		try {
			String surl = urlbase + "/busstops?limit=" + maxResults + "";
			surl += q;
			Log.i(TAG, "surl=" + surl);
			URL url = new URL(surl);

			in = new BufferedInputStream(url.openStream(),
					StreamUtils.IO_BUFFER_SIZE);

			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream,
					StreamUtils.IO_BUFFER_SIZE);
			StreamUtils.copy(in, out);
			out.flush();

			String str = dataStream.toString();
			JSONObject json = new JSONObject(str);

			JSONArray results = (JSONArray) ((JSONObject) json).get("features");

			Log.i(TAG, "results.length=" + results.length());
			if (results.length() == 0) {
				// Toast.makeText(this, R.string.no_items,
				// Toast.LENGTH_SHORT).show();
				// return;
			}
			for (int i = 0; i < results.length(); i++) {
				JSONObject res = results.getJSONObject(i);
				JSONObject properties = res.getJSONObject("properties");
				JSONArray coordinates = (JSONArray) (res
						.getJSONObject("geometry")).get("coordinates");

				final String address = properties.getString("code");
				locationNames.add(address);
				// convert to integer (E6 format)
				locationLatitudes
						.add((int) (coordinates.getDouble(1) * 1000000));
				locationLongitudes
						.add((int) (coordinates.getDouble(0) * 1000000));
				locationInfo.add(properties.getString("name"));
			}

		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(context, "R.string.no_inet_conn",
			// Toast.LENGTH_LONG).show();
			return null;
		} finally {
			StreamUtils.closeStream(in);
			StreamUtils.closeStream(out);
		}

		Bundle bundle = new Bundle();
		// should have done this better - didn't know Java had issues like this!
		int[] latArray = new int[locationLatitudes.size()];
		int[] lonArray = new int[locationLongitudes.size()];
		String[] nameArray = new String[locationNames.size()];
		String[] infoArray = new String[locationInfo.size()];

		System.arraycopy(locationNames.toArray(), 0, nameArray, 0,
				locationNames.size());
		System.arraycopy(locationInfo.toArray(), 0, infoArray, 0,
				locationInfo.size());
		for (int i = 0; i < locationLatitudes.size(); i++)
			latArray[i] = (Integer) locationLatitudes.get(i);
		for (int i = 0; i < locationLatitudes.size(); i++)
			lonArray[i] = (Integer) locationLongitudes.get(i);

		bundle.putStringArray("names", nameArray);
		bundle.putIntArray("latitudes", latArray);
		bundle.putIntArray("longitudes", lonArray);
		bundle.putStringArray("info", infoArray);
		bundle.putInt("size", locationNames.size());
		return bundle;
	}

	public Bundle queryService(String query, GeoPoint from, int mode,
			int maxResults, Context context) {
		locationNames = new ArrayList<String>();
		locationLatitudes = new ArrayList<int[]>();
		locationLongitudes = new ArrayList<int[]>();
		locationInfo = new ArrayList<String>();
		ads = "";

		InputStream in = null;
		OutputStream out = null;

		try {
			URL url = new URL(
					urlbase + "/services/byparadero_simt/" + query
							+ "");

			in = new BufferedInputStream(url.openStream(),
					StreamUtils.IO_BUFFER_SIZE);

			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream,
					StreamUtils.IO_BUFFER_SIZE);
			StreamUtils.copy(in, out);
			out.flush();

			String str = dataStream.toString();
			JSONObject json = new JSONObject(str);
			if (json.has("ads"))
				ads = json.getString("ads");
			JSONArray results = (JSONArray) ((JSONObject) json).get("features");
			// Ut.dd("results.length="+results.length());
			Log.i(TAG, "URL=" + url);
			Log.i(TAG, "results.length=" + results.length());
			if (results.length() == 0) {
				// Toast.makeText(this, R.string.no_items,
				// Toast.LENGTH_SHORT).show();
				// return;
			}
			for (int i = 0; i < results.length(); i++) {
				JSONObject res = results.getJSONObject(i);

				final String address = res.getString("servicio");
				locationNames.add(address);

				String loc = "";
				if (res.has("codigorespuesta")
						&& res.has("codigorespuesta")
						&& !res.getString("codigorespuesta").equals("")
						&& !(res.getString("codigorespuesta").equals("00") || res
								.getString("codigorespuesta").equals("01")))
					loc += res.getString("respuestaServicio") + "\n";

				if (res.has("destino_name") && res.has("destino_name")
						&& !res.getString("destino_name").equals(""))
					loc += "Destino " + res.getString("destino_name") + "\n";

				if (res.has("horaprediccionbus1") && res.has("distanciabus1")
						&& !res.getString("distanciabus1").equals(""))
					loc += "Bus1 " + res.getString("horaprediccionbus1") + "|"
							+ res.getString("distanciabus1") + "mts." + "\n";

				if (res.has("horaprediccionbus2") && res.has("distanciabus2")
						&& !res.getString("distanciabus2").equals(""))
					loc += "Bus2 " + res.getString("horaprediccionbus2") + "|"
							+ res.getString("distanciabus2") + "mts." + "\n";

				locationInfo.add(loc);
			}

		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(context, "R.string.no_inet_conn",
			// Toast.LENGTH_LONG).show();
			return null;
		} finally {
			StreamUtils.closeStream(in);
			StreamUtils.closeStream(out);
		}

		Bundle bundle = new Bundle();
		// should have done this better - didn't know Java had issues like this!
		int[] latArray = new int[locationLatitudes.size()];
		int[] lonArray = new int[locationLatitudes.size()];
		String[] nameArray = new String[locationNames.size()];
		String[] infoArray = new String[locationInfo.size()];

		System.arraycopy(locationNames.toArray(), 0, nameArray, 0,
				locationNames.size());
		System.arraycopy(locationInfo.toArray(), 0, infoArray, 0,
				locationInfo.size());
		for (int i = 0; i < locationLatitudes.size(); i++)
			latArray[i] = (Integer) locationLatitudes.get(i);
		for (int i = 0; i < locationLatitudes.size(); i++)
			lonArray[i] = (Integer) locationLongitudes.get(i);

		bundle.putStringArray("names", nameArray);
		bundle.putIntArray("latitudes", latArray);
		bundle.putIntArray("longitudes", lonArray);
		bundle.putStringArray("info", infoArray);
		bundle.putString("ads", ads);
		return bundle;
	}

}
