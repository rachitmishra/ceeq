/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.services;

import in.ceeq.R;
import in.ceeq.actions.Backup;
import in.ceeq.actions.Lock;
import in.ceeq.actions.Ring;
import in.ceeq.actions.Siren;
import in.ceeq.actions.Wipe;
import in.ceeq.activities.Home;
import in.ceeq.helpers.Logger;
import in.ceeq.helpers.MessagesHelper;
import in.ceeq.helpers.MessagesHelper.MessageType;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.receivers.CloudMessages;
import in.ceeq.receivers.MobileMessages;
import in.ceeq.services.Locater.RequestType;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class Commander extends IntentService {

	public Commander() {
		super("ServiceCommander");
		Logger.d("Service commander started...");
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
		Logger.d("Received command...");
		Bundle extras = intent.getExtras();
		gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		String senderAddress = extras.getString(SENDER_ADDRESS);
		commandType = extras.getInt(ACTION);
		Logger.d("Command Type... " + commandType);

		switch (commandType) {
		case SIREN_ON:
			Siren.getInstance(this).start();
			break;
		case SIREN_OFF:
			Siren.getInstance(this).stop();
			break;
		case RINGER_ON:
			Ring.getInstance(this).start();
			break;
		case RINGER_OFF:
			Ring.getInstance(this).stop();
			break;
		case BACKUP:
			Backup.getInstance(this).backup(Backups.ACTION_TYPE_ALL);
			break;
		case ENABLE_TRACKER:
			Intent tracker = new Intent(this, Tracker.class);
			startService(tracker);
			break;
		case WIPE:
			Wipe.getInstance(this).device();
			break;
		case LOCK:
			break;
		case GET_LOCATION_FOR_PROTECT:
			Intent getProtect = new Intent(this, Locater.class);
			getProtect.putExtra(ACTION, RequestType.PROTECT);
			startService(getProtect);
			break;
		case GET_LOCATION_FOR_MESSAGE:
			Intent getLocation = new Intent(this, Locater.class);
			getLocation.putExtra(ACTION, RequestType.MESSAGE).putExtra(
					SENDER_ADDRESS, senderAddress);
			startService(getLocation);
			break;
		case GET_LOCATION_FOR_CURRENT_DETAILS_MESSAGE:
			Intent getNowLocation = new Intent(this, Locater.class);
			getNowLocation.putExtra(ACTION, RequestType.NOW).putExtra(
					SENDER_ADDRESS, senderAddress);
			startService(getNowLocation);
			break;
		case GET_LOCATION_FOR_BLIP:
			Intent getBlip = new Intent(this, Locater.class);
			getBlip.putExtra(ACTION, RequestType.BLIP);
			startService(getBlip);
			break;
		case SEND_BLIP_TO_SERVER:
			Backup.getInstance(this).backup(Backups.ACTION_TYPE_ALL);
			break;
		case SEND_LOCATION_TO_SERVER:
			Backup.getInstance(this).backup(Backups.ACTION_TYPE_ALL);
			break;
		case SEND_CALLS_DETAILS_MESSAGE:
			MessagesHelper.getInstance(this).sendMessage(senderAddress,
					MessageType.CALLS);
			break;
		case SEND_CURRENT_DETAILS_MESSAGE:
			MessagesHelper.getInstance(this).sendMessage(senderAddress,
					MessageType.NOW);
			break;
		case SEND_CURRENT_LOCATION_MESSAGE:
			MessagesHelper.getInstance(this).sendMessage(senderAddress,
					MessageType.LOCATION);
			break;
		case SEND_NEW_LOCATION_MESSAGE:
			MessagesHelper.getInstance(this).sendMessage(senderAddress,
					MessageType.NEW_LOCATION);
			break;
		case SEND_PROTECT_MESSAGE:
			MessagesHelper.getInstance(this).sendMessage(
					PreferencesHelper.getInstance(this).getString(
							PreferencesHelper.EMERGENCY_CONTACT_NUMBER),
					MessageType.PROTECT_ME);
		case SEND_SIM_CHANGE_MESSAGE:
			MessagesHelper.getInstance(this).sendMessage(
					PreferencesHelper.getInstance(this).getString(
							PreferencesHelper.EMERGENCY_CONTACT_NUMBER),
					MessageType.SIM_CHANGE);
			Lock.getInstance(this).lock();
			break;
		case SEND_PIN_FAIL_MESSAGE:
			MessagesHelper.getInstance(this).sendMessage(senderAddress,
					MessageType.FAIL);
			break;
		default:
			break;
		}

		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Deleted messages on server: "
						+ extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
			}
		}

		CloudMessages.completeWakefulIntent(intent);
		MobileMessages.completeWakefulIntent(intent);

	}

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, Home.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("GCM Notification")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}
