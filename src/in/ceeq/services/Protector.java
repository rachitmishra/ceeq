package in.ceeq.services;

import in.ceeq.actions.Receiver;
import in.ceeq.actions.Receiver.ReceiverType;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class Protector extends Service {
	public Protector() {

	}

	@Override
	public void onCreate() {
		Receiver.getInstance(this).register(ReceiverType.POWER_BUTTON);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
