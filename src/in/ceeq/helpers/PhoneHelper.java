package in.ceeq.helpers;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class PhoneHelper {

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
	public static final int APP_VERSION = 19;

	private Context context;
	private TelephonyManager telephonyManager;
	private PreferencesHelper preferencesHelper;

	public PhoneHelper(Context context) {
		this.context = context;
		preferencesHelper = PreferencesHelper.getInstance(context);
	}

	public static PhoneHelper getInstance(Context context) {
		return new PhoneHelper(context);
	}

	public String get(int dataType) {
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
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
			data = getSize();
			break;
		case DENSITY:
			data = getDensity();
			break;
		case UNIQUE_ID:
			data = getUniqueDeviceId();
			break;
		case APP_COUNT:
			data = getNumberOfApplications() + "";
			break;
		case BATTERY_LEVEL:
			data = (getCurrentBatteryLevel() * 100) + "%";
			break;
		case REGISTRATION_ID:
			data = getRegistrationId();
			break;
		case APP_VERSION:
			data = getAppVersion() + "";
			break;
		default:
			data = "Not Available.";
			break;

		}

		return data;
	}

	public void set(int dataType, String data) {

		switch (dataType) {
		case REGISTRATION_ID:
			setRegistrationId(data);
			break;
		case APP_VERSION:
			break;
		default:
			break;

		}
	}

	public boolean enabled(int featureType) {
		boolean data = false;
		switch (featureType) {
		case GPS:
			data = isGpsEnabled();
			break;
		case INTERNET:
			data = isInternetEnabled();
			break;
		case PLAY_SERVICES:
			data = isPlayServiceInstalled();
			break;
		case EXTERNAL_STORAGE:
			data = isExternalStorageEnabled();
			break;
		default:
			data = false;
			break;

		}
		return data;
	}

	private boolean isGpsEnabled() {
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			return true;
		return false;
	}

	private boolean isPlayServiceInstalled() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);
		if (ConnectionResult.SUCCESS == resultCode)
			return true;
		return false;
	}

	private boolean isExternalStorageEnabled() {
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

	private boolean isInternetEnabled() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	private String getUniqueDeviceId() {
		return android.os.Build.MANUFACTURER.substring(0, 3).toUpperCase(
				Locale.getDefault())
				+ "-" + randomString().substring(0, 6) + get(IEMI);
	}

	private String randomString() {
		return Long.toHexString(Double.doubleToLongBits(Math.random()))
				.toUpperCase();
	}

	private String getSize() {
		Display display = ((Activity) context).getWindowManager()
				.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;

		return width + "pixels x " + height + "pixels";
	}

	private String getDensity() {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		int densityDpi = dm.densityDpi;
		return densityDpi + " PPI";
	}

	private String getRegistrationId() {
		String registrationId = preferencesHelper
				.getString(PreferencesHelper.GCM_REGISTRATION_ID);
		if (registrationId.isEmpty())
			return "";
		int registeredVersion = preferencesHelper
				.getInt(PreferencesHelper.APP_VERSION);
		int currentVersion = getAppVersion();
		if (registeredVersion != currentVersion)
			return "";
		return registrationId;
	}

	private int getAppVersion() {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
	}

	private void setRegistrationId(String regId) {
		preferencesHelper.setString(PreferencesHelper.GCM_REGISTRATION_ID,
				regId);
		preferencesHelper
				.setInt(PreferencesHelper.APP_VERSION, getAppVersion());
	}

	private int getNumberOfApplications() {
		PackageManager pm = context.getPackageManager();
		return pm.getInstalledApplications(PackageManager.GET_META_DATA).size();
	}

	private float getCurrentBatteryLevel() {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		return (level / (float) scale);
	}

}
