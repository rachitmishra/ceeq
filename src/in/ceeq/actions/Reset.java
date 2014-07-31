package in.ceeq.actions;

import in.ceeq.activities.Splash;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.services.Uploader;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

public class Reset {
	private PreferencesHelper preferencesHelper;
	private Context context;
	private GoogleApiClient googleApiClient;

	public Reset(Context context, GoogleApiClient googleApiClient) {
		this.context = context;
		this.googleApiClient = googleApiClient;
		preferencesHelper = PreferencesHelper.getInstance(context);
	}

	public Reset(Context context) {
		this.context = context;
		preferencesHelper = PreferencesHelper.getInstance(context);
	}

	public static Reset getInstance(Context context, GoogleApiClient googleApiClient) {
		return new Reset(context, googleApiClient);
	}

	public static Reset getInstance(Context context) {
		return new Reset(context);
	}

	public void defaults() {
		try {
			preferencesHelper.setString(PreferencesHelper.SIM_NUMBER, "");
			preferencesHelper.setString(PreferencesHelper.LAST_BACKUP_DATE, "");
			preferencesHelper
					.setBoolean(PreferencesHelper.DEVICE_STATUS, false);
			preferencesHelper.setBoolean(PreferencesHelper.APP_STATUS, false);
			preferencesHelper
					.setBoolean(PreferencesHelper.BACKUP_STATUS, false);
			preferencesHelper.setBoolean(PreferencesHelper.SECURITY_STATUS,
					false);
			preferencesHelper.setBoolean(PreferencesHelper.DEVICE_ADMIN_STATUS,
					false);
			preferencesHelper.setBoolean(PreferencesHelper.SYNC_STATUS, false);
			preferencesHelper.setBoolean(PreferencesHelper.AUTO_TRACK_STATUS,
					false);
			preferencesHelper.setBoolean(PreferencesHelper.AUTO_BLIP_STATUS,
					false);
			preferencesHelper.setBoolean(PreferencesHelper.PROTECT_ME_STATUS,
					false);
			preferencesHelper.setBoolean(PreferencesHelper.STEALTH_MODE_STATUS,
					false);
			preferencesHelper.setBoolean(
					PreferencesHelper.ONLINE_ACCOUNT_STATUS, false);
			preferencesHelper.setBoolean(
					PreferencesHelper.GCM_REGISTRATION_STATUS, false);

			preferencesHelper.setBoolean(
					PreferencesHelper.NOTIFICATIONS_STATUS, true);
			preferencesHelper.setBoolean(PreferencesHelper.SPLASH_STATUS, true);

			preferencesHelper.setBoolean(Uploader.UPLOAD_STATUS_ACCOUNT, false);
			preferencesHelper.setBoolean(Uploader.UPLOAD_STATUS_DATA, false);
			preferencesHelper
					.setBoolean(Uploader.UPLOAD_STATUS_FEEDBACK, false);
			preferencesHelper.setBoolean(Uploader.UPLOAD_STATUS_BLIP, false);
			preferencesHelper.setBoolean(PreferencesHelper.FIRST_LOGIN, true);
		}

		catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	public void reset() {
		new AsyncTask<Void, Void, Void>() {
			private ProgressDialog progressDialog;

			@Override
			protected void onPreExecute() {
				progressDialog = new ProgressDialog(context);
				progressDialog.setMessage("Resetting Ceeq...");
				progressDialog.show();
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				try {
					if (googleApiClient.isConnected()) {
						Plus.AccountApi.clearDefaultAccount(googleApiClient);
						Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient);
						PreferencesHelper.getInstance(context).clear();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			}

			@Override
			protected void onPostExecute(Void arg0) {
				progressDialog.dismiss();
				Notifications.getInstance(context).remove();
				Intent launchSplash = new Intent(context,Splash.class);
				launchSplash.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				launchSplash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				launchSplash.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

				context.startActivity(launchSplash);
			}

		}.execute();
	}
}
