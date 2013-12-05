/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.actions;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;

import in.ceeq.R;

public class Admin {

	private static final int DEVICE_ADMIN_ACTIVATION_REQUEST = 9014;
	private Activity activity;
	private ComponentName deviceAdminComponentName;

	public Admin(Activity activity) {
		this.activity = activity;
	}

	public static Admin getInstance(Activity activity) {
		return new Admin(activity);
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
