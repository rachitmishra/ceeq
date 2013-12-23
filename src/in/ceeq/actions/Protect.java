package in.ceeq.actions;

import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.services.Protector;
import in.ceeq.services.Protector.ProtectorType;
import android.content.Context;
import android.content.Intent;

public class Protect {
	private Context context;
	private PreferencesHelper preferencesHelper;

	public Protect(Context context) {
		this.context = context;
		preferencesHelper = PreferencesHelper.getInstance(context);
	}

	public static Protect getInstance(Context context) {
		return new Protect(context);
	}

	public boolean isEnabled() {
		return (preferencesHelper
				.getBoolean(PreferencesHelper.PROTECT_ME_STATUS)) ? true
				: false;
	}

	public void enable() {
		Intent startProtector = new Intent(context, Protector.class);
		startProtector.putExtra(Protector.ACTION, ProtectorType.START);
		context.startService(startProtector);
	}

	public void disable() {
		if (preferencesHelper.getBoolean(PreferencesHelper.PROTECT_ME_STATUS)) {
			Intent stopProtector = new Intent(context, Protector.class);
			context.stopService(stopProtector);
		}
	}
}
