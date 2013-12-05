package in.ceeq.actions;

import android.content.Context;
import android.content.Intent;

import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.services.Uploader;

public class Feedback {
	private Context context;
	private PreferencesHelper preferencesHelper;
	public Feedback(Context context) {
		this.context = context;
		preferencesHelper = PreferencesHelper.getInstance(context);
	}

	public static Feedback getInstance(Context context) {
		return new Feedback(context);
	}

	public void send(String message) {
		preferencesHelper
				.setString(PreferencesHelper.FEEDBACK_MESSAGE, message);
		preferencesHelper.setBoolean(Uploader.UPLOAD_STATUS_FEEDBACK, true);
		Intent uploadFeedback = new Intent(context, Uploader.class);
		context.startService(uploadFeedback);
	}
}
