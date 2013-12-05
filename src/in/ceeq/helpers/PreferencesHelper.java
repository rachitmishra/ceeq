package in.ceeq.helpers;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesHelper {
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	public static final String SIM_NUMBER = "simNumber";
	public static final String IEMI_NUMBER = "iemiNumber";
	public static final String PIN_NUMBER = "pinNumber";
	public static final String ACCOUNT_USER_ID = "userId";
	public static final String ACCOUNT_USER_NAME = "userName";
	public static final String ACCOUNT_USER_IMAGE_URL = "userImage";
	public static final String ACCOUNT_REGISTRATION_DATE = "userRegistrationDate";
	public static final String FIRST_LOGIN = "firstLogin";
	public static final String LAST_BACKUP_DATE = "lastBackupDate";
	public static final String DEVICE_ADMIN_STATUS = "deviceAdmin";
	public static final String DEVICE_STATUS = "device";
	public static final String APP_STATUS = "application";
	public static final String BACKUP_STATUS = "backup";
	public static final String SECURITY_STATUS = "security";
	public static final String REMOTE_STATUS = "remoteAccess";
	public static final String APP_INITIALIZATION_STATUS = "applicationHaInitialised";
	public static final String SYNC_STATUS = "dataSync";
	public static final String AUTO_TRACK_STATUS = "autoTrack";
	public static final String AUTO_BACKUP_STATUS = "autoBackup";
	public static final String AUTO_BLIP_STATUS = "autoBlip";
	public static final String PROTECT_ME_STATUS = "protectMe";
	public static final String STEALTH_MODE_STATUS = "stealthMode";
	public static final String ONLINE_ACCOUNT_STATUS = "onlineAccount";
	public static final String GCM_REGISTRATION_STATUS = "gcm";
	public static final String NOTIFICATIONS_STATUS = "notifications";
	public static final String SPLASH_STATUS = "splash";
	public static final String FACEBOOK_CONNECT_STATUS = "facebookConnected";
	public static final String GOOGLE_CONNECT_STATUS = "googleConnected";
	public static final String TWITTER_CONNECT_STATUS = "twitterConnected";
	public static final String GCM_REGISTRATION_ID = "gcmRegistrationId";
	public static final String APP_VERSION = "appVersion";
	public static final String FEEDBACK_MESSAGE = "feedbackMessage";
	public static final String EMERGENCY_CONTACT_NAME = "emergencyNumber";
	public static final String EMERGENCY_CONTACT_NUMBER = "emergencyName";
	public static final String EMERGENCY_MESSAGE = "emergencyMessage";
	public static final String DISTRESS_MESSAGE = "distressMessage";
	public static final String LAST_LOCATION_LATITUDE = "lastLatitude";
	public static final String LAST_LOCATION_LONGITUDE = "lastLongitude";

	public PreferencesHelper(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		editor = prefs.edit();
		editor.commit();
	}

	public static PreferencesHelper getInstance(Context context) {
		return new PreferencesHelper(context);
	}

	public Boolean getBoolean(String key) {
		return prefs.getBoolean(key, false);
	}

	public void setBoolean(String key, Boolean value) {
		editor.putBoolean(key, value);
		editor.commit();
	}

	public String getString(String key) {
		return prefs.getString(key, "");
	}

	public void setString(String key, String value) {
		editor.putString(key, value);
		editor.commit();
	}

	public int getInt(String key) {
		return prefs.getInt(key, 0);
	}

	public void setInt(String key, int value) {
		editor.putInt(key, value);
		editor.commit();
	}

}