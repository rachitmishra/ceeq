/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.actions;

import android.content.Context;
import android.content.Intent;

import in.ceeq.services.Ringer;
import in.ceeq.services.Ringer.SirenState;

public class Siren {

	private static final String ACTION = "action";
	private Context context;
	public Siren(Context context) {
		this.context = context;
	}

	public static Siren getInstance(Context context) {
		return new Siren(context);
	}

	public void stop() {
		Intent stopSiren = new Intent(context, Ringer.class).putExtra(ACTION,
				SirenState.OFF);
		context.stopService(stopSiren);
	}

	public void start() {
		Intent startSiren = new Intent(context, Ringer.class).putExtra(ACTION,
				SirenState.ON);
		context.startService(startSiren);
	}
}
