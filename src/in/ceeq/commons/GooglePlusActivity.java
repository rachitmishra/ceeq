/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.commons;

import in.ceeq.R;
import in.ceeq.home.HomeActivity;
import in.ceeq.splash.SplashActivity;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class GooglePlusActivity extends Activity {

	private int caller;
	private static final int CONNECTION_FAILURE_REQUEST = 9013;
	public static final String FROM = "from";
	public static final int SPLASH = 0;
	public static final int HOME = 1;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_play);
		caller = getIntent().getIntExtra(FROM, 0);
		if (servicesConnected()) {
			returnFrom(caller);
		}
	}

	public void returnFrom(int caller) {
		switch (caller) {
		case SPLASH:
			startActivity(new Intent(this, SplashActivity.class));
			this.finish();
			break;
		case HOME:
			startActivity(new Intent(this, HomeActivity.class));
			this.finish();
			break;
		}
	}

	public static class ErrorDialogFragment extends DialogFragment {
		private Dialog errorDialog;

		public ErrorDialogFragment() {
			super();
			errorDialog = null;
		}

		public void setDialog(Dialog dialog) {
			errorDialog = dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return errorDialog;
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			this.getActivity().finish();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CONNECTION_FAILURE_REQUEST:
			switch (resultCode) {
			case Activity.RESULT_OK:
				switch (caller) {
				case 0:
					startActivity(new Intent(this, SplashActivity.class));
					break;
				case 1:
					startActivity(new Intent(this, HomeActivity.class));
					break;
				}
				break;
			case CONNECTION_FAILURE_REQUEST:
				switch (resultCode) {
				case Activity.RESULT_OK:
				}
				break;
			}
		}
	}

	public boolean servicesConnected() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		if (ConnectionResult.SUCCESS == resultCode) {
			return true;
		} else {

			ConnectionResult cr = new ConnectionResult(resultCode, null);
			int errorCode = cr.getErrorCode();

			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					errorCode, this, CONNECTION_FAILURE_REQUEST);

			if (errorDialog != null) {
				ErrorDialogFragment ef = new ErrorDialogFragment();
				ef.setDialog(errorDialog);
				ef.show(getFragmentManager(), "LocationUpdates");
			}
			return false;
		}
	}
}