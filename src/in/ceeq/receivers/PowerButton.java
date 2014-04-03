/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import in.ceeq.helpers.Logger;
import in.ceeq.services.Commander;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerButton extends BroadcastReceiver {
	private int pressure;

	public PowerButton() {
		Logger.d("SOS signal received...");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		pressure++;
		if (pressure == 10) {
			Intent commands = new Intent(context, Commander.class);
			commands.putExtra(Commander.ACTION, Commander.SEND_PROTECT_MESSAGE);
			context.startService(commands);
		}

	}
}
