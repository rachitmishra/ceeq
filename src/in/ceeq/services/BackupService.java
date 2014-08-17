/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.services;

import hirondelle.date4j.DateTime;
import in.ceeq.commons.Utils;
import in.ceeq.home.backup.BackupFragment;
import in.ceeq.home.backup.CallManager;
import in.ceeq.home.backup.ContactManager;
import in.ceeq.home.backup.DictionaryManager;
import in.ceeq.home.backup.MessageManager;

import java.io.IOException;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParserException;

import com.github.johnpersano.supertoasts.SuperToast;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class BackupService extends IntentService {

	public static final String INTENT_ACTION_MESSAGE = "in.ceeq.action.MESSAGE";

	private CallManager callsManager;
	private ContactManager contactsManager;
	private MessageManager messagesManager;
	private DictionaryManager dictionaryManager;

	private static final String FILE_NAME_PREFIX_CONTACTS = "contact";
	private static final String FILE_NAME_PREFIX_MESSAGES = "message";
	private static final String FILE_NAME_PREFIX_CALLS = "call";
	private static final String FILE_NAME_PREFIX_WORDS = "dictionary";
	private static final String PREFS_L_MESSAGES = "lastMessageBackup";
	private static final String PREFS_L_CALLS = "lastCallsBackup";
	private static final String PREFS_L_CONTACTS = "lastContactsBackup";
	private static final String PREFS_L_WORDS = "lastDictionaryBackup";
	private static final String LAST_BACKUP_DATE = "lastBackupDate";

	public static final String ACTION_BACKUP = "in.ceeq.action.backup";
	public static final String ACTION_RESTORE = "in.ceeq.action.restore";
	public static final String ACTION_AUTO_BACKUP = "in.ceeq.action.backup.auto";
	public static final String ACTION_DATA = "action_data";
	public static final String ACTION_STATUS = "action_status";

	public final static int ACTION_DATA_ALL = 0;
	public final static int ACTION_DATA_CONTACTS = 1;
	public final static int ACTION_DATA_MESSAGES = 2;
	public final static int ACTION_DATA_CALLS = 3;
	public final static int ACTION_DATA_WORDS = 4;

	public static final boolean ZERO = false; // stop, finish, end
	public static final boolean ONE = true; // start

	private String action;
	private int actionData;
	private static LocalBroadcastManager localBroadcastManager;

	public BackupService() {
		super("Servicebackups");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		callsManager = new CallManager(this);
		contactsManager = new ContactManager(this);
		messagesManager = new MessageManager(this);
		dictionaryManager = new DictionaryManager(this);
		localBroadcastManager = LocalBroadcastManager.getInstance(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		action = intent.getAction();
		actionData = extras.getInt(ACTION_DATA);

		try {
			if (action.equals(ACTION_BACKUP)) {
				broadcastBackup(ONE);
				switch (actionData) {
				case ACTION_DATA_ALL:
					backup();
					break;
				case ACTION_DATA_CONTACTS:
					backupContacts();
					break;
				case ACTION_DATA_MESSAGES:
					backupMessages();
					break;
				case ACTION_DATA_CALLS:
					backupCallLogs();
					break;
				case ACTION_DATA_WORDS:
					backupDictionary();
					break;
				}
				broadcastBackup(ZERO);
			} else if (action.equals(ACTION_RESTORE)) {
				broadcastRestore(ONE);
				switch (actionData) {
				case ACTION_DATA_ALL:
					restore();
					break;
				case ACTION_DATA_CONTACTS:
					restoreContacts(Utils.getStringPrefs(this, PREFS_L_CONTACTS));
					break;
				case ACTION_DATA_MESSAGES:
					restoreMessages(Utils.getStringPrefs(this, PREFS_L_MESSAGES));
					break;
				case ACTION_DATA_CALLS:
					restoreCallLogs(Utils.getStringPrefs(this, PREFS_L_CALLS));
					break;
				case ACTION_DATA_WORDS:
					restoreDictionary(Utils.getStringPrefs(this, PREFS_L_WORDS));
					break;
				}
				broadcastRestore(ZERO);
			} else if (action.equals(ACTION_AUTO_BACKUP)) {
				backup();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void broadcastBackup(boolean status) {
		Intent intent = new Intent(BackupFragment.INTENT_ACTION_STATUS);
		if (status) {
			Utils.notification(Utils.NOTIFICATION_BACKUP_START, this, 0);
			intent.putExtra(ACTION_STATUS, ONE);
			SuperToast.create(this, "Backup started.", Toast.LENGTH_SHORT).show();
		} else {
			Utils.notification(Utils.NOTIFICATION_BACKUP_FINISH, this, 0);
			intent.putExtra(ACTION_STATUS, ZERO);
			SuperToast.create(this, "Backup completed.", Toast.LENGTH_SHORT).show();
		}
		localBroadcastManager.sendBroadcast(intent);
	}

	private void broadcastRestore(boolean status) {
		Intent intent = new Intent(BackupFragment.INTENT_ACTION_STATUS);
		if (status) {
			Utils.notification(Utils.NOTIFICATION_RESTORE_START, this, 0);
			intent.putExtra(ACTION_STATUS, ONE);
			SuperToast.create(this, "Restore started.", Toast.LENGTH_SHORT).show();
		} else {
			Utils.notification(Utils.NOTIFICATION_RESTORE_FINISH, this, 0);
			intent.putExtra(ACTION_STATUS, ZERO);
			SuperToast.create(this, "Restore completed.", Toast.LENGTH_SHORT).show();
		}
		localBroadcastManager.sendBroadcast(intent);
	}

	public void backup() {
		Utils.s("backup()");
		backupContacts();
		backupCallLogs();
		backupMessages();
		backupDictionary();
		Utils.c("backup()");
	}

	public void backupContacts() {
		Utils.s("backupContacts()");
		try {
			Utils.setStringPrefs(this, PREFS_L_CONTACTS,
					Utils.writeFile(contactsManager.read(), FILE_NAME_PREFIX_CONTACTS, this));
			Utils.setStringPrefs(this, LAST_BACKUP_DATE, DateTime.today(TimeZone.getDefault()).toString());
			Utils.c("backupContacts()");
		} catch (IOException e) {
			Utils.f("backupContacts()");
			e.printStackTrace();
		}
	}

	public void backupMessages() {
		Utils.s("backupMessages()");
		try {
			Utils.setStringPrefs(this, PREFS_L_MESSAGES,
					Utils.writeFile(messagesManager.read(), FILE_NAME_PREFIX_MESSAGES, this));
			Utils.setStringPrefs(this, LAST_BACKUP_DATE, DateTime.today(TimeZone.getDefault()).toString());
			Utils.c("backupMessages()");
		} catch (IOException e) {
			Utils.f("backupMessages()");
			e.printStackTrace();
		}
	}

	public void backupCallLogs() {
		Utils.s("backupCallLogs()");
		try {
			Utils.setStringPrefs(this, PREFS_L_CALLS,
					Utils.writeFile(callsManager.read(), FILE_NAME_PREFIX_CALLS, this));
			Utils.setStringPrefs(this, LAST_BACKUP_DATE, DateTime.today(TimeZone.getDefault()).toString());
			Utils.c("backupCallLogs()");
		} catch (IOException e) {
			Utils.f("backupCallLogs()");
			e.printStackTrace();
		}
	}

	public void backupDictionary() {
		Utils.s("backupDictionary()");
		try {
			Utils.setStringPrefs(this, PREFS_L_WORDS,
					Utils.writeFile(dictionaryManager.read(), FILE_NAME_PREFIX_WORDS, this));
			Utils.setStringPrefs(this, LAST_BACKUP_DATE, DateTime.today(TimeZone.getDefault()).toString());
			Utils.c("backupDictionary()");
		} catch (IOException e) {
			Utils.f("backupDictionary()");
			e.printStackTrace();
		}
	}

	public void restore() {
		restoreContacts(Utils.getStringPrefs(this, PREFS_L_CONTACTS));
		restoreMessages(Utils.getStringPrefs(this, PREFS_L_MESSAGES));
		restoreCallLogs(Utils.getStringPrefs(this, PREFS_L_CALLS));
		restoreDictionary(Utils.getStringPrefs(this, PREFS_L_WORDS));
	}

	public boolean restoreContacts(String fileName) {
		try {
			if (fileName.isEmpty())
				throw new NullPointerException();
			contactsManager.write(Utils.getParser(Utils.readFile(fileName, this)));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean restoreMessages(String fileName) {
		try {
			if (fileName.isEmpty())
				throw new NullPointerException();
			messagesManager.write(Utils.getParser(Utils.readFile(fileName, this)));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean restoreCallLogs(String fileName) {
		try {
			if (fileName.isEmpty())
				throw new NullPointerException();
			callsManager.write(Utils.getParser(Utils.readFile(fileName, this)));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean restoreDictionary(String fileName) {
		try {
			if (fileName.isEmpty())
				throw new NullPointerException();
			dictionaryManager.write(Utils.getParser(Utils.readFile(fileName, this)));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return false;
	}

}
