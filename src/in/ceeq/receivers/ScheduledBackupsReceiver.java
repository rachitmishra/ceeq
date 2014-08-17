/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.receivers;

import in.ceeq.services.BackupService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScheduledBackupsReceiver extends BroadcastReceiver {

	public static final int BACKUP = 1;
	public static final int ACTION_PARENT_SERVICE = 2;

	public ScheduledBackupsReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		Intent takeBackup = new Intent(context, BackupService.class).setAction(BackupService.ACTION_AUTO_BACKUP).putExtra(
				BackupService.ACTION_DATA, BackupService.ACTION_DATA_ALL);
		context.startService(takeBackup);

	}
}
