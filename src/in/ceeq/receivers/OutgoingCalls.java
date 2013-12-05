/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import in.ceeq.activities.Splash;

public class OutgoingCalls extends BroadcastReceiver {
	public OutgoingCalls() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String phonenumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		if (phonenumber.equals("*#2337#*")) {
			context.startActivity(new Intent(context, Splash.class)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			setResultData(null);
		}
	}
}
