/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import java.util.TimeZone;

import hirondelle.date4j.DateTime;
import in.ceeq.helpers.NotificationsHelper;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.services.Commander;
import in.ceeq.services.Commander.Command;

public class Startup extends BroadcastReceiver {

	private PreferencesHelper preferencesHelper;
	private NotificationsHelper notificationsHelper;

	public static final int ALARM_ACTIVATION_REQUEST = 9012;

	public Startup() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		preferencesHelper = new PreferencesHelper(context);
		notificationsHelper = new NotificationsHelper(context);
		showNotification();
		checkSimChange(context);
		if (preferencesHelper.getBoolean("autoBackup"))
			scheduleAlarms(context);
	}

	public void showNotification() {
		if (preferencesHelper
				.getBoolean(PreferencesHelper.NOTIFICATIONS_STATUS))
			notificationsHelper.defaultNotification();
	}

	public void checkSimChange(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		try {
			if (!tm.getSimSerialNumber().equals(
					preferencesHelper.getString("simNumber"))) {
				Intent i2 = new Intent(context,
						in.ceeq.activities.Lockscreen.class);
				i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				try {
					context.startActivity(i2);
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
	public void scheduleAlarms(Context context) {
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
