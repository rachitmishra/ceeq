/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import in.ceeq.actions.Backup;
import in.ceeq.actions.Backup.State;
import in.ceeq.actions.Notifications;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.services.Commander;
import in.ceeq.services.Commander.Command;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class Startup extends BroadcastReceiver {

	private PreferencesHelper preferencesHelper;

	public static final int ALARM_ACTIVATION_REQUEST = 9012;

	public Startup() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		preferencesHelper = PreferencesHelper.getInstance(context);
		showNotification(context);
		checkSimChange(context);
		if (preferencesHelper.getBoolean(PreferencesHelper.AUTO_BACKUP_STATUS))
			setupAlarms(context);
	}

	public void showNotification(Context context) {
		if (preferencesHelper
				.getBoolean(PreferencesHelper.NOTIFICATIONS_STATUS))
			Notifications.getInstance(context).show();
	}

	public void checkSimChange(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		try {
			if (!tm.getSimSerialNumber().equals(
					preferencesHelper.getString(PreferencesHelper.SIM_NUMBER))) {

				try {
					Intent commands = new Intent(context, Commander.class);
					commands.putExtra(Commander.ACTION,
							Command.SEND_SIM_CHANGE_MESSAGE);
					context.startService(commands);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setupAlarms(Context context) {
		Backup.getInstance(context).autoBackups(State.ON);
	}

}
