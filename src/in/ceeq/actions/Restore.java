package in.ceeq.actions;

import android.content.Context;
import android.content.Intent;
import android.os.Messenger;

import in.ceeq.activities.Home;
import in.ceeq.services.Backups;
import in.ceeq.services.Backups.Action;

public class Restore {
	private Context context;
	public Restore(Context context) {
		this.context = context;
	}

	public static Restore getInstance(Context context) {
		return new Restore(context);
	}

	public void restore(int actionType) {
		Intent startRestore = new Intent(context, Backups.class)
				.putExtra(Backups.ACTION, Action.RESTORE)
				.putExtra(Backups.ACTION_TYPE, actionType)
				.putExtra(Home.MESSENGER, new Messenger(Home.messageHandler));
		context.startService(startRestore);
	}
}
