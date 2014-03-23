/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.activities;

import hirondelle.date4j.DateTime;
import in.ceeq.R;
import in.ceeq.helpers.PhoneHelper;
import in.ceeq.helpers.PhoneHelper.Phone;
import in.ceeq.helpers.PreferencesHelper;

import java.util.TimeZone;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class Splash extends Activity implements ConnectionCallbacks,
		OnConnectionFailedListener {

	private static final int NEXT_FIRSTRUN = 1;
	private static final int NEXT_HOME = 2;
	private static final int REQUEST_CODE_SIGN_IN = 9010;

	private ProgressBar progressBar;
	private GoogleApiClient googleApiClient;
	private SignInButton signInButton;
	private PhoneHelper phoneHelper;
	private boolean isSetupComplete, isGoogleConnected, mSignInClicked,
			mIntentInProgress;
	private ConnectionResult mConnectionResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		setupHelpers();
		checkPlayServices();
		checkConnectivity();
		checkGoogleConnect();
		setupGoogleConnect();
	}

	private PreferencesHelper preferencesHelper;

	private void setupHelpers() {
		preferencesHelper = PreferencesHelper.getInstance(this);
		phoneHelper = PhoneHelper.getInstance(this);
	}

	private void checkConnectivity() {
		if (!phoneHelper.enabled(Phone.INTERNET)) {
			Toast.makeText(this, R.string.toast_string_0, Toast.LENGTH_SHORT)
					.show();
		}

	}

	private void checkPlayServices() {
		if (!phoneHelper.enabled(Phone.PLAY_SERVICES)) {
			startActivity(new Intent(this, GoogleServices.class).putExtra(
					"from", 1));
			this.finish();
		}
	}

	private void checkGoogleConnect() {
		isGoogleConnected = preferencesHelper
				.getBoolean(PreferencesHelper.GOOGLE_CONNECT_STATUS);
		signInButton = (SignInButton) findViewById(R.id.sign_in_button);
		if (!isGoogleConnected) {
			signInButton.setVisibility(View.VISIBLE);
			signInButton.setSize(SignInButton.SIZE_WIDE);
			signInButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					mSignInClicked = true;
					connectGoogle();
				}
			});
		}

	}

	private void setupGoogleConnect() {
		progressBar = (ProgressBar) findViewById(R.id.connectProgress);
		googleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API, null)
				.addScope(Plus.SCOPE_PLUS_PROFILE).build();
	}

	private void connectGoogle() {
		if (mConnectionResult.hasResolution()) {
			try {
				mIntentInProgress = true;
				mConnectionResult.startResolutionForResult(this,
						REQUEST_CODE_SIGN_IN);
			} catch (SendIntentException e) {
				mIntentInProgress = false;
				googleApiClient.connect();
			}
		}
	}

	public void onConnectionFailed(ConnectionResult result) {
		if (!mIntentInProgress) {
			mConnectionResult = result;

			if (mSignInClicked) {
				connectGoogle();
			}
		}
	}

	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		if (requestCode == REQUEST_CODE_SIGN_IN) {
			if (responseCode != RESULT_OK) {
				mSignInClicked = false;
			}

			mIntentInProgress = false;

			if (!googleApiClient.isConnecting()) {
				googleApiClient.connect();
			}
		}
	}

	private static final int ONE_SECOND = 1;
	private static final int ZERO_SECONDS = 0;

	private void delayedStart(final int nextActivity, int secondsDelayed) {

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
				case NEXT_FIRSTRUN:
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

	@Override
	protected void onStart() {
		Log.w("Debug", "onStart()");
		super.onStart();
		isSetupComplete = preferencesHelper
				.getBoolean(PreferencesHelper.APP_INITIALIZATION_STATUS);
		if (!isGoogleConnected) {
			googleApiClient.connect();
		} else if (!isSetupComplete) {
			signInButton.setVisibility(View.INVISIBLE);
			delayedStart(NEXT_FIRSTRUN, ONE_SECOND);

		} else {
			signInButton.setVisibility(View.INVISIBLE);
			delayedStart(NEXT_HOME, ONE_SECOND);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (googleApiClient.isConnected()) {
			googleApiClient.disconnect();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {

		Person currentUser = Plus.PeopleApi.getCurrentPerson(googleApiClient);
		preferencesHelper.setString(PreferencesHelper.ACCOUNT_USER_ID,
				Plus.AccountApi.getAccountName(googleApiClient));
		preferencesHelper.setString(PreferencesHelper.ACCOUNT_USER_NAME,
				currentUser.getDisplayName());
		preferencesHelper.setString(PreferencesHelper.ACCOUNT_USER_IMAGE_URL,
				currentUser.getImage().getUrl());
		preferencesHelper.setString(
				PreferencesHelper.ACCOUNT_REGISTRATION_DATE,
				DateTime.today(TimeZone.getDefault()).toString());
		preferencesHelper.setBoolean(PreferencesHelper.GOOGLE_CONNECT_STATUS,
				true);

		if (!isSetupComplete) {
			signInButton.setVisibility(View.INVISIBLE);
			delayedStart(NEXT_FIRSTRUN, ZERO_SECONDS);
		}
	}

	public void onConnectionSuspended(int cause) {
		googleApiClient.connect();
	}
}
