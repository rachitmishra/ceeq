package in.ceeq.commons;

import hirondelle.date4j.DateTime;
import in.ceeq.R;
import in.ceeq.exceptions.ExternalStorageNotFoundException;
import in.ceeq.home.HomeActivity;
import in.ceeq.receivers.DeviceAdministrationReceiver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class Utils {

	/**************************************
	 **************** General ***************
	 **************************************/

	public static final int CONTACT_REQUEST = 9012;

	/**
	 * 
	 * Choose a contact
	 * 
	 * @param activity
	 */
	public static void chooseContact(Activity activity) {
		Intent chooseContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		chooseContact.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		activity.startActivityForResult(chooseContact, CONTACT_REQUEST);
	}

	public static String getTopActivityInStack(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

		Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());

		ComponentName componentInfo = taskInfo.get(0).topActivity;
		return componentInfo.getPackageName();
	}

	/**
	 * 
	 * Set application default
	 * 
	 * @param ctx
	 */
	public static void defaults(Context ctx) {
		try {
			setStringPrefs(ctx, SIM_NUMBER, "");
			setStringPrefs(ctx, LAST_BACKUP_DATE, "");
			setBooleanPrefs(ctx, DEVICE_STATUS, false);
			setBooleanPrefs(ctx, APP_STATUS, false);
			setBooleanPrefs(ctx, BACKUP_STATUS, false);
			setBooleanPrefs(ctx, SECURITY_STATUS, false);
			setBooleanPrefs(ctx, DEVICE_ADMIN_STATUS, false);
			setBooleanPrefs(ctx, SYNC_STATUS, false);
			setBooleanPrefs(ctx, AUTO_TRACK_STATUS, false);
			setBooleanPrefs(ctx, AUTO_BLIP_STATUS, false);
			setBooleanPrefs(ctx, PROTECT_ME_STATUS, false);
			setBooleanPrefs(ctx, STEALTH_MODE_STATUS, false);
			setBooleanPrefs(ctx, ONLINE_ACCOUNT_STATUS, false);
			setBooleanPrefs(ctx, GCM_REGISTRATION_STATUS, false);

			setBooleanPrefs(ctx, NOTIFICATIONS_STATUS, true);
			setBooleanPrefs(ctx, SPLASH_STATUS, true);

			setBooleanPrefs(ctx, FIRST_LOGIN, true);
		}

		catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	/**************************************
	 ************** Application **************
	 **************************************/

	public static final int APP_VERSION_CODE = 1;
	public static final int APP_PACKAGE_NAME = 2;

	public static String getApplicationData(Context context, int dataType) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

			switch (dataType) {
			case APP_VERSION_CODE:
				return packageInfo.versionCode + "";
			case APP_PACKAGE_NAME:
				return packageInfo.packageName;
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}

		return "";
	}

	/**************************************
	 ***************** Phone ***************
	 **************************************/

	public static final int SIM_ID = 0;
	public static final int NUMBER = 1;
	public static final int IEMI = 3;
	public static final int IMSI = 4;
	public static final int OPERATOR = 5;
	public static final int MANUFACTURER = 6;
	public static final int MODEL = 7;
	public static final int ANDROID_VERSION = 8;
	public static final int SIZE = 9;
	public static final int DENSITY = 10;
	public static final int UNIQUE_ID = 11;
	public static final int APP_COUNT = 12;
	public static final int GPS = 13;
	public static final int INTERNET = 14;
	public static final int PLAY_SERVICES = 15;
	public static final int EXTERNAL_STORAGE = 16;
	public static final int BATTERY_LEVEL = 17;
	public static final int REGISTRATION_ID = 18;

	/**
	 * 
	 * Set application data
	 * 
	 * @param context
	 * @param dataType
	 * 
	 * @return
	 */
	public static String getPhoneData(Context context, int dataType) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String data = "Not Available.";

		switch (dataType) {
		case SIM_ID:
			data = telephonyManager.getSimSerialNumber();
			break;
		case NUMBER:
			if (telephonyManager.getLine1Number() != null)
				data = telephonyManager.getLine1Number();
			break;
		case IEMI:
			data = telephonyManager.getDeviceId();
			break;
		case IMSI:
			data = telephonyManager.getSubscriberId();
			break;
		case OPERATOR:
			data = telephonyManager.getSimOperatorName();
			break;
		case MANUFACTURER:
			data = android.os.Build.MANUFACTURER;
			break;
		case MODEL:
			data = android.os.Build.MODEL;
			break;
		case ANDROID_VERSION:
			data = android.os.Build.VERSION.RELEASE;
			break;
		case SIZE:
			data = getSize(context);
			break;
		case DENSITY:
			data = getDensity(context);
			break;
		case UNIQUE_ID:
			data = getUniqueDeviceId(context);
			break;
		case APP_COUNT:
			data = getNumberOfApplications(context) + "";
			break;
		case BATTERY_LEVEL:
			data = (getCurrentBatteryLevel(context) * 100) + "%";
			break;
		case REGISTRATION_ID:
			data = getRegistrationId(context);
			break;
		default:
			data = "Not Available.";
			break;
		}

		return data;
	}

	/**
	 * 
	 * Set phone data
	 * 
	 * @param dataType
	 * @param data
	 * @param context
	 */
	public static void setData(int dataType, String data, Context context) {

		switch (dataType) {
		case REGISTRATION_ID:
			setRegistrationId(data, context);
			break;
		case APP_VERSION_CODE:
			break;
		default:
			break;

		}
	}

	/**
	 * 
	 * Check phone feature enabled
	 * 
	 * @param featureType
	 * @param context
	 * @return
	 */
	public static boolean enabled(int featureType, Context context) {
		boolean data = false;
		switch (featureType) {
		case GPS:
			data = isGpsEnabled(context);
			break;
		case INTERNET:
			data = isInternetEnabled(context);
			break;
		case PLAY_SERVICES:
			data = isPlayServiceInstalled(context);
			break;
		case EXTERNAL_STORAGE:
			data = isExternalStorageEnabled(context);
			break;
		default:
			data = false;
			break;

		}
		return data;
	}

	/**
	 * 
	 * Is GPS enabled
	 * 
	 * @param context
	 * 
	 * @return status
	 */
	private static boolean isGpsEnabled(Context context) {
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			return true;
		return false;
	}

	private static boolean isPlayServiceInstalled(Context context) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if (ConnectionResult.SUCCESS == resultCode)
			return true;
		return false;
	}

	private static boolean isExternalStorageEnabled(Context context) {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return mExternalStorageAvailable && mExternalStorageWriteable;
	}

	private static boolean isInternetEnabled(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	private static String getUniqueDeviceId(Context context) {
		return android.os.Build.MANUFACTURER.substring(0, 3).toUpperCase(Locale.getDefault()) + "-"
				+ randomString().substring(0, 6) + getPhoneData(context, IEMI);
	}

	private static String randomString() {
		return Long.toHexString(Double.doubleToLongBits(Math.random())).toUpperCase();
	}

	private static String getSize(Context context) {
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;

		return width + "pixels x " + height + "pixels";
	}

	private static String getDensity(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		int densityDpi = dm.densityDpi;
		return densityDpi + " PPI";
	}

	private static String getRegistrationId(Context ctx) {
		String registrationId = getStringPrefs(ctx, GCM_REGISTRATION_ID);
		if (registrationId.isEmpty())
			return "";
		int registeredVersion = getIntPrefs(ctx, APP_VERSION);
		int currentVersion = Integer.parseInt(getApplicationData(ctx, APP_VERSION_CODE));
		if (registeredVersion != currentVersion)
			return "";
		return registrationId;
	}

	private static void setRegistrationId(String regId, Context ctx) {
		setStringPrefs(ctx, GCM_REGISTRATION_ID, regId);
		setIntPrefs(ctx, APP_VERSION, Integer.parseInt(getApplicationData(ctx, APP_VERSION_CODE)));
	}

	private static int getNumberOfApplications(Context context) {
		PackageManager pm = context.getPackageManager();
		return pm.getInstalledApplications(PackageManager.GET_META_DATA).size();
	}

	private static float getCurrentBatteryLevel(Context context) {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		return (level / (float) scale);
	}

	/**************************************
	 ********** Device Administration **********
	 **************************************/

	private static DevicePolicyManager devicePolicyManager;
	private static ComponentName deviceAdminComponentName;
	public static final int DEFAULT_NOTIFICATION_ID = 9007;

	public static void lock(Context ctx) {
		devicePolicyManager = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
		deviceAdminComponentName = new ComponentName(ctx, DeviceAdministrationReceiver.class);
		if (getInitialLockState(ctx)) {
			lockNow();
		} else {
			setPasswordThenLock(ctx);
		}
	}

	public static void setPasswordThenLock(Context ctx) {
		devicePolicyManager.setPasswordQuality(deviceAdminComponentName, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
		devicePolicyManager.setPasswordMinimumNumeric(deviceAdminComponentName, 6);
		devicePolicyManager.resetPassword(getStringPrefs(ctx, PIN_NUMBER),
				DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
		devicePolicyManager.lockNow();
	}

	public static void lockNow() {
		devicePolicyManager.lockNow();
	}

	public static void removeLock(Context ctx) {
		devicePolicyManager = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
		deviceAdminComponentName = new ComponentName(ctx, DeviceAdministrationReceiver.class);
		devicePolicyManager.setPasswordQuality(deviceAdminComponentName,
				DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
		devicePolicyManager.resetPassword("", 0);
	}

	public static void setInitialLockState(Context ctx) {
		setBooleanPrefs(ctx, DEVICE_HAS_PASSWORD, hasPassword(ctx));
	}

	public static boolean getInitialLockState(Context ctx) {
		return getBooleanPrefs(ctx, DEVICE_HAS_PASSWORD);
	}

	public static boolean hasPassword(Context ctx) {
		devicePolicyManager = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
		deviceAdminComponentName = new ComponentName(ctx, DeviceAdministrationReceiver.class);
		int currentPasswordQuality = devicePolicyManager.getPasswordQuality(null);
		devicePolicyManager
				.setPasswordQuality(deviceAdminComponentName, DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
		boolean hasPassword = devicePolicyManager.isActivePasswordSufficient();
		devicePolicyManager.setPasswordQuality(deviceAdminComponentName, currentPasswordQuality);
		d("The user has password set: " + hasPassword);
		return hasPassword;
	}

	/**************************************
	 ************** Notifications *************
	 **************************************/

	public static void showNotifications(Context ctx) {

		Intent openHome = new Intent(ctx, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		PendingIntent pi = PendingIntent.getActivity(ctx, 0, openHome, 0);
		String applicationStatus = getBooleanPrefs(ctx, APP_STATUS) ? "Protected" : "Vulnerable";
		NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(ctx).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Ceeq").setContentText(applicationStatus).setOngoing(true);
		nBuilder.setContentIntent(pi);
		((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE)).notify(DEFAULT_NOTIFICATION_ID,
				nBuilder.build());
	}

	public static void removeAllNotifications(Context ctx) {
		((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
	}

	public static final int NOTIFICATION_CANCEL = 0;
	public static final int NOTIFICATION_NOTIFY = 1;
	public static final int NOTIFICATION_CANCEL_ALL = 2;
	public static final int NOTIFICATION_RESTORE_START = 3;
	public static final int NOTIFICATION_BACKUP_START = 4;
	public static final int NOTIFICATION_RESTORE_FINISH = 5;
	public static final int NOTIFICATION_BACKUP_FINISH = 6;

	public static boolean notificationsEnabled(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(NOTIFICATIONS_STATUS, false);
	}

	public static void notification(int action, Context ctx, int notificationId) {
		NotificationManager notificationManager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx);
		notificationBuilder.setContentTitle("Ceeq").setOngoing(true);

		if (notificationsEnabled(ctx)) {
			switch (action) {
			case NOTIFICATION_CANCEL:
				notificationManager.cancel(notificationId);
				break;
			case NOTIFICATION_CANCEL_ALL:
				notificationManager.cancelAll();
				break;
			case NOTIFICATION_NOTIFY:
				Intent openHome = new Intent(ctx, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				PendingIntent pi = PendingIntent.getActivity(ctx, 0, openHome, 0);
				notificationBuilder.setContentIntent(pi);
				notificationManager.notify(DEFAULT_NOTIFICATION_ID, notificationBuilder.build());
				break;
			case NOTIFICATION_BACKUP_START:
				notificationBuilder.setProgress(0, 0, true);
				notificationBuilder.setContentText("Backup in progress ...");
				notificationManager.notify(DEFAULT_NOTIFICATION_ID, notificationBuilder.build());
				break;
			case NOTIFICATION_RESTORE_START:
				notificationBuilder.setProgress(0, 0, true);
				notificationBuilder.setContentText("Restore in progress ...");
				notificationManager.notify(DEFAULT_NOTIFICATION_ID, notificationBuilder.build());
				break;
			case NOTIFICATION_RESTORE_FINISH:
				notificationBuilder.setContentText("Restore complete.");
				notificationManager.notify(DEFAULT_NOTIFICATION_ID, notificationBuilder.build());
				break;
			case NOTIFICATION_BACKUP_FINISH:
				notificationBuilder.setContentText("Backup complete.");
				notificationManager.notify(DEFAULT_NOTIFICATION_ID, notificationBuilder.build());
				break;
			default:
				break;
			}
		}
	}

	/**************************************
	 **************** Backups ***************
	 **************************************/

	public static final String INTENT_ACTION_BACKUP = "in.ceeq.action.backup";
	public static final int ALARM_ACTIVATION_REQUEST = 2337;

	public static void scheduledBackup(Context ctx, boolean status) {
		setBooleanPrefs(ctx, AUTO_BACKUP_STATUS, status);
		PendingIntent pi;
		if (status) {
			Toast.makeText(ctx, "Automatic backups started, everyday at 2:00 AM", Toast.LENGTH_SHORT).show();
			d("Turning alarm ON");
			pi = PendingIntent.getBroadcast(ctx, ALARM_ACTIVATION_REQUEST, new Intent(INTENT_ACTION_BACKUP),
					PendingIntent.FLAG_CANCEL_CURRENT);
			((AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE)).setInexactRepeating(AlarmManager.RTC_WAKEUP,
					new DateTime(DateTime.today(TimeZone.getDefault()) + " 02:00:00").getMilliseconds(TimeZone
							.getDefault()), AlarmManager.INTERVAL_DAY, pi);
		} else {
			Toast.makeText(ctx, "Automatic backups cancelled.", Toast.LENGTH_SHORT).show();
			pi = PendingIntent.getBroadcast(ctx, ALARM_ACTIVATION_REQUEST, new Intent(INTENT_ACTION_BACKUP),
					PendingIntent.FLAG_CANCEL_CURRENT);
			((AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE)).cancel(pi);
		}
	}

	public static void completeWipe(Context ctx) {
		((DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE))
				.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
	}

	/**************************************
	 ***************** Logs *****************
	 **************************************/

	public static String STARTED = "Starting... ";
	public static String COMPLETED = "Completed... ";
	public static String FAILED = "Failed... ";
	public static String ELLIPSIZE = "... ";

	/**
	 * Log debug message.
	 * 
	 * @param message
	 */
	public static void d(String message) {
		Log.d("@ceeq", message);
	}

	/**
	 * Log warning message.
	 * 
	 * @param message
	 */
	public static void w(String message) {
		Log.w("@ceeq", message);
	}

	/**
	 * Log informative message.
	 * 
	 * @param message
	 */
	public static void i(String message) {
		Log.i("@ceeq", message);
	}

	/**
	 * Log action started message.
	 * 
	 * @param action
	 *            backup or restore
	 * @param actionType
	 *            contacts or call logs or messages or dictionary.
	 */
	public static void s(String message) {
		Log.i("@ceeq", STARTED + message + ELLIPSIZE);
	}

	/**
	 * Log action completed message.
	 * 
	 * @param action
	 *            backup or restore
	 * @param actionType
	 *            contacts or call logs or messages or dictionary.
	 */
	public static void c(String message) {
		Log.i("@ceeq", COMPLETED + message + ELLIPSIZE);
	}

	/**
	 * Log action failed message.
	 * 
	 * @param action
	 *            backup or restore
	 * @param actionType
	 *            contacts or call logs or messages or dictionary.
	 */
	public static void f(String message) {
		Log.e("@ceeq", FAILED + message + ELLIPSIZE);
	}

	/**************************************
	 *************** Messages ***************
	 **************************************/

	public static final int CALLS_M = 1;
	public static final int LOCATION_M = 2;
	public static final int NEW_LOCATION_M = 3;
	public static final int NOW_M = 4;
	public static final int PROTECT_ME_M = 5;
	public static final int SIM_CHANGE_M = 6;
	public static final int FAIL_M = 7;

	/**
	 * Send message based on message type
	 * 
	 * @param deliverTo
	 * @param messageType
	 */
	public static void sendMessage(Context ctx, String deliverTo, int messageType) {
		String message = "";
		switch (messageType) {
		case CALLS_M:
			message = getCallsMessage(ctx);
			break;
		case LOCATION_M:
			message = getLastLocationMessage(ctx);
			d(message);
			break;
		case NEW_LOCATION_M:
			message = getNewLocationMessage(ctx);
			d(message);
			break;
		case NOW_M:
			message = getDetailsMessage(ctx);
			d(message);
			break;
		case PROTECT_ME_M:
			message = getProtectMeMessage(ctx);
			d(message);
			break;
		case SIM_CHANGE_M:
			message = getSimChangeMessage(ctx);
			break;
		case FAIL_M:
			message = getFailedChangeMessage();
			break;
		default:
			break;
		}

		try {
			// String senderAddress = getString(ctx, SENDER_ADDRESS);
			if (!message.isEmpty()) {
				// SmsManager.getDefault()..sendTextMessage(senderAddress, null, message, null, null);
			}
		} catch (Exception exception) {
			d("Either the mobile number empty or not correct.");
		}
	}

	private static String getFailedChangeMessage() {
		return "Sorry, The PIN entered by you is incorrect.";
	}

	/**
	 * Create a call log message message
	 * 
	 * @return
	 */
	public static String getCallsMessage(Context ctx) {
		return "Last 10 calls from device are : " + getCalls(ctx, 10);
	}

	/**
	 * Create a last location message
	 * 
	 * @return
	 */
	public static String getLastLocationMessage(Context ctx) {
		return "Last location of device is : " + getLocationMessage(ctx);
	}

	/**
	 * Create a new location message
	 * 
	 * @return
	 */
	public static String getNewLocationMessage(Context ctx) {
		return "Device location has changed. New location is : " + getLocationMessage(ctx);
	}

	/**
	 * Create raw location message
	 * 
	 * @return
	 */
	public static String getLocationMessage(Context ctx) {
		return getStringPrefs(ctx, LAST_LOCATION_LATITUDE) + ", " + getStringPrefs(ctx, LAST_LOCATION_LATITUDE);
	}

	public static final String COLUMN_NUMBER = CallLog.Calls.NUMBER;
	public static final String DURATION = CallLog.Calls.DURATION;
	public static final String TYPE = CallLog.Calls.TYPE;

	/**
	 * Get last n calls from the logs
	 * 
	 * @return String
	 */
	public static String getCalls(Context ctx, int n) {
		Cursor cs = ctx.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
		StringBuffer sb = new StringBuffer();
		int count = 0;
		try {
			if (cs.moveToFirst()) {
				do {
					if (cs.getInt(cs.getColumnIndex(TYPE)) != 3) {
						String number = cs.getString(cs.getColumnIndex(COLUMN_NUMBER));
						String duration = cs.getString(cs.getColumnIndex(DURATION));
						int durations = (Integer.parseInt(duration) / 60);
						sb.append(number + " " + durations + "mins\n");
						count++;
					}
				} while (cs.moveToNext() && count < n);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cs.close();
		}
		return sb.toString();
	}

	/**
	 * Create SIM changed message
	 * 
	 * @return
	 */
	public static String getSimChangeMessage(Context ctx) {
		return getStringPrefs(ctx, EMERGENCY_MESSAGE) + "\n" + "New Sim Number : " + getPhoneData(ctx, SIM_ID) + "\n"
				+ "New Sim Operator : " + getPhoneData(ctx, OPERATOR) + "\n" + "New Sim Subscriber Id : "
				+ getPhoneData(ctx, IMSI) + "\n" + "Your Device IEMI: " + getPhoneData(ctx, IEMI) + "\n";
	}

	/**
	 * Create protect me message
	 * 
	 * @return
	 */
	public static String getProtectMeMessage(Context ctx) {
		return "Help " + getStringPrefs(ctx, ACCOUNT_USER_NAME) + getStringPrefs(ctx, DISTRESS_MESSAGE) + "\n"
				+ "Last User Location : " + getLocationMessage(ctx) + "\n" + "Battery Status : "
				+ getPhoneData(ctx, BATTERY_LEVEL)
				+ "\nCeeq will send you regular location updates every 10 minutes.\n";
	}

	/**
	 * Get current phone details
	 * 
	 * @return
	 */
	public static String getDetailsMessage(Context ctx) {
		return "Current \n" + "Sim Number : " + Utils.getPhoneData(ctx, Utils.SIM_ID) + "\n" + "Sim Operator : "
				+ getPhoneData(ctx, OPERATOR) + "\n" + "Sim Subscriber Id : " + getPhoneData(ctx, IMSI) + "\n"
				+ "Location :" + getLocationMessage(ctx) + "\n";
	}

	/**************************************
	 ************** Preferences **************
	 **************************************/

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
	/**
	 * Application initial setup status
	 */
	public static final String APP_INITIALIZATION_STATUS = "applicationHasInitialised";
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
	public static final String EMERGENCY_CONTACT_NAME = "emergencyName";
	public static final String EMERGENCY_CONTACT_NUMBER = "emergencyNumber";
	public static final String EMERGENCY_MESSAGE = "emergencyMessage";
	public static final String DISTRESS_MESSAGE = "distressMessage";
	public static final String LAST_LOCATION_LATITUDE = "lastLatitude";
	public static final String LAST_LOCATION_LONGITUDE = "lastLongitude";
	public static final String DEVICE_HAS_PASSWORD = "deviceHasPassword";
	public static final String APP_UNINSTALL_PROTECTION = "appUninstallProtection";
	public static final String DEVICE_ID = "deviceId";
	public static final String SENDER_ADDRESS = "senderAddress";

	public static Boolean getBooleanPrefs(Context ctx, String key) {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(key, false);
	}

	public static void setBooleanPrefs(Context ctx, String key, Boolean value) {
		PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean(key, value).commit();
	}

	public static String getStringPrefs(Context ctx, String key) {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getString(key, "");
	}

	public static void setStringPrefs(Context ctx, String key, String value) {
		PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(key, value).commit();
	}

	public static int getIntPrefs(Context ctx, String key) {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getInt(key, 0);
	}

	public static void setIntPrefs(Context ctx, String key, int value) {
		PreferenceManager.getDefaultSharedPreferences(ctx).edit().putInt(key, value).commit();
	}

	public static void clearPrefs(Context ctx) {
		PreferenceManager.getDefaultSharedPreferences(ctx).edit().clear().commit();
	}

	/**************************************
	 ***************** Files *****************
	 **************************************/

	public static final String APP_PATH = "/data/ceeq";
	public static final String BACKUP_PATH = "/data/ceeq/backups";
	public static final String CAM_PATH = "/data/ceeq/camera";

	public static boolean haveBackupFiles(Context ctx) {
		if (!enabled(EXTERNAL_STORAGE, ctx)) {
			Toast.makeText(ctx, "Sorry, External storage not found.", Toast.LENGTH_SHORT).show();
		}
		File storageLocation = new File(Environment.getExternalStorageDirectory(), BACKUP_PATH);
		if (!storageLocation.exists()) {
			storageLocation.mkdirs();
		}
		if ((storageLocation.listFiles()).length == 0)
			return false;
		return true;
	}

	public static File[] getFiles(Context ctx, String path) {
		if (!enabled(EXTERNAL_STORAGE, ctx)) {
			Toast.makeText(ctx, "Sorry, External storage not found.", Toast.LENGTH_SHORT).show();
		}
		File storageLocation = new File(Environment.getExternalStorageDirectory(), path);
		if (!storageLocation.exists()) {
			storageLocation.mkdirs();
		}
		File[] files = storageLocation.listFiles();
		return files;
	}

	// change this hashmap
	public static HashMap<String, ArrayList<String>> getFileNames(File[] files) {
		HashMap<String, ArrayList<String>> fileNames = new HashMap<String, ArrayList<String>>();
		for (File file : files)
			fileNames.put(
					file.getName(),
					new ArrayList<String>(
							Arrays.asList(new String[] {
									fileType(file.getName()),
									(file.length() / 1024) + "",
									DateTime.forInstant(file.lastModified(), TimeZone.getDefault()).toString()
											.substring(0, 10) })));
		return fileNames;
	}

	public static String fileType(String name) {
		if (name.contains("contact"))
			return "Contacts";
		if (name.contains("message"))
			return "Messages";
		if (name.contains("calls"))
			return "Call logs";
		if (name.contains("dictionary"))
			return "User Dictionary";
		return name;
	}

	public static File createFile(String path, String type, Context ctx) throws IOException,
			ExternalStorageNotFoundException {
		if (!enabled(EXTERNAL_STORAGE, ctx)) {
			throw new ExternalStorageNotFoundException();
		}

		File storageLocation = new File(Environment.getExternalStorageDirectory(), path);

		if (!storageLocation.exists()) {
			storageLocation.mkdirs();
		}

		File file = new File(storageLocation, getFileName(type));
		file.createNewFile();
		return file;
	}

	public static InputStream readFile(String fileName, Context ctx) throws FileNotFoundException,
			ExternalStorageNotFoundException {
		if (!enabled(EXTERNAL_STORAGE, ctx)) {
			throw new ExternalStorageNotFoundException();
		}
		return new FileInputStream(Environment.getExternalStorageDirectory() + BACKUP_PATH + "/" + fileName);
	}

	public static String writeFile(String text, String type, Context ctx) throws IOException,
			ExternalStorageNotFoundException {
		FileOutputStream fos = new FileOutputStream(createFile(BACKUP_PATH, type, ctx));
		DataOutputStream out = new DataOutputStream(fos);
		out.writeBytes(text);
		out.close();
		return getFileName(type);
	}

	public static boolean deleteFile(String path, String[] fileName, Context ctx)
			throws ExternalStorageNotFoundException {
		boolean deleted = false;
		if (!enabled(EXTERNAL_STORAGE, ctx)) {
			throw new ExternalStorageNotFoundException();
		} else {

			File storageLocation = new File(Environment.getExternalStorageDirectory(), path);
			for (int i = 0; i < fileName.length; i++) {
				new File(storageLocation + "/" + fileName[i]).delete();
			}
			deleted = true;
		}
		return deleted;
	}

	public static String getFileName(String type) {
		if (type.equals("cam"))
			return type + "_" + getDate() + ".jpg";
		else
			return type + "_" + getDate() + ".xml";
	}

	public static String getDate() {
		return DateTime.now(TimeZone.getDefault()).format("DD-MM-YY-hh-mm-ss").toString();
	}

	/**************************************
	 *************** Camera ****************
	 **************************************/

	public static PictureCallback getJpegCallback(final Context ctx) {

		PictureCallback jpeg = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				FileOutputStream fos;
				try {
					try {
						fos = new FileOutputStream(createFile(CAM_PATH, "cam", ctx));
						fos.write(data);
						fos.close();
					} catch (ExternalStorageNotFoundException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		return jpeg;
	}

	/**************************************
	 *************** Xml Parser **************
	 **************************************/

	/**
	 * @param in
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * 
	 * @return XmlPullParser
	 */
	public static XmlPullParser getParser(InputStream in) throws XmlPullParserException, IOException,
			IllegalArgumentException {
		XmlPullParser parser = null;

		try {
			parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return parser;
	}

	/**
	 * 
	 * @param parser
	 * @param tag
	 * @return String
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static String readTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
		String tagData = "";
		parser.require(XmlPullParser.START_TAG, null, tag);
		if (parser.next() == XmlPullParser.TEXT) {
			tagData = parser.getText();
			parser.nextTag();
		}
		parser.require(XmlPullParser.END_TAG, null, tag);
		return tagData;
	}

	/**
	 * 
	 * @param parser
	 * @param tag
	 * @param attributeName
	 * @return String
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static String readAttribute(XmlPullParser parser, String tag, String attributeName) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, tag);
		String attributeData = parser.getAttributeValue(null, attributeName);
		parser.nextTag();
		parser.require(XmlPullParser.END_TAG, null, tag);
		return attributeData;
	}
}
