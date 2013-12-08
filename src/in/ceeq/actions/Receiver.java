package in.ceeq.actions;

import in.ceeq.receivers.LowBattery;
import in.ceeq.receivers.OutgoingCalls;
import in.ceeq.receivers.PowerButton;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

public class Receiver {

	private Context context;
	PackageManager packageManager;

	public enum ReceiverType {
		LOW_BATTERY, OUTGOING_CALLS, POWER_BUTTON
	}

	public Receiver(Context context) {
		this.context = context;
		packageManager = context.getPackageManager();
	}

	public static Receiver getInstance(Context context) {
		return new Receiver(context);
	}

	public void register(ReceiverType receiver) {
		ComponentName receiverComponent = null;
		switch (receiver) {
		case LOW_BATTERY:
			receiverComponent = new ComponentName(context, LowBattery.class);
			break;
		case OUTGOING_CALLS:
			receiverComponent = new ComponentName(context, OutgoingCalls.class);
			break;
		case POWER_BUTTON:
			BroadcastReceiver power = new PowerButton();
			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			context.registerReceiver(power, filter);
			break;
		default:
			break;

		}
		try {
			if (receiverComponent != null)
				packageManager.setComponentEnabledSetting(receiverComponent,
						PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
						PackageManager.DONT_KILL_APP);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void unregister(ReceiverType receiver) {
		ComponentName receiverComponent = null;
		switch (receiver) {
		case LOW_BATTERY:
			receiverComponent = new ComponentName(context, LowBattery.class);
			break;
		case OUTGOING_CALLS:
			receiverComponent = new ComponentName(context, OutgoingCalls.class);
			break;
		case POWER_BUTTON:
			BroadcastReceiver power = new PowerButton();
			try {
				context.unregisterReceiver(power);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default:
			break;

		}
		try {
			if (receiverComponent != null)
				packageManager.setComponentEnabledSetting(receiverComponent,
						PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
