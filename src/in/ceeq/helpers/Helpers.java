/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.helpers;

import hirondelle.date4j.DateTime;
import in.ceeq.activities.Home.ComponentState;
import in.ceeq.activities.Home.SwitchState;
import in.ceeq.receivers.DeviceAdmin;
import in.ceeq.receivers.LowBattery;
import in.ceeq.receivers.OutgoingCalls;
import in.ceeq.receivers.PowerButton;
import in.ceeq.receivers.ScheduledBackups;

import java.util.Locale;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Environment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class Helpers {

	public static final int ALARM_ACTIVATION_REQUEST = 9012;

	private Context context;
	private LocationManager locationManager;
	private PreferencesHelper preferencesHelper;
	private DevicePolicyManager devicePolicyManager;
	private ComponentName deviceAdminComponentName;
	private ConnectivityManager connectivityManager;
	private PackageManager packageManager;

	public enum Receivers {
		OUTGOING_CALLS_RECEIVER, POWER_BUTTON_RECEIVER, SCHEDULED_BACKUPS_RECEIVER, LOW_BATTERY_RECEIVER
	}

	public Helpers(Context context) {
		this.context = context;
		preferencesHelper = new PreferencesHelper(context);
	}

	public static Helpers getInstance(Context context) {
		return new Helpers(context);
	}

	public boolean hasGpsEnabled() {
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			return true;
		return false;
	}

	public boolean hasDeviceAdminEnabled() {
		devicePolicyManager = (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		deviceAdminComponentName = new ComponentName(context, DeviceAdmin.class);

		if (devicePolicyManager.isAdminActive(deviceAdminComponentName))
			return true;
		return false;
	}

	public boolean isGooglePlayConnected() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);
		if (ConnectionResult.SUCCESS == resultCode)
			return true;
		return false;
	}

	public boolean hasExternalStorage() {
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

	public boolean hasInternet() {
		connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public String generateDeviceId() {
		return android.os.Build.MANUFACTURER.substring(0, 3).toUpperCase(
				Locale.getDefault())
				+ "-" + randomString().substring(0, 6);
	}

	public String randomString() {
		return Long.toHexString(Double.doubleToLongBits(Math.random()))
				.toUpperCase();
	}

	public String getNewFileName() {
		return DateTime.now(TimeZone.getDefault()).format("DD-MM-YY-hh-mm-ss")
				.toString();
	}

	public float getBatteryLevel() {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		return (level / (float) scale);
	}

	public String getRegistrationId() {
		String registrationId = preferencesHelper
				.getString(PreferencesHelper.GCM_REGISTRATION_ID);
		if (registrationId.isEmpty())
			return "";
		int registeredVersion = preferencesHelper
				.getInt(PreferencesHelper.APP_VERSION);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion)
			return "";
		return registrationId;
	}

	public int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public void storeRegistrationId(String regId) {
		preferencesHelper.setString(PreferencesHelper.GCM_REGISTRATION_ID,
				regId);
		preferencesHelper.setInt(PreferencesHelper.APP_VERSION,
				getAppVersion(context));
	}

	public void setupAlarms(SwitchState state) {
		PendingIntent pi;
		AlarmManager alarms = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		switch (state) {
		case OFF:
			pi = PendingIntent.getBroadcast(context, ALARM_ACTIVATION_REQUEST,
					new Intent("in.ceeq.ACTION_BACKUP"),
					PendingIntent.FLAG_CANCEL_CURRENT);
			alarms.cancel(pi);

			break;
		case ON:
			pi = PendingIntent.getBroadcast(context, ALARM_ACTIVATION_REQUEST,
					new Intent("in.ceeq.ACTION_BACKUP"),
					PendingIntent.FLAG_CANCEL_CURRENT);
			alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, new DateTime(
					DateTime.today(TimeZone.getDefault()) + " 02:00:00")
					.getMilliseconds(TimeZone.getDefault()),
					AlarmManager.INTERVAL_DAY, pi);
			break;
		}

	}

	public void setupReceivers(Receivers receiverName, ComponentState state) {
		packageManager = context.getPackageManager();
		ComponentName componentName = null;
		switch (receiverName) {
		case LOW_BATTERY_RECEIVER:
			componentName = new ComponentName(context, LowBattery.class);
			break;
		case OUTGOING_CALLS_RECEIVER:
			componentName = new ComponentName(context, OutgoingCalls.class);
			break;
		case POWER_BUTTON_RECEIVER:
			componentName = new ComponentName(context, PowerButton.class);
			break;
		case SCHEDULED_BACKUPS_RECEIVER:
			componentName = new ComponentName(context, ScheduledBackups.class);
			break;
		}

		switch (state) {
		case DISABLE:
			packageManager.setComponentEnabledSetting(componentName,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);
			break;
		case ENABLE:
			packageManager.setComponentEnabledSetting(componentName,
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
					PackageManager.DONT_KILL_APP);
			break;
		}
	}

	public static void Toast(String message) {
		// Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}
