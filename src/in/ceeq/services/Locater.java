/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.services;

import in.ceeq.helpers.Logger;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.receivers.Locations;
import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class Locater extends IntentService implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	private static final String CURRENT_LOCATION_ACTION = "in.ceeq.ACTION_CURRENT_LOCATION";
	private static final String NEW_LOCATION_ACTION = "in.ceeq.ACTION_NEW_LOCATION";

	public Locater() {
		super("ServiceTracker");
		Logger.d("Locater started...");
	}

	public static final String ACTION = "action";

	public enum RequestType {
		BLIP, MESSAGE, SERVER, PROTECT, NOW, TRACKER
	}

	private static final String SENDER_ADDRESS = "senderAddress";
	private LocationRequest locationRequest;
	private LocationClient locationClient;
	private Location location;
	private PreferencesHelper preferencesHelper;
	private RequestType requestType;
	private String senderAddress;

	@Override
	protected void onHandleIntent(Intent intent) {

		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		preferencesHelper = PreferencesHelper.getInstance(this);
		locationClient = new LocationClient(this, this, this);

		requestType = (RequestType) intent.getExtras().get(ACTION);
		senderAddress = intent.getExtras().getString(SENDER_ADDRESS);
		Logger.d("Type : " + requestType + " has sender address : "
				+ senderAddress);
		locationClient.connect();
	}

	@Override
	public void onLocationChanged(Location l) {
		preferencesHelper.setString(PreferencesHelper.LAST_LOCATION_LATITUDE,
				location.getLatitude() + "");
		preferencesHelper.setString(PreferencesHelper.LAST_LOCATION_LONGITUDE,
				location.getLongitude() + "");
		Intent newLocationUpdate = new Intent();
		newLocationUpdate.setAction(NEW_LOCATION_ACTION);
		switch (requestType) {
		case MESSAGE:
			newLocationUpdate.putExtra(ACTION, RequestType.MESSAGE);
			newLocationUpdate.putExtra(SENDER_ADDRESS, senderAddress);
			break;
		case PROTECT:
			newLocationUpdate.putExtra(ACTION, RequestType.PROTECT);
			break;
		default:
			break;
		}
		sendBroadcast(newLocationUpdate);
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
		Intent sendLocationUpdate = new Intent(this, Locations.class);
		sendLocationUpdate.setAction(CURRENT_LOCATION_ACTION);
		switch (requestType) {
		case BLIP:
			sendLocationUpdate.putExtra(ACTION, RequestType.BLIP);
			break;
		case MESSAGE:
			sendLocationUpdate.putExtra(ACTION, RequestType.MESSAGE);
			sendLocationUpdate.putExtra(SENDER_ADDRESS, senderAddress);
			break;
		case PROTECT:
			sendLocationUpdate.putExtra(ACTION, RequestType.PROTECT);
			sendLocationUpdate.putExtra(SENDER_ADDRESS, senderAddress);
			break;
		case NOW:
			sendLocationUpdate.putExtra(ACTION, RequestType.NOW);
			sendLocationUpdate.putExtra(SENDER_ADDRESS, senderAddress);
			break;
		case SERVER:
			sendLocationUpdate.putExtra(ACTION, RequestType.SERVER);
			break;
		default:
			break;
		}
		Logger.d("Broadcasting location update...");
		sendBroadcast(sendLocationUpdate);
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

}
