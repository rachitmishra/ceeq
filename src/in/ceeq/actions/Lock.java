package in.ceeq.actions;

import in.ceeq.helpers.Logger;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.receivers.DeviceAdmin;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

public class Lock {

	private DevicePolicyManager devicePolicyManager;
	private Context context;
	private ComponentName deviceAdminComponentName;

	public Lock(Context context) {
		this.context = context;
		devicePolicyManager = (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		deviceAdminComponentName = new ComponentName(context, DeviceAdmin.class);
	}

	public static Lock getInstance(Context context) {
		return new Lock(context);
	}

	public void lock() {
		if (getInitialLockState()) {
			lockNow();
		} else {
			setPasswordThenLock();
		}
	}

	public void setPasswordThenLock() {
		devicePolicyManager.setPasswordQuality(deviceAdminComponentName,
				DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
		devicePolicyManager.setPasswordMinimumNumeric(deviceAdminComponentName,
				6);
		devicePolicyManager.resetPassword(PreferencesHelper
				.getInstance(context).getString(PreferencesHelper.PIN_NUMBER),
				DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
		devicePolicyManager.lockNow();
	}

	public void lockNow() {
		devicePolicyManager.lockNow();
	}

	public void remove() {
		devicePolicyManager.setPasswordQuality(deviceAdminComponentName,
				DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
		devicePolicyManager.resetPassword("", 0);
	}

	public void setInitialLockState() {
		PreferencesHelper.getInstance(context).setBoolean(
				PreferencesHelper.DEVICE_HAS_PASSWORD, hasPassword());
	}

	public boolean getInitialLockState() {
		return PreferencesHelper.getInstance(context).getBoolean(
				PreferencesHelper.DEVICE_HAS_PASSWORD);
	}

	public boolean hasPassword() {
		int currentPasswordQuality = devicePolicyManager
				.getPasswordQuality(null);
		devicePolicyManager.setPasswordQuality(deviceAdminComponentName,
				DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
		boolean hasPassword = devicePolicyManager.isActivePasswordSufficient();
		devicePolicyManager.setPasswordQuality(deviceAdminComponentName,
				currentPasswordQuality);
		Logger.d("The user has password set: " + hasPassword);
		return hasPassword;
	}
}
