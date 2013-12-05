package in.ceeq.actions;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import in.ceeq.receivers.LowBattery;
import in.ceeq.receivers.OutgoingCalls;
import in.ceeq.receivers.PowerButton;

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
			case LOW_BATTERY :
				receiverComponent = new ComponentName(context, LowBattery.class);
				break;
			case OUTGOING_CALLS :
				receiverComponent = new ComponentName(context,
						OutgoingCalls.class);
				break;
			case POWER_BUTTON :
				receiverComponent = new ComponentName(context,
						PowerButton.class);
				break;
			default :
				break;

		}
		try {
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
			case LOW_BATTERY :
				receiverComponent = new ComponentName(context, LowBattery.class);
				break;
			case OUTGOING_CALLS :
				receiverComponent = new ComponentName(context,
						OutgoingCalls.class);
				break;
			case POWER_BUTTON :
				receiverComponent = new ComponentName(context,
						PowerButton.class);
				break;
			default :
				break;

		}
		try {
			packageManager.setComponentEnabledSetting(receiverComponent,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
