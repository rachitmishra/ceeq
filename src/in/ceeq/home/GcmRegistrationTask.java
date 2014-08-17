package in.ceeq.home;

import in.ceeq.commons.Utils;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmRegistrationTask extends AsyncTask<Void, Void, Boolean> {

	private static final String SENDER_ID = "909602096750";
	private String registrationId;
	private GoogleCloudMessaging gcm;
	private Context context;

	public static void run(Context context) {
		new GcmRegistrationTask(context).execute();
	}

	public GcmRegistrationTask(Context context) {
		this.gcm = GoogleCloudMessaging.getInstance(context);
		this.context = context;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			if (gcm == null) {
				gcm = GoogleCloudMessaging.getInstance(context);
			}
			registrationId = gcm.register(SENDER_ID);
			Utils.setData(Utils.REGISTRATION_ID, registrationId, context);
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result)
			Utils.setBooleanPrefs(context, Utils.GCM_REGISTRATION_STATUS, true);
		else
			Utils.setBooleanPrefs(context, Utils.GCM_REGISTRATION_STATUS, false);
	}
}
