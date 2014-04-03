/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.services;

import in.ceeq.R;
import in.ceeq.helpers.Logger;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

public class Ringer extends IntentService {

	public static final String ACTION = "action";
	public static final String ACTION_TYPE = "actionType";

	public static final int OFF = 0;
	public static final int ON = 1;

	public static final int RING = 1;
	public static final int SIREN = 2;

	private MediaPlayer player;
	private AudioManager audioManager;
	private int soundState;
	private int soundType;

	public Ringer() {
		super("Ringer");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Uri uri = RingtoneManager.getActualDefaultRingtoneUri(this,
				RingtoneManager.TYPE_RINGTONE);
		soundType = intent.getExtras().getInt(ACTION_TYPE, SIREN);
		Logger.d("Sound type ..."+soundType);
		switch (soundType) {
		case RING:
			player = MediaPlayer.create(this, uri);
			break;
		case SIREN:
			player = MediaPlayer.create(this, R.raw.siren);
			break;
		}

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		audioManager.setStreamVolume(AudioManager.STREAM_RING,
				audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
		soundState = intent.getExtras().getInt(ACTION, OFF);

		switch (soundState) {
		case ON:
			if (player.isPlaying() || player.isLooping()) {
				Logger.d("Already playing ...");
				player.stop();
				while (true)
					player.start();
			} else {
				while (true)
					player.start();
			}
		case OFF:
			player.stop();
			break;
		}
	}
}
