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

public class BatteryStateReceiver extends BroadcastReceiver {
	public BatteryStateReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent commands = new Intent(context, CommandService.class);
		commands.putExtra(CommandService.ACTION, CommandService.SEND_BLIP_TO_SERVER);
		context.startService(commands);

	}
}
