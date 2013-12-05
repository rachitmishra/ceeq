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
import android.widget.Toast;

public class LowBattery extends BroadcastReceiver {
	public LowBattery() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// context.startService(new Intent(Home.this,
		// ServiceUploader.class).putExtra("action",
		// Constants.UPLOAD_TYPE_BLIP));
		Toast.makeText(context, "Low Battery", Toast.LENGTH_LONG).show();

	}
}
