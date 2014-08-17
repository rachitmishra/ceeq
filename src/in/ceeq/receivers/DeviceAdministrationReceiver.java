/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import in.ceeq.R;
import in.ceeq.commons.Utils;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DeviceAdministrationReceiver extends DeviceAdminReceiver {

	private DevicePolicyManager devicePolicyManager;

	@Override
	public DevicePolicyManager getManager(Context context) {
		return super.getManager(context);
	}

	@Override
	public CharSequence onDisableRequested(Context context, Intent intent) {
		devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		if (Utils.getBooleanPrefs(context, Utils.APP_UNINSTALL_PROTECTION)) {
			Utils.lock(context);
		}
		return context.getString(R.string.help_note_35);
	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		super.onDisabled(context, intent);
		devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		Utils.setBooleanPrefs(context, Utils.DEVICE_ADMIN_STATUS, false);
	}

	@Override
	public void onEnabled(Context context, Intent intent) {
		super.onEnabled(context, intent);
		devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		Utils.setInitialLockState(context);
		Utils.setBooleanPrefs(context, Utils.DEVICE_ADMIN_STATUS, true);
	}

	@Override
	public void onPasswordChanged(Context context, Intent intent) {
		super.onPasswordChanged(context, intent);
	}

	@Override
	public void onPasswordFailed(Context context, Intent intent) {
		super.onPasswordFailed(context, intent);
		devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		int failedAttempts = devicePolicyManager.getCurrentFailedPasswordAttempts();
		if (failedAttempts == 4) {
			Toast.makeText(context, "This is your last attempt, device will be wiped after this.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onPasswordSucceeded(Context context, Intent intent) {
		super.onPasswordSucceeded(context, intent);
		if (Utils.getInitialLockState(context))
			Utils.removeLock(context);
	}
}
