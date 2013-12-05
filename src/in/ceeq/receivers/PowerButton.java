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

import in.ceeq.services.Commander;
import in.ceeq.services.Commander.Command;

public class PowerButton extends BroadcastReceiver {
	private int pressure;

	public PowerButton() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		pressure++;
		if (pressure == 10) {
			Intent commands = new Intent(context, Commander.class);
			commands.putExtra(Commander.ACTION, Command.SEND_PROTECT_MESSAGE);
			context.startService(commands);
		}

	}
}
