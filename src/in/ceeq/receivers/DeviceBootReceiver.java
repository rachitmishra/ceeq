/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import in.ceeq.commons.Utils;
import in.ceeq.services.CommandService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class DeviceBootReceiver extends BroadcastReceiver {

	public static final int ALARM_ACTIVATION_REQUEST = 9012;

	public DeviceBootReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		showNotification(context);
		checkSimChange(context);
		if (Utils.getBooleanPrefs(context, Utils.AUTO_BACKUP_STATUS))
			setupAlarms(context);
	}

	public void showNotification(Context context) {
		if (Utils.getBooleanPrefs(context, Utils.NOTIFICATIONS_STATUS))
			Utils.showNotifications(context);
	}

	public void checkSimChange(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		try {
			if (!tm.getSimSerialNumber().equals(Utils.getStringPrefs(context, Utils.SIM_NUMBER))) {

				try {
					Intent commands = new Intent(context, CommandService.class);
					commands.putExtra(CommandService.ACTION, CommandService.SEND_SIM_CHANGE_MESSAGE);
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
		Utils.scheduledBackup(context, true);
	}

}
