/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.services;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.List;

import in.ceeq.activities.Lockscreen;

public class Runner extends IntentService {

	public Runner() {
		super("ServiceLockScreen");
	}

	public boolean isFront = false;

	@Override
	protected void onHandleIntent(Intent intent) {
		switch (intent.getIntExtra("action", 2)) {
			case 1 :
				int secondsDelayed = 1;
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (!isFront) {
							ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

							List<ActivityManager.RunningTaskInfo> taskInfo = am
									.getRunningTasks(1);
							Log.w("lockscreen",
									taskInfo.get(0).topActivity.getClassName()
											+ "");
							if (!(taskInfo.get(0).topActivity.getClassName()
									.equals("LockScreen")))
								startActivity(new Intent(
										Runner.this,
										Lockscreen.class)
										.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
						}
					}
				}, secondsDelayed);
				break;
			case 2 :
				setFront(false);
		}
	}

	public void setFront(boolean front) {
		isFront = front;
	}
}
