/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.splash;

import hirondelle.date4j.DateTime;
import in.ceeq.R;
import in.ceeq.commons.GooglePlusActivity;
import in.ceeq.commons.Utils;
import in.ceeq.home.HomeActivity;

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

import com.github.johnpersano.supertoasts.SuperToast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class SplashActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {

	private static final int NEXT_SETUP = 1;
	private static final int NEXT_HOME = 2;
	private static final int REQUEST_CODE_SIGN_IN = 9010;
	private static final int ONE_SECOND = 1;
	private static final int ZERO_SECONDS = 0;

	private ProgressBar progressBar;
	private GoogleApiClient googleApiClient;
	private SignInButton signInButton;
	private boolean isSetupComplete, isGoogleConnected, mSignInClicked, mIntentInProgress;
	private ConnectionResult mConnectionResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		setupUi();
		setupGoogleConnect();
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();
		checkConnectivity();
		checkGoogleConnect();
	}

	private void setupUi() {
		signInButton = (SignInButton) findViewById(R.id.sign_in_button);
	}

	private void checkConnectivity() {
		if (!Utils.enabled(Utils.INTERNET, this)) {
			Toast.makeText(this, R.string.toast_string_0, Toast.LENGTH_SHORT).show();
		}

	}

	private void checkPlayServices() {
		if (!Utils.enabled(Utils.PLAY_SERVICES, this)) {
			startActivity(new Intent(this, GooglePlusActivity.class).putExtra(GooglePlusActivity.FROM,
					GooglePlusActivity.SPLASH));
			this.finish();
		}
	}

	private void checkGoogleConnect() {
		if (!isGoogleConnected) {
			signInButton.setVisibility(View.VISIBLE);
			signInButton.setSize(SignInButton.SIZE_WIDE);
			signInButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					mSignInClicked = true;
					connectGooglePlus();
				}
			});
		}

	}

	private void setupGoogleConnect() {
		progressBar = (ProgressBar) findViewById(R.id.connectProgress);
		googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API).addScope(Plus.SCOPE_PLUS_PROFILE).build();
	}

	private void connectGooglePlus() {
		if (mConnectionResult.hasResolution()) {
			try {
				progressBar.setVisibility(View.VISIBLE);
				mIntentInProgress = true;
				mConnectionResult.startResolutionForResult(this, REQUEST_CODE_SIGN_IN);
			} catch (SendIntentException e) {
				mIntentInProgress = false;
				googleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		mSignInClicked = false;
		progressBar.setVisibility(View.GONE);
		try {
			Person currentUser = Plus.PeopleApi.getCurrentPerson(googleApiClient);

			if (currentUser != null) {
				Utils.setStringPrefs(this, Utils.ACCOUNT_USER_ID, Plus.AccountApi.getAccountName(googleApiClient));
				Utils.setStringPrefs(this, Utils.ACCOUNT_USER_NAME, currentUser.getDisplayName());
				Utils.setStringPrefs(this, Utils.ACCOUNT_USER_IMAGE_URL, currentUser.getImage().getUrl()
						.replace("50", "150"));
				Utils.setStringPrefs(this, Utils.ACCOUNT_REGISTRATION_DATE, DateTime.today(TimeZone.getDefault()).toString());
				Utils.setBooleanPrefs(this, Utils.GOOGLE_CONNECT_STATUS, true);
			} else {
				Toast.makeText(this, "User is null", Toast.LENGTH_LONG).show();
			}

			if (!isSetupComplete) {
				Utils.d("Setup yet not completed");
				signInButton.setVisibility(View.INVISIBLE);
				next(NEXT_SETUP, ZERO_SECONDS);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Utils.d("Exception in onConnected()");
			SuperToast.create(this, "An error occured.", Toast.LENGTH_SHORT).show();
		}
	}

	public void onConnectionSuspended(int cause) {
		googleApiClient.connect();
	}

	public void onConnectionFailed(ConnectionResult result) {
		if (!mIntentInProgress) {
			mConnectionResult = result;
			if (mSignInClicked) {
				connectGooglePlus();
			}
		}
	}

	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
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

	private void next(final int nextActivity, int secondsDelayed) {

		if (Utils.getBooleanPrefs(this, Utils.SPLASH_STATUS))
			secondsDelayed = ONE_SECOND;
		else
			secondsDelayed = ZERO_SECONDS;

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent launchNextActivity;
				switch (nextActivity) {
				case NEXT_SETUP:
					launchNextActivity = new Intent(SplashActivity.this, SetupActivity.class);
					break;
				default:
					launchNextActivity = new Intent(SplashActivity.this, HomeActivity.class);
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
		super.onStart();
		isSetupComplete = Utils.getBooleanPrefs(this, Utils.APP_INITIALIZATION_STATUS);
		isGoogleConnected = Utils.getBooleanPrefs(this, Utils.GOOGLE_CONNECT_STATUS);

		if (!isGoogleConnected) {
			googleApiClient.connect();
		} else if (!isSetupComplete) {
			signInButton.setVisibility(View.INVISIBLE);
			next(NEXT_SETUP, ONE_SECOND);
		} else {
			signInButton.setVisibility(View.INVISIBLE);
			next(NEXT_HOME, ONE_SECOND);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (googleApiClient.isConnected()) {
			googleApiClient.disconnect();
		}
	}
}
