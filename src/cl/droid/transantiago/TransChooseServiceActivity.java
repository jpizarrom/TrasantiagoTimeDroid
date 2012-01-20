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
package cl.droid.transantiago;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.opensatnav.android.OpenSatNavConstants;
import org.opensatnav.android.services.GeoCoder;
import org.opensatnav.android.util.FormatHelper;
import org.osmdroid.util.GeoPoint;

import cl.droid.transantiago.services.TransantiagoGeoCoder;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TransChooseServiceActivity extends ListActivity {

	protected ProgressDialog progress;
	Bundle b;
	String[] locationInfo;
	String[] locationNames;;
	ImageView ads;
	
	@Override
	public void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.services_list);
		
		ads = (ImageView)this.findViewById(R.id.ads);
//		Uri uri= Uri.parse("http://198.41.36.27:8080/admMarketing/img/11273693.jpg");
//		ads.setImageURI(uri);
		BitmapFactory.Options bmOptions;
	    bmOptions = new BitmapFactory.Options();
	    bmOptions.inSampleSize = 1;
	    Bitmap bm = loadImage("http://198.41.36.27:8080/admMarketing/img/0125137911.gif", bmOptions);
//	    ads.setImageBitmap(bm);
		
		GeoPoint from = GeoPoint.fromDoubleString(getIntent().getStringExtra("fromLocation"), ',');
		final String paradero = getIntent().getStringExtra("paradero");
		
		b = getIntent().getBundleExtra("locations");
		locationInfo = b.getStringArray("info");
		locationNames = b.getStringArray("names");
		
		setTitle(this.getResources().getText(R.string.busstop) + " : " + paradero 
//				+ " - " + this.getResources().getText(R.string.choose_location_service)
				);
		// Set the title
		((TextView) findViewById(R.id.title_text)).setText(paradero);
		
		final LocationAdapter la = new LocationAdapter(from);
		setListAdapter(la);
//		getListView().setTextFilterEnabled(true);
//		getListView().setOnItemClickListener();
				
//		OnItemClickListener showTimesFromWeb = new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long position) {
//				final String servicio = la.getServicio((int)position);
//				progress = ProgressDialog.show(
//						TransChooseServiceActivity.this, TransChooseServiceActivity.this.getResources().getText(
//								R.string.please_wait), TransChooseServiceActivity.this.getResources().getText(
//								R.string.searching), true, true);
//				final Handler handler = new Handler() {
//					@Override
//					public void handleMessage(Message msg) {
//						if (progress.isShowing())
//							try {
//								progress.dismiss();
////								backgroundThreadComplete = true;
//							} catch (IllegalArgumentException e) {
//								// if orientation change, thread continue but the dialog cannot be dismissed without exception
//							}
//					}
//				};
//				new Thread(new Runnable() {
//					public void run() {
//						// put long running operations here
//						String r = "";
////						InputStream in = null;
////						OutputStream out = null;
//						ArrayList <NameValuePair> params;
////						
////						HttpClient httpclient = new DefaultHttpClient();
////						HttpResponse response=null;
////						HttpGet httpget = new HttpGet("http://web.simt.cl/simtweb/buscar.action");
////
////						httpget.setHeader("User-Agent","Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_7; en-us) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Safari/ 530.17");
////						httpget.setHeader("Accept", "application/xml, application/xhtml+xml, text/plain;q=0.8, image/png, */*;q=0.5");
////						httpget.setHeader("Accept-Charset","utf-8, iso-8859-1, utf-16, *;q=0.7");
//////						httpget.setHeader("Accept-Encoding","gzip");
////						httpget.setHeader("Accept-Language","es-ES, en-US");
////						
////						httpget.setHeader("Keep-Alive","300");
////						httpget.setHeader("Connection","keep-alive");
////						
//////						httpget.setHeader("Content-type", "text/html;charset=UTF-8");
//////						httpget.setHeader("Referer","http://web.simt.cl/simtweb/cargar.action");
//////						httpget.setHeader("Host","web.simt.cl");
//////						httpget.setHeader("Cookie","JSESSIONID=B273A06B7923DEDFD6F182C07136C214");
////
////						
////											
////						for (int i = 0; i<httpget.getAllHeaders().length;i++)
////							Log.i(OpenSatNavConstants.LOG_TAG, httpget.getAllHeaders()[i].toString());
////						
////						try {
////							response = httpclient.execute(httpget);
////							
////							in = response.getEntity().getContent();
////							final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
////							out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
////							StreamUtils.copy(in, out);
////							out.flush();
////							
////							for (int i = 0; i<response.getAllHeaders().length;i++)
////								Log.i(OpenSatNavConstants.LOG_TAG, response.getAllHeaders()[i].toString());
////							
////							Log.i(OpenSatNavConstants.LOG_TAG, dataStream.toString());
////
////						} catch (ClientProtocolException e1) {
////							// TODO Auto-generated catch block
////							e1.printStackTrace();
////						} catch (IOException e1) {
////							// TODO Auto-generated catch block
////							e1.printStackTrace();
////						}
////						
////						try {
//////							String co = response.getFirstHeader("Set-Cookie").getValue();
//////							httpget.setHeader("Cookie",co.substring(0, co.indexOf(";")));
////							for (int i = 0; i<httpget.getAllHeaders().length;i++)
////								Log.i(OpenSatNavConstants.LOG_TAG, httpget.getAllHeaders()[i].toString());
////							
////							response = httpclient.execute(httpget);
////							
////							in = response.getEntity().getContent();
////							final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
////							out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
////							StreamUtils.copy(in, out);
////							out.flush();
////							
////							for (int i = 0; i<response.getAllHeaders().length;i++)
////								Log.i(OpenSatNavConstants.LOG_TAG, response.getAllHeaders()[i].toString());
////							
////							Log.i(OpenSatNavConstants.LOG_TAG, dataStream.toString());
////
////						} catch (ClientProtocolException e1) {
////							// TODO Auto-generated catch block
////							e1.printStackTrace();
////						} catch (IOException e1) {
////							// TODO Auto-generated catch block
////							e1.printStackTrace();
////						}
////						
//					    params = new ArrayList<NameValuePair>();
//					    params.add(new BasicNameValuePair("accion", "normal"));
//					    params.add(new BasicNameValuePair("servicio", "101"));
//					    params.add(new BasicNameValuePair("destino", "33824"));
//					    params.add(new BasicNameValuePair("ejeCruce", "LA PLATA / DORSAL"));
//					    params.add(new BasicNameValuePair("paradero", "PB10"));
//					    
//					    //add parameters
//		                String combinedParams = "";
//		                if(!params.isEmpty()){
//		                    combinedParams += "?";
//		                    for(NameValuePair p : params)
//		                    {
//		                        String paramString = "";
//		                        try {
//									paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");
//								} catch (UnsupportedEncodingException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//		                        if(combinedParams.length() > 1)
//		                        {
//		                            combinedParams  +=  "&" + paramString;
//		                        }
//		                        else
//		                        {
//		                            combinedParams += paramString;
//		                        }
//		                    }
//		                }
//						String urlstring = "http://web.simt.cl/simtweb/buscar.action";
////		                Log.i(OpenSatNavConstants.LOG_TAG, urlstring+combinedParams);	
////						
////					    HttpPost httppost = new HttpPost(urlstring);
////					    httppost.setHeader("User-Agent","Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_7; en-us) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Safari/ 530.17");
////					    httppost.setHeader("Accept", "application/xml, application/xhtml+xml, text/plain;q=0.8, image/png, */*;q=0.5");
////					    httppost.setHeader("Accept-Charset","utf-8, iso-8859-1, utf-16, *;q=0.7");
//////						httppost.setHeader("Accept-Encoding","gzip");
////					    httppost.setHeader("Accept-Language","es-ES, en-US");
////					    
////					    httppost.setHeader("Keep-Alive","300");
////					    httppost.setHeader("Connection","keep-alive");
////					    httppost.setHeader("Pragma","no-cache");
////					    httppost.setHeader("Cache-Control","no-cache");
////					    				    
////						for (int i = 0; i<httppost.getAllHeaders().length;i++)
////							Log.i(OpenSatNavConstants.LOG_TAG, httppost.getAllHeaders()[i].toString());
////					    				    
////					    if(!params.isEmpty()){
////					    	try {
////								httppost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
////							} catch (UnsupportedEncodingException e) {
////								// TODO Auto-generated catch block
////								e.printStackTrace();
////							}
////		                }
////					    
////					    try {
////							response = httpclient.execute(httppost);
////							
////							in = response.getEntity().getContent();
////							final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
////							out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
////							StreamUtils.copy(in, out);
////							out.flush();
////							
////							r = dataStream.toString();
////						} catch (ClientProtocolException e) {
////							// TODO Auto-generated catch block
////							e.printStackTrace();
////						} catch (IOException e) {
////							// TODO Auto-generated catch block
////							e.printStackTrace();
////						}
//						
//						Log.i(OpenSatNavConstants.LOG_TAG, "results.length="+r);
//						urlstring = "http://m.ibus.cl/index.jsp?paradero="+paradero.toLowerCase()+"&servicio="+servicio+"&boton.x=66&boton.y=14";
////						urlstring = "http://web.simt.cl/simtweb/buscarAction.do?d=busquedaRapida&servicio=-1&destino=-1&paradero=-1&busqueda_rapida="+paradero+"+"+servicio+"&ingresar_paradero=PC616";
//						Log.i(OpenSatNavConstants.LOG_TAG, urlstring);
//						Intent intent = new Intent(TransChooseServiceActivity.this,
//								cl.droid.transantiago.ServiceActivity.class);
//						intent.putExtra("url", urlstring);
//						intent.putExtra("params", combinedParams);
//						startActivity(intent);
//
//						// ok, we are done
//						handler.sendEmptyMessage(0);
//						
//					}
//				}).start();
////				Intent data = getIntent();
//////				data.putExtra("location", la.getLocation((int) position).toString());
////				setResult(RESULT_OK, data);
////				finish();
//
//			}
//
//		};
	}

	protected class LocationAdapter extends BaseAdapter {

		GeoPoint from;
//		Bundle b = getIntent().getBundleExtra("locations");
//		String[] locationInfo = b.getStringArray("info");
//		String[] locationNames = b.getStringArray("names");
//		final int[] locationLats = b.getIntArray("latitudes");
//		final int[] locationLongs = b.getIntArray("longitudes");

		public LocationAdapter(GeoPoint from) {
			this.from = from;
		}

		@Override
		public int getCount() {
			return locationNames.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout mainView = new LinearLayout(TransChooseServiceActivity.this);
			mainView.setOrientation(LinearLayout.VERTICAL);
			
			TextView placeView = new TextView(TransChooseServiceActivity.this);
			TextView infoView = new TextView(TransChooseServiceActivity.this);
			TextView distanceView = new TextView(TransChooseServiceActivity.this);
			//add name
			String place = locationNames[position];
			// add unnamed text for places that need it
			if (place==null || place.length() == 0)
				place = (String) TransChooseServiceActivity.this.getResources().getText(R.string.unnamed_place);
			// add location type
			String info = locationInfo[position];
//			info = info.substring(0,1).toUpperCase()+info.substring(1);
			// add distance away
//			String distance = 
//				new FormatHelper(getBaseContext()).formatDistanceFuzzy
//				(from.distanceTo(new GeoPoint(locationLats[position], locationLongs[position])))
//				+ " " + ChooseServiceActivity.this.getResources().getText(R.string.away);
			
			placeView.setText(place);
			placeView.setTextSize(20);
			placeView.setTextColor(Color.WHITE);
			infoView.setText(info);
//			distanceView.setText(distance);
			
			mainView.addView(placeView, 0);
			mainView.addView(infoView, 1);
//			mainView.addView(distanceView, 2);
			
			return mainView;
		}

//		public GeoPoint getLocation(int position) {
//			return new GeoPoint(locationLats[position], locationLongs[position]);
//
//		}
		public String getServicio(int position) {
		return locationNames[position];

		}
		

	}

	   private Bitmap loadImage(final String URL, final BitmapFactory.Options options)
	   {
		   final Handler handler = new Handler() {
//			   @Override
//			   public void handleMessage(Message msg) {
//				   ads.setImageBitmap(bm);
//			   }
		   };
		   new Thread(new Runnable() {
			   public void run() {
				   InputStream in = null;       
				   try {
					   in = OpenHttpConnection(URL);
					   final Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
					   in.close();
					   
					   handler.post(new Runnable() {
                           public void run() {
                               if (ads != null &&  bitmap!= null) {
                            	   ads.setImageBitmap(bitmap);
                               }
                           }
                       });
				   } catch (IOException e1) {
					   handler.sendEmptyMessage(0);
				   } catch (Exception e1) {
					   handler.sendEmptyMessage(0);
				   }
				   //return bitmap;   
			   }
			   }
		   ).start();
		   return null;
	   }
	   private InputStream OpenHttpConnection(String strURL) throws IOException{
		   InputStream inputStream = null;
		   URL url = new URL(strURL);
		   URLConnection conn = url.openConnection();

		   try{
		    HttpURLConnection httpConn = (HttpURLConnection)conn;
		    httpConn.setRequestMethod("GET");
		    httpConn.connect();

		    if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
		     inputStream = httpConn.getInputStream();
		    }
		   }
		   catch (Exception ex)
		   {
		   }
		   return inputStream;
		  }
	
}
