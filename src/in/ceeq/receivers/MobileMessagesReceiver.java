/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import in.ceeq.commons.Utils;
import in.ceeq.services.CommandService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsMessage;

public class MobileMessagesReceiver extends WakefulBroadcastReceiver {

	/**
	 * Sms commands receiver commands allowed Siren, Ring, Now, Calls,
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();

		Object messages[] = (Object[]) bundle.get("pdus");
		SmsMessage smsMessage[] = new SmsMessage[messages.length];
		for (int n = 0; n < messages.length; n++) {
			smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
		}

		String messageText = smsMessage[0].getMessageBody().toString()
				.toUpperCase();
		String senderAddress = smsMessage[0].getOriginatingAddress();
		Utils.setStringPrefs(context, Utils.SENDER_ADDRESS, senderAddress);
		Intent sendCommand = new Intent(context, CommandService.class);
		if (messageText.contains("CEEQ")
				&& messageText
						.contains(Utils.getStringPrefs(context, Utils.PIN_NUMBER))) {
			if (messageText.contains("ALARM")) {
				sendCommand.putExtra(CommandService.ACTION, CommandService.SIREN_ON);
			} else if (messageText.contains("RING")) {
				sendCommand.putExtra(CommandService.ACTION, CommandService.RINGER_ON);
			} else if (messageText.contains("ERASE")) {
				sendCommand.putExtra(CommandService.ACTION, CommandService.WIPE);
			} else if (messageText.contains("NOW")) {
				sendCommand.putExtra(CommandService.ACTION,
						CommandService.GET_LOCATION_FOR_CURRENT_DETAILS_MESSAGE);
			} else if (messageText.contains("CALLS")) {
				sendCommand.putExtra(CommandService.ACTION,
						CommandService.SEND_CALLS_DETAILS_MESSAGE);
			} else if (messageText.contains("SPY")) {
				sendCommand.putExtra(CommandService.ACTION, CommandService.ENABLE_TRACKER);
			} else if (messageText.contains("LOCATE")) {
				sendCommand.putExtra(CommandService.ACTION,
						CommandService.GET_LOCATION_FOR_MESSAGE);
			}
		} else
			sendCommand.putExtra(CommandService.ACTION,
					CommandService.SEND_PIN_FAIL_MESSAGE);

		startWakefulService(context, sendCommand);
		setResultCode(Activity.RESULT_OK);
	}
}
