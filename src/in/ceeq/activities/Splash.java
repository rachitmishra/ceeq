/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.activities;

import hirondelle.date4j.DateTime;
import in.ceeq.R;
import in.ceeq.helpers.Helpers;
import in.ceeq.helpers.PreferencesHelper;

import java.util.TimeZone;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;

public class Splash extends Activity implements ConnectionCallbacks,
		OnConnectionFailedListener {

	private enum Next {
		FIRSTRUN, HOME
	}

	private boolean appHasInitialised, googleConnect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		checkConnectivity();
		checkPlayServices();
		setupGoogleConnect();
		setupHelpers();
		checkGoogleConnect();
	}

	private PreferencesHelper preferencesHelper;

	private void setupHelpers() {
		preferencesHelper = new PreferencesHelper(this);
	}

	private ProgressBar progreeBar;
	private PlusClient plus;
	private SignInButton button;

	private void setupGoogleConnect() {
		progreeBar = (ProgressBar) findViewById(R.id.connectProgress);
		plus = new PlusClient.Builder(this, this, this).setActions(
				"http://schemas.google.com/AddActivity").build();
	}

	private void checkConnectivity() {
		if (!Helpers.getInstance(this).hasInternet()) {
			Toast.makeText(this, R.string.toast_string_0, Toast.LENGTH_SHORT)
					.show();
		}

	}

	private void checkPlayServices() {
		if (!Helpers.getInstance(this).isGooglePlayConnected()) {
			startActivity(new Intent(this, GoogleServices.class).putExtra(
					"from", 1));
			this.finish();
		}
	}

	private void checkGoogleConnect() {
		googleConnect = preferencesHelper
				.getBoolean(PreferencesHelper.GOOGLE_CONNECT_STATUS);
		button = (SignInButton) findViewById(R.id.sign_in_button);
		if (!googleConnect) {
			button.setVisibility(View.VISIBLE);
			button.setSize(SignInButton.SIZE_WIDE);
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					connectGoogle();
				}
			});
		}

	}

	private void connectGoogle() {
		if (!plus.isConnected()) {
			if (connectionResult == null) {
				progreeBar.setVisibility(View.VISIBLE);
				plus.connect();

			} else {
				try {
					connectionResult.startResolutionForResult(this,
							CONNECTION_FAILURE_REQUEST);
				} catch (SendIntentException e) {
					connectionResult = null;
					plus.connect();
				}
			}
		}
	}

	private static final int ONE_SECOND = 1;
	private static final int ZERO_SECONDS = 0;

	private void delayedStart(final Next nextActivity, int secondsDelayed) {

		if (PreferencesHelper.getInstance(this).getBoolean(
				PreferencesHelper.SPLASH_STATUS))
			secondsDelayed = ONE_SECOND;
		else
			secondsDelayed = ZERO_SECONDS;

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent launchNextActivity;
				switch (nextActivity) {
				case FIRSTRUN:
					launchNextActivity = new Intent(Splash.this, Firstrun.class);
					break;
				default:
					launchNextActivity = new Intent(Splash.this, Home.class);
					break;

				}

				launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(launchNextActivity);
				overridePendingTransition(0, 0);
			}
		}, secondsDelayed * 1000);
	}

	private ConnectionResult connectionResult;
	private static final int CONNECTION_FAILURE_REQUEST = 9010;

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (progreeBar.isActivated()) {
			if (result.hasResolution()) {
				try {
					result.startResolutionForResult(this,
							CONNECTION_FAILURE_REQUEST);
				} catch (SendIntentException e) {
					plus.connect();
				}
			}
		}
		connectionResult = result;
	}

	@Override
	protected void onStart() {
		super.onStart();
		appHasInitialised = preferencesHelper
				.getBoolean(PreferencesHelper.APP_INITIALIZATION_STATUS);
		if (!googleConnect) {
			plus.connect();
		} else if (!appHasInitialised) {
			button.setVisibility(View.INVISIBLE);
			delayedStart(Next.FIRSTRUN, ONE_SECOND);

		} else {
			button.setVisibility(View.INVISIBLE);
			delayedStart(Next.HOME, ONE_SECOND);

		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		plus.disconnect();
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		if (requestCode == CONNECTION_FAILURE_REQUEST
				& responseCode == RESULT_OK) {
			connectionResult = null;
			plus.connect();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Person currentUser = plus.getCurrentPerson();
		preferencesHelper.setString(PreferencesHelper.ACCOUNT_USER_ID,
				plus.getAccountName());
		preferencesHelper.setString(PreferencesHelper.ACCOUNT_USER_NAME,
				currentUser.getDisplayName());
		preferencesHelper.setString(PreferencesHelper.ACCOUNT_USER_IMAGE_URL,
				currentUser.getImage().getUrl());
		preferencesHelper.setString(
				PreferencesHelper.ACCOUNT_REGISTRATION_DATE,
				DateTime.today(TimeZone.getDefault()).toString());
		preferencesHelper.setBoolean(PreferencesHelper.GOOGLE_CONNECT_STATUS,
				true);

		if (!appHasInitialised) {
			button.setVisibility(View.INVISIBLE);
			delayedStart(Next.FIRSTRUN, ZERO_SECONDS);
		}
	}

	@Override
	public void onDisconnected() {
	}
}
