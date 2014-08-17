package in.ceeq.settings;

import in.ceeq.commons.Utils;
import in.ceeq.splash.SplashActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

public class ResetTask extends AsyncTask<Void, Void, Void> {
	private ProgressDialog progressDialog;
	private Context context;
	private GoogleApiClient googleApiClient;

	public static void run(Context context, GoogleApiClient googleApiClient) {
		new ResetTask(context, googleApiClient).execute();
	}

	public ResetTask(Context context, GoogleApiClient googleApiClient) {
		this.context = context;
		this.googleApiClient = googleApiClient;
	}

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
				Utils.clearPrefs(context);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void arg0) {
		progressDialog.dismiss();
		Utils.notification(Utils.NOTIFICATION_CANCEL_ALL, context, 0);
		Intent launchSplash = new Intent(context, SplashActivity.class);
		launchSplash.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		launchSplash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		launchSplash.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

		context.startActivity(launchSplash);
	}
}