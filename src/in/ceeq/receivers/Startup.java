/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import hirondelle.date4j.DateTime;
import in.ceeq.actions.Notifications;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.services.Commander;
import in.ceeq.services.Commander.Command;

import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
		preferencesHelper = new PreferencesHelper(context);
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
					preferencesHelper.getString("simNumber"))) {

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
		PendingIntent pi;
		AlarmManager alarms = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		pi = PendingIntent.getBroadcast(context, ALARM_ACTIVATION_REQUEST,
				new Intent("in.ceeq.ACTION_BACKUP"),
				PendingIntent.FLAG_CANCEL_CURRENT);
		alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				new DateTime(DateTime.today(TimeZone.getDefault())
						+ " 02:00:00").getMilliseconds(TimeZone.getDefault()),
				AlarmManager.INTERVAL_DAY, pi);
	}

}
