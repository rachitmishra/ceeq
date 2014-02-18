/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.activities;

import in.ceeq.R;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class GoogleServices extends Activity {

	private int caller;
	private static final int CONNECTION_FAILURE_REQUEST = 9013;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_play);
		setupBugsense();
		caller = getIntent().getIntExtra("from", 0);
		if (servicesConnected()) {
			returnFrom(caller);
		}
	}

	public void setupBugsense() {
		BugSenseHandler.initAndStartSession(GoogleServices.this, "5996b3d9");
	}

	public void returnFrom(int jump) {
		switch (jump) {
		case 0:
			startActivity(new Intent(this, Splash.class));
			this.finish();
			break;
		case 1:
			startActivity(new Intent(this, Home.class));
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
					startActivity(new Intent(this, Splash.class));
					break;
				case 1:
					startActivity(new Intent(this, Home.class));
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