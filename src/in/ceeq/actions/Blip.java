/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.actions;

import android.content.Context;
import android.content.Intent;

import in.ceeq.services.Locater;
import in.ceeq.services.Locater.RequestType;

public class Blip {
	private Context context;
	public Blip(Context context) {
		this.context = context;
	}

	public static Blip getInstance(Context context) {
		return new Blip(context);
	}

	public void send() {
		Intent getLocation = new Intent(context, Locater.class);
		getLocation.putExtra(Locater.ACTION, RequestType.MESSAGE);
		context.startService(getLocation);
	}
}
