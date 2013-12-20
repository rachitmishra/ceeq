package in.ceeq.services;

import in.ceeq.helpers.Logger;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.services.Locater.RequestType;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class Tracker extends Service implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	// private static final String CURRENT_LOCATION_ACTION =
	// "in.ceeq.ACTION_CURRENT_LOCATION";
	// private static final String NEW_LOCATION_ACTION =
	// "in.ceeq.ACTION_NEW_LOCATION";

	public static final String ACTION = "action";

	// private static final String SENDER_ADDRESS = "senderAddress";
	private LocationRequest locationRequest;
	private LocationClient locationClient;
	private Location location;
	private PreferencesHelper preferencesHelper;
	private RequestType requestType;
	private String senderAddress;
	private static final int INTERVAL = 1800000;
	private static final int FASTEST_INTERVAL = 60000;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(INTERVAL);
		locationRequest.setFastestInterval(FASTEST_INTERVAL);
		preferencesHelper = PreferencesHelper.getInstance(this);
		locationClient = new LocationClient(this, this, this);
		requestType = (RequestType) intent.getExtras().get(ACTION);
		// senderAddress = intent.getExtras().getString(SENDER_ADDRESS);
		Logger.d("Type : " + requestType + " has sender address : "
				+ senderAddress);
		locationClient.connect();
		return START_REDELIVER_INTENT;
	}

	@Override
	public void onLocationChanged(Location l) {
		preferencesHelper.setString(PreferencesHelper.LAST_LOCATION_LATITUDE,
				location.getLatitude() + "");
		preferencesHelper.setString(PreferencesHelper.LAST_LOCATION_LONGITUDE,
				location.getLongitude() + "");
		/*
		 * Intent newLocationUpdate = new Intent();
		 * newLocationUpdate.setAction(NEW_LOCATION_ACTION); switch
		 * (requestType) { case MESSAGE: newLocationUpdate.putExtra(ACTION,
		 * RequestType.MESSAGE); break; case PROTECT:
		 * newLocationUpdate.putExtra(ACTION, RequestType.PROTECT); break;
		 * default: break; } sendBroadcast(newLocationUpdate);
		 */
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onConnected(Bundle arg0) {
		Logger.d("Location client connected...");
		location = locationClient.getLastLocation();
		if (location == null)
			Logger.d("Location is null...");
		preferencesHelper.setString(PreferencesHelper.LAST_LOCATION_LATITUDE,
				location.getLatitude() + "");
		preferencesHelper.setString(PreferencesHelper.LAST_LOCATION_LONGITUDE,
				location.getLongitude() + "");
		/*
		 * Intent sendLocationUpdate = new Intent(this, Locations.class);
		 * sendLocationUpdate.setAction(CURRENT_LOCATION_ACTION);
		 * sendLocationUpdate.putExtra(ACTION, RequestType.TRACKER);
		 * sendBroadcast(sendLocationUpdate);
		 */
	}

	@Override
	public void onDestroy() {
		if (locationClient.isConnected()) {
			locationClient.removeLocationUpdates(this);
		}
		locationClient.disconnect();
	}

	@Override
	public void onDisconnected() {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
