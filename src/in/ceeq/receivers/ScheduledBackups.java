/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import in.ceeq.services.Backups;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScheduledBackups extends BroadcastReceiver {
	
	public static final int BACKUP = 1;
	public static final int ACTION_PARENT_SERVICE = 2;
	public ScheduledBackups() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		Intent takeBackup = new Intent(context, Backups.class)
				.putExtra(Backups.ACTION, BACKUP)
				.putExtra(Backups.ACTION_TYPE, Backups.ACTION_TYPE_ALL)
				.putExtra(Backups.ACTION_PARENT, ACTION_PARENT_SERVICE);
		context.startService(takeBackup);

	}
}
