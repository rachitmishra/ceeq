package in.ceeq.actions;

import android.content.Context;

import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.services.Uploader;

public class Reset {
	private PreferencesHelper preferencesHelper;

	public Reset(Context context) {
		preferencesHelper = PreferencesHelper.getInstance(context);
	}

	public static Reset getInstance(Context context) {
		return new Reset(context);
	}

	public void defaults() {
		try {
			preferencesHelper.setString(PreferencesHelper.SIM_NUMBER, "");
			preferencesHelper.setString(PreferencesHelper.LAST_BACKUP_DATE, "");

			preferencesHelper.setBoolean(
					PreferencesHelper.APP_INITIALIZATION_STATUS, false);
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

}
