/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.actions;

import hirondelle.date4j.DateTime;
import in.ceeq.helpers.Logger;
import in.ceeq.services.Backups;

import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Backup {
	public static final String INTENT_ACTION_ACTION_BACKUP = "in.ceeq.action.BACKUP";
	public static final int ALARM_ACTIVATION_REQUEST = 2337;
	public static final String INTENT_ACTION_MESSAGE = "in.ceeq.action.MESSAGE";
	public static final int OFF = 0;
	public static final int ON = 1;

	private Context context;

	public Backup(Context context) {
		this.context = context;
	}

	public static Backup getInstance(Context context) {
		return new Backup(context);
	}

	public void backup(int data) {
		Intent startBackup = new Intent(context, Backups.class)
				.putExtra(Backups.ACTION, Backups.ACTION_BACKUP)
				.putExtra(Backups.ACTION_TYPE, data)
				.putExtra(Backups.ACTION_PARENT, Backups.ACTION_PARENT_ACTIVITY);
		context.startService(startBackup);
	}

	public void autoBackups(int state) {
		PendingIntent pi;
		AlarmManager alarms = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		switch (state) {
		case OFF:
			pi = PendingIntent.getBroadcast(context, ALARM_ACTIVATION_REQUEST,
					new Intent(INTENT_ACTION_ACTION_BACKUP),
					PendingIntent.FLAG_CANCEL_CURRENT);
			alarms.cancel(pi);

			break;
		case ON:
			Logger.d("Turning alarm ON");
			pi = PendingIntent.getBroadcast(context, ALARM_ACTIVATION_REQUEST,
					new Intent(INTENT_ACTION_ACTION_BACKUP),
					PendingIntent.FLAG_CANCEL_CURRENT);
			alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, new DateTime(
					DateTime.today(TimeZone.getDefault()) + " 02:00:00")
					.getMilliseconds(TimeZone.getDefault()),
					AlarmManager.INTERVAL_DAY, pi);
			break;
		}

	}
}
