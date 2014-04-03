/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import in.ceeq.services.Commander;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LowBattery extends BroadcastReceiver {
	public LowBattery() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent commands = new Intent(context, Commander.class);
		commands.putExtra(Commander.ACTION, Commander.SEND_BLIP_TO_SERVER);
		context.startService(commands);

	}
}
