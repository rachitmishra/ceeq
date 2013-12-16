/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.actions;

import in.ceeq.R;
import in.ceeq.receivers.DeviceAdmin;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class Admin {

	private static final int DEVICE_ADMIN_ACTIVATION_REQUEST = 9014;
	private Activity activity;
	private Context context;
	private ComponentName deviceAdminComponentName;
	private DevicePolicyManager devicePolicyManager;

	public Admin(Context context) {
		this.context = context;
		this.activity = (Activity) context;
	}

	public static Admin getInstance(Context context) {
		return new Admin(context);
	}

	public boolean isRegistered() {
		devicePolicyManager = (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		deviceAdminComponentName = new ComponentName(context, DeviceAdmin.class);
		return (devicePolicyManager.isAdminActive(deviceAdminComponentName)) ? true
				: false;
	}

	public void register() {

		activity.startActivityForResult(
				new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
						.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
								deviceAdminComponentName).putExtra(
								DevicePolicyManager.EXTRA_ADD_EXPLANATION,
								activity.getString(R.string.help_note_25)),
				DEVICE_ADMIN_ACTIVATION_REQUEST);
	}
}
