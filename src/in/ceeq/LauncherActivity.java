package in.ceeq;

/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

import in.ceeq.splash.SplashActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LauncherActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_transparent);
		Intent launchSplash;
		launchSplash = new Intent(this,SplashActivity.class);
		launchSplash.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		launchSplash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		launchSplash.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(launchSplash);

		overridePendingTransition(0, 0);
	}
}
