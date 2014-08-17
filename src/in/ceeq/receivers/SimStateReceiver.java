package in.ceeq.receivers;

import in.ceeq.commons.Utils;
import in.ceeq.services.CommandService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class SimStateReceiver extends BroadcastReceiver {

	public SimStateReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		checkSimChange(context);
	}

	public void checkSimChange(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		try {
			if (!tm.getSimSerialNumber().equals(Utils.getStringPrefs(context, Utils.SIM_NUMBER))) {

				try {
					Intent commands = new Intent(context, CommandService.class);
					commands.putExtra(CommandService.ACTION, CommandService.SEND_SIM_CHANGE_MESSAGE);
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
