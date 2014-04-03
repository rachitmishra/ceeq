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

public class Siren {
	
	private Context context;

	public Siren(Context context) {
		this.context = context;
	}

	public static Siren getInstance(Context context) {
		return new Siren(context);
	}

	public void stop() {
		Intent stopSiren = new Intent(context, Ringer.class).putExtra(
				Ringer.ACTION_TYPE, Ringer.SIREN).putExtra(Ringer.ACTION, Ringer.OFF);
		context.stopService(stopSiren);
	}

	public void start() {
		Intent startSiren = new Intent(context, Ringer.class).putExtra(
				Ringer.ACTION_TYPE, Ringer.SIREN).putExtra(Ringer.ACTION, Ringer.ON);
		context.startService(startSiren);
	}
}
