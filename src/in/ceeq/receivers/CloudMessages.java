/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import in.ceeq.services.Commander;

public class CloudMessages extends WakefulBroadcastReceiver {
	public CloudMessages() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		ComponentName component = new ComponentName(context.getPackageName(),
				Commander.class.getName());
		startWakefulService(context, (intent.setComponent(component)));
		setResultCode(Activity.RESULT_OK);
	}
}
