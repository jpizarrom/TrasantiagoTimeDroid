package org.opensatnav.android;

import org.opensatnav.android.contribute.content.TracksColumns;
import org.opensatnav.android.contribute.services.ITrackRecordingService;
import org.opensatnav.android.contribute.services.TrackRecordingService;
import org.opensatnav.android.contribute.util.MyTracksUtils;
import org.opensatnav.android.contribute.util.StringUtils;
import org.opensatnav.android.contribute.util.UnitConversions;
import org.opensatnav.android.contribute.util.constants.ContributeConstants;

import cl.droid.transantiago.R;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ContributeActivity extends ListActivity implements OnSharedPreferenceChangeListener, View.OnClickListener {
	Bundle gpsTracks = new Bundle();

	private static final int UPLOAD_NOW = 10;
	private static final int TRACE_TOGGLE = UPLOAD_NOW + 1;
	private static final int DELETE_TRACKS = TRACE_TOGGLE + 1;
	private static final int NEW_WAYPOINT = DELETE_TRACKS + 1;


	private boolean startNewTrackRequested = false;

	private Cursor tracksCursor = null;
	private ListView listView = null;
	private boolean metricUnits = true;
	private int contextPosition = -1;
	private long trackId = -1;

	/**
	 * The id of the currently recording track.
	 */
	private long recordingTrackId = -1;

	/**
	 * The id of the currently selected track.
	 */
	private long selectedTrackId = -1;

	/**
	 * Does the user want to share the current track.
	 */
	private boolean shareRequested = false;

	/**
	 * Utilities to deal with the database.
	 */
	private MyTracksUtils providerUtils;
	/**
	 * The connection to the track recording service.
	 */

	private ITrackRecordingService trackRecordingService;
	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d(OpenSatNavConstants.LOG_TAG, "MyTracks: Service now connected.");
			trackRecordingService = ITrackRecordingService.Stub.asInterface(service);
			if (startNewTrackRequested) {
				startNewTrackRequested = false;
				try {
					recordingTrackId = trackRecordingService.startNewTrack();

					setSelectedAndRecordingTrack(recordingTrackId, recordingTrackId);
				} catch (RemoteException e) {
					/*Toast.makeText(MyTracks.this,
	              R.string.error_unable_to_start_recording, Toast.LENGTH_SHORT)
	                  .show();*/
					Log.w(OpenSatNavConstants.LOG_TAG, "Unable to start recording.", e);
				}
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			Log.d(OpenSatNavConstants.LOG_TAG, "MyTracks: Service now disconnected.");
			trackRecordingService = null;
		}
	};
	@Override
	public void onCreate(Bundle onSavedInstance) {
		super.onCreate(onSavedInstance);
		setTitle(R.string.contribute_title);
		setContentView(R.layout.contribute);
		//setContentView(R.layout.mytracks_list);

		listView = getListView();
		listView.setOnCreateContextMenuListener(contextMenuListener);

		SharedPreferences preferences =
			getSharedPreferences(OpenSatNavConstants.SETTINGS_NAME, 0);
		preferences.registerOnSharedPreferenceChangeListener(this);
		metricUnits = true;
		recordingTrackId =
			preferences.getLong(OpenSatNavConstants.RECORDING_TRACK, -1);

		tracksCursor = getContentResolver().query(
				TracksColumns.CONTENT_URI, null, null, null, "_id DESC");
		startManagingCursor(tracksCursor);
		setListAdapter();





		SharedPreferences prefsTracks =
			getSharedPreferences(OpenSatNavConstants.SETTINGS_NAME, 0);
		if (prefsTracks != null) {
			selectedTrackId = prefsTracks.getLong(OpenSatNavConstants.SELECTED_TRACK, -1);
			recordingTrackId = prefsTracks.getLong(OpenSatNavConstants.RECORDING_TRACK, -1);
			prefsTracks.registerOnSharedPreferenceChangeListener(this);
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (!(prefs.contains(String.valueOf(R.string.pref_username_key))) && prefs.contains(String.valueOf(R.string.pref_password_key))); 
		TextView textInfo = (TextView) findViewById(R.id.textInfo);
		textInfo.setText(getText(R.string.prefs_contribute_osm_username) + " : " + prefs.getString(getString(R.string.pref_username_key), getString(R.string.contribute_username_not_entered)));
		Boolean tracing;
		tracing = isRecording();
		Button startButton = (Button) findViewById(R.id.startRecord);
		if (tracing == true) {
			startButton.setText(this.getResources().getText(
					R.string.contribute_stop_recording));
		}

		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startRecording();				
			}

		});
		Button stopButton = (Button) findViewById(R.id.stopRecord);
		stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopRecording();				
			}

		});

	}





	private void startRecording() {
		Log.v(OpenSatNavConstants.LOG_TAG, "ContributeActivity.startRecording");
		if (trackRecordingService == null) {

			startNewTrackRequested = true;
			Intent startIntent = new Intent(this, TrackRecordingService.class);
			startService(startIntent);
			tryBindTrackRecordingService();
		} else {
			try {
				recordingTrackId = trackRecordingService.startNewTrack();
				//Toast.makeText(this, getString(R.string.status_now_recording),
				//    Toast.LENGTH_SHORT).show();
				setSelectedAndRecordingTrack(recordingTrackId, recordingTrackId);
			} catch (RemoteException e) {
				//Toast.makeText(this,
				//   getString(R.string.error_unable_to_start_recording),
				//   Toast.LENGTH_SHORT).show();
				Log.e(OpenSatNavConstants.LOG_TAG,
						"Failed to start track recording service", e);
			}
		}

	}

	/**
	 * Stops the track recording service and unbinds from it. Will display a toast
	 * "Stopped recording" and pop up the Track Details activity.
	 */
	private void stopRecording() {
		if (trackRecordingService != null) {
			try {
				trackRecordingService.endCurrentTrack();
			} catch (RemoteException e) {
				Log.e(OpenSatNavConstants.LOG_TAG, "Unable to stop recording.", e);
			}
			setRecordingTrack(-1);
			/*Intent intent = new Intent(this, MyTracksDetails.class);
	      intent.putExtra("trackid", recordingTrackId);
	      intent.putExtra("hasCancelButton", false);
	      recordingTrackId = -1;
	      startActivity(intent);*/
		}
		tryUnbindTrackRecordingService();
		try {
			stopService(new Intent(this, TrackRecordingService.class));
		} catch (SecurityException e) {
			Log.e(OpenSatNavConstants.LOG_TAG,
					"Encountered a security exception when trying to stop service.", e);
		}
		trackRecordingService = null;
	}

	private void setRecordingTrack(final long trackId) {
		runOnUiThread(new Runnable() {
			public void run() {
				SharedPreferences prefs =
					getSharedPreferences(OpenSatNavConstants.SETTINGS_NAME, 0);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putLong(OpenSatNavConstants.RECORDING_TRACK, trackId);
				editor.commit();
			}
		});
	}

	/**
	 * Binds to track recording service if it is running.
	 */
	private void tryBindTrackRecordingService() {
		Log.d(OpenSatNavConstants.LOG_TAG,
		"MyTracks: Trying to bind to track recording service...");
		bindService(new Intent(this, TrackRecordingService.class),
				serviceConnection, 0);
	}

	/**
	 * Tries to unbind the track recording service. Catches exception silently in
	 * case service is not registered anymore.
	 */
	private void tryUnbindTrackRecordingService() {
		Log.d(OpenSatNavConstants.LOG_TAG,
		"MyTracks: Trying to unbind from track recording service...");
		try {
			unbindService(serviceConnection);
		} catch (IllegalArgumentException e) {
			Log.d(OpenSatNavConstants.LOG_TAG,
					"MyTracks: Tried unbinding, but service was not registered.", e);
		}
	}

	private void displayToast(String msg) {
		Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
	}

	private void displayToast(int stringReference) {
		displayToast((String) getText(stringReference));
	}

	private void setSelectedAndRecordingTrack(final long theSelectedTrackId,
			final long theRecordingTrackId) {
		runOnUiThread(new Runnable() {
			public void run() {
				SharedPreferences prefs =
					getSharedPreferences(OpenSatNavConstants.SETTINGS_NAME, 0);
				if (prefs != null) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putLong(OpenSatNavConstants.SELECTED_TRACK, theSelectedTrackId);
					editor.putLong(OpenSatNavConstants.RECORDING_TRACK, theRecordingTrackId);
					editor.commit();
				}
			}
		});
	}



	public boolean isRecording() {
		if (trackRecordingService == null) {
			return false;
		}
		try {
			return trackRecordingService.isRecording();
		} catch (RemoteException e) {
			Log.e(OpenSatNavConstants.LOG_TAG, "MyTracks: Remote exception.", e);
			return false;
		}
	}
	private final OnCreateContextMenuListener contextMenuListener =
		new OnCreateContextMenuListener() {
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			menu.setHeaderTitle(R.string.tracklist_this_track);
			AdapterView.AdapterContextMenuInfo info =
				(AdapterView.AdapterContextMenuInfo) menuInfo;
			contextPosition = info.position;
			trackId = ContributeActivity.this.listView.getAdapter().getItemId(
					contextPosition);
			Log.v(OpenSatNavConstants.LOG_TAG, "isRecording is " + isRecording());

			Log.v(OpenSatNavConstants.LOG_TAG, "trackID is " + trackId);

			Log.v(OpenSatNavConstants.LOG_TAG, "recordingTrackId is " + recordingTrackId);
			/*menu.add(0, MyTracksConstants.MENU_SHOW, 0,
	              R.string.tracklist_show_track);
	          menu.add(0, MyTracksConstants.MENU_EDIT, 0,
	              R.string.tracklist_edit_track);*/
			if (isRecording() == false
					|| trackId != recordingTrackId) {
				menu.add(0, ContributeConstants.MENU_SEND_TO_OSM, 0,
						R.string.tracklist_send_to_osm);
				//TODO: Do these!
				/*SubMenu share = menu.addSubMenu(0, ContributeConstants.MENU_SHARE, 0,
						R.string.tracklist_share_track);
				share.add(0, ContributeConstants.MENU_SHARE_GPX_FILE, 0,
						R.string.tracklist_share_gpx_file);
				share.add(0, ContributeConstants.MENU_SHARE_KML_FILE, 0,
						R.string.tracklist_share_kml_file);
				share.add(0, ContributeConstants.MENU_SHARE_CSV_FILE, 0,
						R.string.tracklist_share_csv_file);
				SubMenu save = menu.addSubMenu(0,
						ContributeConstants.MENU_WRITE_TO_SD_CARD, 0,
						R.string.tracklist_write_to_sd);
				save.add(0, ContributeConstants.MENU_SAVE_GPX_FILE, 0,
						R.string.tracklist_save_as_gpx);
				save.add(0, ContributeConstants.MENU_SAVE_KML_FILE, 0,
						R.string.tracklist_save_as_kml);
				save.add(0, ContributeConstants.MENU_SAVE_CSV_FILE, 0,
						R.string.tracklist_save_as_csv);
				menu.add(0, ContributeConstants.MENU_DELETE, 0,
						R.string.tracklist_delete_track);*/
			}
		}
	};

	@Override
	public void onSharedPreferenceChanged(
			SharedPreferences sharedPreferences, String key) {
		if (key == null) {
			return;
		}
		//if (key.equals(OpenSatNavConstants.METRIC_UNITS)) {
		metricUnits = true;//sharedPreferences.getBoolean(OpenSatNavConstants.METRIC_UNITS, true);
		if (tracksCursor != null && !tracksCursor.isClosed()) {
			tracksCursor.requery();
		}
		//}
		if (key.equals(OpenSatNavConstants.RECORDING_TRACK)) {
			recordingTrackId = sharedPreferences.getLong(OpenSatNavConstants.RECORDING_TRACK, -1);
		}
	}

	//TODO: this would be nice!
	/*@Override
	  protected void onListItemClick(ListView l, View v, int position, long id) {
	    Intent result = new Intent();
	    result.putExtra("trackid", id);
	    setResult(OpenSatNavConstants.SHOW_TRACK, result);
	    finish();
	  }*/

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (!super.onMenuItemSelected(featureId, item)) {
			Log.v(OpenSatNavConstants.LOG_TAG, "ItemID clicked was "+ item.getItemId());
			switch (item.getItemId()) {
			/*case OpenSatNavConstants.MENU_SHOW: {
	          onListItemClick(null, null, 0, trackId);
	          return true;
	        }
	        case OpenSatNavConstants.MENU_EDIT: {
	          Intent intent = new Intent(this, MyTracksDetails.class);
	          intent.putExtra("trackid", trackId);
	          startActivity(intent);
	          return true;
	        }*/


			case ContributeConstants.MENU_SHARE:
			case ContributeConstants.MENU_WRITE_TO_SD_CARD:
				return false;
			default: {
				Intent result = new Intent();
				result.putExtra("trackid", trackId);
				setResult(
						ContributeConstants.getActionFromMenuId(item.getItemId()), result);
				finish();
				return true;
			}
			}
		}
		return false;
	}

	private void setListAdapter() {
		// Get a cursor with all tracks
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
				this,
				R.layout.contribute_list_item,
				tracksCursor,
				new String[] { TracksColumns.NAME, TracksColumns.STARTTIME,
						TracksColumns.TOTALDISTANCE, TracksColumns.DESCRIPTION,
						TracksColumns.CATEGORY },
						new int[] { R.id.trackdetails_item_name, R.id.trackdetails_item_time,
						R.id.trackdetails_item_stats, R.id.trackdetails_item_description,
						R.id.trackdetails_item_category });

		final int startTimeIdx =
			tracksCursor.getColumnIndexOrThrow(TracksColumns.STARTTIME);
		final int totalTimeIdx =
			tracksCursor.getColumnIndexOrThrow(TracksColumns.TOTALTIME);
		final int totalDistanceIdx =
			tracksCursor.getColumnIndexOrThrow(TracksColumns.TOTALDISTANCE);

		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				TextView textView = (TextView) view;
				if (columnIndex == startTimeIdx) {
					long time = cursor.getLong(startTimeIdx);
					//TODO: Make this more human friendly
					textView.setText(String.format("%tc", time));
				} else if (columnIndex == totalDistanceIdx) {
					double length = cursor.getDouble(totalDistanceIdx);
					String lengthUnit = null;
					if (metricUnits) {
						if (length > 1000) {
							length /= 1000;
							lengthUnit = getString(R.string.kilometer);
						} else {
							lengthUnit = getString(R.string.meter);
						}
					} else {
						if (length > UnitConversions.MI_TO_M) {
							length /= UnitConversions.MI_TO_M;
							lengthUnit = getString(R.string.mile);
						} else {
							length *= UnitConversions.M_TO_FT;
							lengthUnit = getString(R.string.feet);
						}
					}
					textView.setText(String.format("%s  %.2f %s",
							StringUtils.formatTime(cursor.getLong(totalTimeIdx)),
							length,
							lengthUnit));
				} else {
					textView.setText(cursor.getString(columnIndex));
					if (textView.getText().length() < 1) {
						textView.setVisibility(View.GONE);
					} else {
						textView.setVisibility(View.VISIBLE);
					}
				}
				return true;
			}
		});
		setListAdapter(adapter);
	}
	@Override
	public void onClick(View v) {

		//Do nothing. Grr
	}

}