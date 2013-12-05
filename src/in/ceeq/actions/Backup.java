/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.actions;

import android.content.Context;
import android.content.Intent;
import android.os.Messenger;

import in.ceeq.activities.Home;
import in.ceeq.services.Backups;
import in.ceeq.services.Backups.Action;
import in.ceeq.services.Backups.ActionParent;

public class Backup {
	private Context context;
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
}
