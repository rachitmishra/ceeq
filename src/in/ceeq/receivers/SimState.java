package in.ceeq.receivers;

import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.services.Commander;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class SimState extends BroadcastReceiver {

	private PreferencesHelper preferencesHelper;

	public SimState() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		preferencesHelper = new PreferencesHelper(context);
		checkSimChange(context);
	}

	public void checkSimChange(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		try {
			if (!tm.getSimSerialNumber().equals(
					preferencesHelper.getString("simNumber"))) {

				try {
					Intent commands = new Intent(context, Commander.class);
					commands.putExtra(Commander.ACTION,
							Commander.SEND_SIM_CHANGE_MESSAGE);
					context.startService(commands);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
