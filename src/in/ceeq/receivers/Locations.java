package in.ceeq.receivers;

import in.ceeq.helpers.Logger;
import in.ceeq.services.Commander;
import in.ceeq.services.Locater.RequestType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class Locations extends BroadcastReceiver {

	private static final String ACTION = "action";
	private static final String SENDER_ADDRESS = "senderAddress";
	private RequestType requestType;

	public Locations() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Logger.d("New location broadcast received...");
		Bundle extras = intent.getExtras();
		requestType = (RequestType) extras.get(ACTION);
		Intent commands = new Intent(context, Commander.class);
		switch (requestType) {
		case BLIP:
			commands.putExtra(Commander.ACTION, Commander.SEND_BLIP_TO_SERVER);
			break;
		case MESSAGE:
			commands.putExtra(Commander.ACTION,
					Commander.SEND_CURRENT_LOCATION_MESSAGE);
			commands.putExtra(SENDER_ADDRESS,
					intent.getExtras().getString(SENDER_ADDRESS));
			break;
		case SERVER:
			commands.putExtra(Commander.ACTION, Commander.SEND_LOCATION_TO_SERVER);
			break;
		case PROTECT:
			commands.putExtra(Commander.ACTION, Commander.SEND_PROTECT_MESSAGE);
			commands.putExtra(SENDER_ADDRESS,
					intent.getExtras().getString(SENDER_ADDRESS));
			break;
		case NOW:
			commands.putExtra(Commander.ACTION,
					Commander.SEND_CURRENT_DETAILS_MESSAGE);
			commands.putExtra(SENDER_ADDRESS,
					intent.getExtras().getString(SENDER_ADDRESS));
			break;
		default:
			break;
		}
		context.startService(commands);
	}
}
