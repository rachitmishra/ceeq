/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.services;

import in.ceeq.R;
import in.ceeq.commons.Utils;
import in.ceeq.home.HomeActivity;
import in.ceeq.receivers.CloudMessagesReceiver;
import in.ceeq.receivers.MobileMessagesReceiver;
import in.ceeq.services.LocationService.RequestType;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class CommandService extends IntentService {

	public CommandService() {
		super("ServiceCommander");
		Utils.d("Service commander started...");
	}

	public static final int NOTIFICATION_ID = 1;
	public static final String ACTION = "action";
	public static final String SENDER_ADDRESS = "senderAddress";
	public static final int NONE = 0;
	public static final int SIREN_ON = 1;
	public static final int SIREN_OFF = 2;
	public static final int RINGER_ON = 3;
	public static final int BACKUP = 4;
	public static final int WIPE = 5;
	public static final int LOCK = 6;
	public static final int SEND_CALLS_DETAILS_MESSAGE = 7;
	public static final int ENABLE_TRACKER = 8;
	public static final int SEND_CURRENT_DETAILS_MESSAGE = 9;
	public static final int SEND_PIN_FAIL_MESSAGE = 10;
	public static final int GET_LOCATION_FOR_MESSAGE = 11;
	public static final int GET_LOCATION_FOR_BLIP = 12;
	public static final int GET_LOCATION_FOR_PROTECT = 13;
	public static final int SEND_SIM_CHANGE_MESSAGE = 14;
	public static final int SEND_PROTECT_MESSAGE = 15;
	public static final int SEND_CURRENT_LOCATION_MESSAGE = 16;
	public static final int SEND_NEW_LOCATION_MESSAGE = 17;
	public static final int SEND_BLIP_TO_SERVER = 18;
	public static final int SEND_LOCATION_TO_SERVER = 19;
	public static final int GET_LOCATION_FOR_CURRENT_DETAILS_MESSAGE = 20;
	public static final int RINGER_OFF = 21;

	private NotificationManager mNotificationManager;
	private int commandType;
	private GoogleCloudMessaging gcm;

	@Override
	protected void onHandleIntent(Intent intent) {
		Utils.d("Received command...");
		Bundle extras = intent.getExtras();
		gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		String senderAddress = extras.getString(SENDER_ADDRESS);
		commandType = extras.getInt(ACTION);
		Utils.d("Command Type... " + commandType);

		switch (commandType) {
		case SIREN_ON:
			startSiren();
			break;
		case SIREN_OFF:
			stopRinger();
			break;
		case RINGER_ON:
			startRinger();
			break;
		case RINGER_OFF:
			stopRinger();
			break;
		case BACKUP:
			backup();
			break;
		case ENABLE_TRACKER:
			Intent tracker = new Intent(this, TrackerService.class);
			startService(tracker);
			break;
		case WIPE:
			Utils.completeWipe(this);
			break;
		case LOCK:
			break;
		case GET_LOCATION_FOR_PROTECT:
			Intent getProtect = new Intent(this, LocationService.class);
			getProtect.putExtra(ACTION, RequestType.PROTECT);
			startService(getProtect);
			break;
		case GET_LOCATION_FOR_MESSAGE:
			Intent getLocation = new Intent(this, LocationService.class);
			getLocation.putExtra(ACTION, RequestType.MESSAGE).putExtra(SENDER_ADDRESS, senderAddress);
			startService(getLocation);
			break;
		case GET_LOCATION_FOR_CURRENT_DETAILS_MESSAGE:
			Intent getNowLocation = new Intent(this, LocationService.class);
			getNowLocation.putExtra(ACTION, RequestType.NOW).putExtra(SENDER_ADDRESS, senderAddress);
			startService(getNowLocation);
			break;
		case GET_LOCATION_FOR_BLIP:
			Intent getBlip = new Intent(this, LocationService.class);
			getBlip.putExtra(ACTION, RequestType.BLIP);
			startService(getBlip);
			break;
		case SEND_BLIP_TO_SERVER:

			break;
		case SEND_LOCATION_TO_SERVER:
			break;
		case SEND_CALLS_DETAILS_MESSAGE:
			Utils.sendMessage(this, senderAddress, Utils.CALLS_M);
			break;
		case SEND_CURRENT_DETAILS_MESSAGE:
			Utils.sendMessage(this, senderAddress, Utils.NOW_M);
			break;
		case SEND_CURRENT_LOCATION_MESSAGE:
			Utils.sendMessage(this, senderAddress, Utils.LOCATION_M);
			break;
		case SEND_NEW_LOCATION_MESSAGE:
			Utils.sendMessage(this, senderAddress, Utils.NEW_LOCATION_M);
			break;
		case SEND_PROTECT_MESSAGE:
			Utils.sendMessage(this, Utils.getStringPrefs(this, Utils.EMERGENCY_CONTACT_NUMBER), Utils.PROTECT_ME_M);
		case SEND_SIM_CHANGE_MESSAGE:
			Utils.sendMessage(this, Utils.getStringPrefs(this, Utils.EMERGENCY_CONTACT_NUMBER), Utils.SIM_CHANGE_M);
			Utils.lock(this);
			break;
		case SEND_PIN_FAIL_MESSAGE:
			Utils.sendMessage(this, senderAddress, Utils.FAIL_M);
			break;
		default:
			break;
		}

		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				sendNotification("Deleted messages on server: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
			}
		}

		CloudMessagesReceiver.completeWakefulIntent(intent);
		MobileMessagesReceiver.completeWakefulIntent(intent);

	}

	private void startSiren() {
		Intent startSiren = new Intent(this, RingerService.class);
		startSiren.setAction(RingerService.SIREN_START_ACTION);
		startService(startSiren);
	}

	private void startRinger() {
		Intent startSiren = new Intent(this, RingerService.class);
		startSiren.setAction(RingerService.RINGER_START_ACTION);
		startService(startSiren);
	}

	private void stopRinger() {
		Intent stopSiren = new Intent(this, RingerService.class);
		stopSiren.setAction(RingerService.STOP_ACTION);
		stopService(stopSiren);
	}

	private void backup() {
		Intent startBackup = new Intent(this, BackupService.class);
		startBackup.setAction(BackupService.ACTION_BACKUP);
		startBackup.putExtra(BackupService.ACTION_DATA, BackupService.ACTION_DATA_ALL);
		startService(startBackup);
	}

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, HomeActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("GCM Notification").setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}
