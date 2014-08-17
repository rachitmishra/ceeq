/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import in.ceeq.services.CommandService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class CloudMessagesReceiver extends WakefulBroadcastReceiver {
	public CloudMessagesReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		ComponentName component = new ComponentName(context.getPackageName(),
				CommandService.class.getName());
		startWakefulService(context, (intent.setComponent(component)));
		setResultCode(Activity.RESULT_OK);
	}
}
