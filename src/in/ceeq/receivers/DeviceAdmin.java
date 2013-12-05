/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;

import in.ceeq.helpers.PreferencesHelper;

public class DeviceAdmin extends DeviceAdminReceiver {

	private PreferencesHelper preferencesHelper;

	@Override
	public DevicePolicyManager getManager(Context context) {
		return super.getManager(context);
	}

	@Override
	public CharSequence onDisableRequested(Context context, Intent intent) {
		return super.onDisableRequested(context, intent);
	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		super.onDisabled(context, intent);
		preferencesHelper.setBoolean(PreferencesHelper.DEVICE_ADMIN_STATUS,
				false);
	}

	@Override
	public void onEnabled(Context context, Intent intent) {
		super.onEnabled(context, intent);
		preferencesHelper.setBoolean(PreferencesHelper.DEVICE_ADMIN_STATUS,
				true);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		preferencesHelper = PreferencesHelper.getInstance(context);
	}

}
