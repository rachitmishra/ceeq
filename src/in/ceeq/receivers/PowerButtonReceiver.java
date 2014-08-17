/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import in.ceeq.services.CommandService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerButtonReceiver extends BroadcastReceiver {
	private int pressure;

	public PowerButtonReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		pressure++;
		if (pressure == 10) {
			Intent commands = new Intent(context, CommandService.class);
			commands.putExtra(CommandService.ACTION, CommandService.SEND_PROTECT_MESSAGE);
			context.startService(commands);
		}

	}
}
