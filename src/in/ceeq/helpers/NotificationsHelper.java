package in.ceeq.helpers;

import in.ceeq.R;
import in.ceeq.activities.Home;
import in.ceeq.services.Backups.Action;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class NotificationsHelper {

	public static final int DEFAULT_NOTIFICATION_ID = 9007;
	public static final int NOTIFICATION_ID_RESERVED = 9008;

	private NotificationManager notificationManager;
	private Context context;
	private NotificationCompat.Builder nBuilder, bBuilder;
	private PreferencesHelper preferencesHelper;

	public NotificationsHelper(Context context) {
		this.context = context;
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		preferencesHelper = new PreferencesHelper(context);
	}

	public static NotificationsHelper getInstance(Context context) {
		return new NotificationsHelper(context);
	}

	public void showPersistentNotification() {
		Intent openHome = new Intent(context, Home.class)
				.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		PendingIntent pi = PendingIntent.getActivity(context, 0, openHome, 0);
		nBuilder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Ceeq")
				.setContentText(
						setApplicationStatus(preferencesHelper
								.getBoolean("appStatus"))).setOngoing(true);
		nBuilder.setContentIntent(pi);
		notificationManager.notify(DEFAULT_NOTIFICATION_ID, nBuilder.build());
	}

	public void removePersistentNotification() {
		notificationManager.cancel(DEFAULT_NOTIFICATION_ID);
	}

	public void removeAllNotifications() {
		notificationManager.cancelAll();
	}

	public void setContentText(String text) {
		nBuilder.setContentText(text);
	}

	public void startBackupNotification(Action action) {

		Intent openHome = new Intent(context, Home.class)
				.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		PendingIntent pi = PendingIntent.getActivity(context, 0, openHome, 0);
		bBuilder = new NotificationCompat.Builder(context)
				.setContentTitle("Ceeq").setProgress(0, 0, true)
				.setOngoing(true);
		switch (action) {
		case BACKUP:
			bBuilder.setContentText("Backup in progress");
			break;
		case RESTORE:
			bBuilder.setContentText("Restore in progress");
			break;
		}
		bBuilder.setContentIntent(pi);
		notificationManager.notify(NOTIFICATION_ID_RESERVED, bBuilder.build());
	}

	public void stopBackupNotification(Action action) {
		bBuilder = new NotificationCompat.Builder(context)
				.setContentTitle("Ceeq").setProgress(100, 100, false)
				.setOngoing(false);
		switch (action) {
		case BACKUP:
			bBuilder.setContentText("Backup complete.");
			break;
		case RESTORE:
			bBuilder.setContentText("Restore complete.");
			break;
		}
		notificationManager.notify(NOTIFICATION_ID_RESERVED, bBuilder.build());

	}

	public void hideNotification(int nIdentity) {
		notificationManager.cancel(nIdentity);
	}

	public String setApplicationStatus(Boolean type) {
		if (type)
			return "Protected";
		return "Vulnerable";
	}
}
