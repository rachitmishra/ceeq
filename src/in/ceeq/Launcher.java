package in.ceeq;

/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

import in.ceeq.services.Commander;
import in.ceeq.services.Commander.Command;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Launcher extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transparent);
		// startActivity(new Intent(this, Home.class));

		startService(new Intent(this, Commander.class).putExtra(
				Commander.ACTION, Command.GET_LOCATION_FOR_MESSAGE));
		this.finish();
	}
}
