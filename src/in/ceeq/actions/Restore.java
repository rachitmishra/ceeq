package in.ceeq.actions;

import in.ceeq.services.Backups;
import android.content.Context;
import android.content.Intent;

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
				.putExtra(Backups.ACTION, Backups.ACTION_RESTORE)
				.putExtra(Backups.ACTION_TYPE, actionType);
		context.startService(startRestore);
	}
}
