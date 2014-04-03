/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.actions;

import in.ceeq.services.Ringer;
import android.content.Context;
import android.content.Intent;

public class Ring {

	private static final String ACTION = "action";
	private static final String ACTION_TYPE = "actionType";
	private Context context;

	public Ring(Context context) {
		this.context = context;
	}

	public static Ring getInstance(Context context) {
		return new Ring(context);
	}

	public void stop() {
		Intent stopSiren = new Intent(context, Ringer.class).putExtra(
				ACTION_TYPE, Ringer.RING).putExtra(ACTION, Ringer.OFF);
		context.stopService(stopSiren);
	}

	public void start() {
		Intent startSiren = new Intent(context, Ringer.class).putExtra(
				ACTION_TYPE, Ringer.RING).putExtra(ACTION, Ringer.ON);
		context.startService(startSiren);
	}
}
