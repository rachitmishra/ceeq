/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.services;

import in.ceeq.R;
import in.ceeq.commons.Utils;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

public class RingerService extends IntentService {

	public static final String RINGER_START_ACTION = "in.ceeq.ringer.start";
	public static final String SIREN_START_ACTION = "in.ceeq.siren.start";
	public static final String STOP_ACTION = "in.ceeq.ringer.stop";

	private MediaPlayer mediaPlayer;
	private AudioManager audioManager;
	private String action;

	public RingerService() {
		super("Ringer");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		audioManager.setStreamVolume(AudioManager.STREAM_RING,
				audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Uri uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
		action = intent.getAction();

		if (action.equals(RINGER_START_ACTION)) {
			mediaPlayer = MediaPlayer.create(this, uri);
			startPlaying();
		}

		if (action.equals(RINGER_START_ACTION)) {
			mediaPlayer = MediaPlayer.create(this, R.raw.siren);
			startPlaying();
		}
		
		if (action.equals(STOP_ACTION)) {
			stopPlaying();
		}

	}

	private void startPlaying() {
		if (mediaPlayer.isPlaying() || mediaPlayer.isLooping()) {
			Utils.d("Already playing ...");
			mediaPlayer.stop();
			while (true)
				mediaPlayer.start();
		} else {
			while (true)
				mediaPlayer.start();
		}
	}

	private void stopPlaying() {
		mediaPlayer.stop();
	}
}
