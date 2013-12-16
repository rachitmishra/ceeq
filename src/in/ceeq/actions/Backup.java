/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.actions;

import hirondelle.date4j.DateTime;
import in.ceeq.activities.Home;
import in.ceeq.services.Backups;
import in.ceeq.services.Backups.Action;
import in.ceeq.services.Backups.ActionParent;

import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Messenger;

public class Backup {
	private Context context;
	public static final int ALARM_ACTIVATION_REQUEST = 9012;

	public enum State {
		ON, OFF
	}

	public Backup(Context context) {
		this.context = context;
	}

	public static Backup getInstance(Context context) {
		return new Backup(context);
	}

	public void backup(int data) {
		Intent startBackup = new Intent(context, Backups.class)
				.putExtra(Backups.ACTION, Action.BACKUP)
				.putExtra(Backups.ACTION_TYPE, data)
				.putExtra(Backups.ACTION_PARENT, ActionParent.ACTIVITY)
				.putExtra(Home.MESSENGER, new Messenger(Home.messageHandler));
		context.startService(startBackup);
	}

	public void autoBackups(State state) {
		PendingIntent pi;
		AlarmManager alarms = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		switch (state) {
		case OFF:
			pi = PendingIntent.getBroadcast(context, ALARM_ACTIVATION_REQUEST,
					new Intent("in.ceeq.ACTION_BACKUP"),
					PendingIntent.FLAG_CANCEL_CURRENT);
			alarms.cancel(pi);

			break;
		case ON:
			pi = PendingIntent.getBroadcast(context, ALARM_ACTIVATION_REQUEST,
					new Intent("in.ceeq.ACTION_BACKUP"),
					PendingIntent.FLAG_CANCEL_CURRENT);
			alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, new DateTime(
					DateTime.today(TimeZone.getDefault()) + " 02:00:00")
					.getMilliseconds(TimeZone.getDefault()),
					AlarmManager.INTERVAL_DAY, pi);
			break;
		}

	}
}
