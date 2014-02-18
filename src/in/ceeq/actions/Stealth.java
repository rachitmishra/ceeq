package in.ceeq.actions;

import in.ceeq.Launcher;
import in.ceeq.helpers.PreferencesHelper;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class Stealth {
	private Context context;
	private PreferencesHelper preferencesHelper;
	private Notifications notifications;

	public Stealth(Context context) {
		this.context = context;
		preferencesHelper = PreferencesHelper.getInstance(context);
		notifications = Notifications.getInstance(context);
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
				true);

		context.getPackageManager().setComponentEnabledSetting(
				new ComponentName("in.ceeq", "in.ceeq.Launcher"),
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
		preferencesHelper.setBoolean(PreferencesHelper.NOTIFICATIONS_STATUS,
				false);
		notifications.remove();
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
				false);
		preferencesHelper.setBoolean(PreferencesHelper.NOTIFICATIONS_STATUS,
				true);
		notifications.show();
	}
}
