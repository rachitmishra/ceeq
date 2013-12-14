package in.ceeq.actions;

import in.ceeq.Launcher;
import in.ceeq.helpers.NotificationsHelper;
import in.ceeq.helpers.PreferencesHelper;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class Stealth {
	private Context context;
	private PreferencesHelper preferencesHelper;
	private NotificationsHelper notificationsHelper;
	private static final boolean ENABLE = true;
	private static final boolean DISABLE = false;

	public Stealth(Context context) {
		this.context = context;
		preferencesHelper = PreferencesHelper.getInstance(context);
		notificationsHelper = NotificationsHelper.getInstance(context);
	}

	public static Stealth getInstance(Context context) {
		return new Stealth(context);
	}

	public boolean isEnabled() {
		return (preferencesHelper
				.getBoolean(PreferencesHelper.STEALTH_MODE_STATUS)) ? true
				: false;
	}

	public void enable() {
		preferencesHelper.setBoolean(PreferencesHelper.STEALTH_MODE_STATUS,
				ENABLE);

		context.getPackageManager().setComponentEnabledSetting(
				new ComponentName("in.ceeq", "in.ceeq.Launcher"),
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
		preferencesHelper.setBoolean(PreferencesHelper.NOTIFICATIONS_STATUS,
				DISABLE);
		notificationsHelper
				.hideNotification(NotificationsHelper.DEFAULT_NOTIFICATION_ID);
		try {
			context.startActivity(new Intent(context, Launcher.class));
		} catch (Exception e) {
		}

	}

	public void disable() {
		context.getPackageManager().setComponentEnabledSetting(
				new ComponentName("in.ceeq", "in.ceeq.Launcher"),
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
		preferencesHelper.setBoolean(PreferencesHelper.STEALTH_MODE_STATUS,
				DISABLE);
		preferencesHelper.setBoolean(PreferencesHelper.NOTIFICATIONS_STATUS,
				ENABLE);
		notificationsHelper.showPersistentNotification();
	}
}
