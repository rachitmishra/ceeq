package in.ceeq.receivers;

import in.ceeq.services.CommandService;
import in.ceeq.services.LocationService.RequestType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class LocationsReceiver extends BroadcastReceiver {

	private static final String ACTION = "action";
	private static final String SENDER_ADDRESS = "senderAddress";
	private RequestType requestType;

	public LocationsReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		requestType = (RequestType) extras.get(ACTION);
		Intent commands = new Intent(context, CommandService.class);
		switch (requestType) {
		case BLIP:
			commands.putExtra(CommandService.ACTION, CommandService.SEND_BLIP_TO_SERVER);
			break;
		case MESSAGE:
			commands.putExtra(CommandService.ACTION,
					CommandService.SEND_CURRENT_LOCATION_MESSAGE);
			commands.putExtra(SENDER_ADDRESS,
					intent.getExtras().getString(SENDER_ADDRESS));
			break;
		case SERVER:
			commands.putExtra(CommandService.ACTION, CommandService.SEND_LOCATION_TO_SERVER);
			break;
		case PROTECT:
			commands.putExtra(CommandService.ACTION, CommandService.SEND_PROTECT_MESSAGE);
			commands.putExtra(SENDER_ADDRESS,
					intent.getExtras().getString(SENDER_ADDRESS));
			break;
		case NOW:
			commands.putExtra(CommandService.ACTION,
					CommandService.SEND_CURRENT_DETAILS_MESSAGE);
			commands.putExtra(SENDER_ADDRESS,
					intent.getExtras().getString(SENDER_ADDRESS));
			break;
		default:
			break;
		}
		context.startService(commands);
	}
}
