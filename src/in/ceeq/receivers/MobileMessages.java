/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import in.ceeq.helpers.Logger;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.services.Commander;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsMessage;

public class MobileMessages extends WakefulBroadcastReceiver {
	private PreferencesHelper preferencesHelper;

	/**
	 * Sms commands receiver commands allowed Siren, Ring, Now, Calls,
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		preferencesHelper = new PreferencesHelper(context);
		Bundle bundle = intent.getExtras();

		Object messages[] = (Object[]) bundle.get("pdus");
		SmsMessage smsMessage[] = new SmsMessage[messages.length];
		for (int n = 0; n < messages.length; n++) {
			smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
		}

		String messageText = smsMessage[0].getMessageBody().toString()
				.toUpperCase();
		String senderAddress = smsMessage[0].getOriginatingAddress();
		PreferencesHelper.getInstance(context).setString(PreferencesHelper.SENDER_ADDRESS, senderAddress);
		Intent sendCommand = new Intent(context, Commander.class);
		if (messageText.contains("CEEQ")
				& messageText
						.contains(preferencesHelper.getString("pinNumber"))) {
			if (messageText.contains("ALARM")) {
				sendCommand.putExtra(Commander.ACTION, Commander.SIREN_ON);
			} else if (messageText.contains("RING")) {
				sendCommand.putExtra(Commander.ACTION, Commander.RINGER_ON);
			} else if (messageText.contains("ERASE")) {
				sendCommand.putExtra(Commander.ACTION, Commander.WIPE);
			} else if (messageText.contains("NOW")) {
				sendCommand.putExtra(Commander.ACTION,
						Commander.GET_LOCATION_FOR_CURRENT_DETAILS_MESSAGE);
			} else if (messageText.contains("CALLS")) {
				sendCommand.putExtra(Commander.ACTION,
						Commander.SEND_CALLS_DETAILS_MESSAGE);
			} else if (messageText.contains("SPY")) {
				sendCommand.putExtra(Commander.ACTION, Commander.ENABLE_TRACKER);
			} else if (messageText.contains("LOCATE")) {
				sendCommand.putExtra(Commander.ACTION,
						Commander.GET_LOCATION_FOR_MESSAGE);
			}
		} else
			sendCommand.putExtra(Commander.ACTION,
					Commander.SEND_PIN_FAIL_MESSAGE);
		Logger.d("Starting commander service...");
		startWakefulService(context, sendCommand);
		setResultCode(Activity.RESULT_OK);
	}
}
