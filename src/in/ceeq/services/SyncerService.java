package in.ceeq.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncerService extends Service {
	public SyncerService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
