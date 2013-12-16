/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.helpers;

import hirondelle.date4j.DateTime;

import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Environment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class Helpers {

	private Context context;
	private LocationManager locationManager;
	private PreferencesHelper preferencesHelper;

	private ConnectivityManager connectivityManager;

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
}
