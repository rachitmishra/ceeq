/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.services;

import hirondelle.date4j.DateTime;
import in.ceeq.actions.Backup.State;
import in.ceeq.actions.Notifications;
import in.ceeq.activities.Home;
import in.ceeq.helpers.FilesHelper;
import in.ceeq.helpers.Logger;
import in.ceeq.helpers.PreferencesHelper;
import in.ceeq.helpers.XmlDataParseHelper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.UserDictionary;
import android.util.Log;
import android.util.Xml;

public class Backups extends IntentService {

	private Calls_ callsManager;
	private Contacts_ contactsManager;
	private Messages_ messagesManager;
	private Dictionary_ dictionaryManager;
	private FilesHelper filesHelper;
	private PreferencesHelper preferencesHelper;

	private static final String FILE_NAME_PREFIX_CONTACTS = "contact";
	private static final String FILE_NAME_PREFIX_MESSAGES = "message";
	private static final String FILE_NAME_PREFIX_CALLS = "call";
	private static final String FILE_NAME_PREFIX_WORDS = "dictionary";
	private static final String NULL = null;
	private static final String PREFS_L_MESSAGES = "lastMessageBackup";
	private static final String PREFS_L_CALLS = "lastCallsBackup";
	private static final String PREFS_L_CONTACTS = "lastContactsBackup";
	private static final String PREFS_L_WORDS = "lastDictionaryBackup";
	private static final String LAST_BACKUP_DATE = "lastBackupDate";

	public static final String ACTION = "action";
	public static final String ACTION_TYPE = "actionType";
	public static final String ACTION_PARENT = "actionParent";
	
	public final static int ACTION_TYPE_ALL = 0;
	public final static int ACTION_TYPE_CONTACTS = 1;
	public final static int ACTION_TYPE_MESSAGES = 2;
	public final static int ACTION_TYPE_CALLS = 3;
	public final static int ACTION_TYPE_WORDS = 4;

	public static final int ACTION_BACKUP = 1;
	public static final int ACTION_RESTORE = 2;

	public static final int SHOW = 0;
	public static final int HIDE = 1;

	public static int ACTION_PARENT_ACTIVITY = 1;
	public static int ACTION_PARENT_SERVICE = 2;

	private int action;
	private int actionType;
	private int actionParent;
	private ContentResolver resolver;
	private XmlSerializer serializer;
	private StringWriter writer;
	private Messenger messageHandler;

	public Backups() {
		super("Servicebackups");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		setupHelpers();
	}

	public void setupHelpers() {
		callsManager = new Calls_();
		contactsManager = new Contacts_();
		messagesManager = new Messages_();
		dictionaryManager = new Dictionary_();
		filesHelper = new FilesHelper(this);
		preferencesHelper = new PreferencesHelper(this);
		resolver = getContentResolver();
		serializer = Xml.newSerializer();
		writer = new StringWriter();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		action = extras.getInt(ACTION);
		actionType = extras.getInt(ACTION_TYPE);
		actionParent = extras.getInt(ACTION_PARENT);
		
		if (actionParent == ACTION_PARENT_ACTIVITY) {
			messageHandler = (Messenger) extras.get(Home.MESSENGER);
			sendMessage(Home.SHOW);
		}

		notify(State.ON);

		try {
			switch (action) {
			case BACKUP:
				switch (actionType) {
				case ACTION_TYPE_ALL:
					backup();
					break;
				case ACTION_TYPE_CONTACTS:
					backupContacts();
					break;
				case ACTION_TYPE_MESSAGES:
					backupMessages();
					break;
				case ACTION_TYPE_CALLS:
					backupCallLogs();
					break;
				case ACTION_TYPE_WORDS:
					backupDictionary();
					break;
				}
				break;
			case RESTORE:
				switch (actionType) {

				case ACTION_TYPE_ALL:
					restore();
					break;
				case ACTION_TYPE_CONTACTS:
					restoreContacts(preferencesHelper
							.getString(PREFS_L_CONTACTS));
					break;
				case ACTION_TYPE_MESSAGES:
					restoreMessages(preferencesHelper
							.getString(PREFS_L_MESSAGES));
					break;
				case ACTION_TYPE_CALLS:
					restoreCallLogs(preferencesHelper.getString(PREFS_L_CALLS));
					break;
				case ACTION_TYPE_WORDS:
					restoreDictionary(preferencesHelper
							.getString(PREFS_L_WORDS));
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			notify(State.OFF);
			if (actionParent == ACTION_PARENT_ACTIVITY) {
				sendMessage(Home.HIDE);
			}
		}
	}

	public void notify(State state) {
		if (preferencesHelper
				.getBoolean(PreferencesHelper.NOTIFICATIONS_STATUS)) {
			switch (state) {
			case OFF:
				Notifications.getInstance(this).finish(action);
				break;
			case ON:
				Notifications.getInstance(this).start(action);
				break;
			}
		}
	}

	public void sendMessage(int state) {
		Message message = Message.obtain();
		switch (state) {
		case Home.SHOW:
			message.arg1 = Home.SHOW;
			break;
		case Home.HIDE:
			message.arg1 = Home.HIDE;
			break;
		}
		try {
			messageHandler.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void backup() {
		Logger.s("backup()");
		backupContacts();
		backupCallLogs();
		backupMessages();
		backupDictionary();
		Logger.c("backup()");
	}

	public void backupContacts() {
		Logger.s("backupContacts()");
		try {
			preferencesHelper.setString(PREFS_L_CONTACTS, filesHelper
					.writeFile(contactsManager.readContacts(),
							FILE_NAME_PREFIX_CONTACTS));
			preferencesHelper.setString(LAST_BACKUP_DATE,
					DateTime.today(TimeZone.getDefault()).toString());
			Logger.c("backupContacts()");
		} catch (IOException e) {
			Logger.f("backupContacts()");
			e.printStackTrace();
		}
	}

	public void backupMessages() {
		Logger.s("backupMessages()");
		try {
			preferencesHelper.setString(PREFS_L_MESSAGES, filesHelper
					.writeFile(messagesManager.readMessages(),
							FILE_NAME_PREFIX_MESSAGES));
			preferencesHelper.setString(LAST_BACKUP_DATE,
					DateTime.today(TimeZone.getDefault()).toString());
			Logger.c("backupMessages()");
		} catch (IOException e) {
			Logger.f("backupMessages()");
			e.printStackTrace();
		}
	}

	public void backupCallLogs() {
		Logger.s("backupCallLogs()");
		try {
			preferencesHelper.setString(PREFS_L_CALLS, filesHelper.writeFile(
					callsManager.readCalls(), FILE_NAME_PREFIX_CALLS));
			preferencesHelper.setString(LAST_BACKUP_DATE,
					DateTime.today(TimeZone.getDefault()).toString());
			Logger.c("backupCallLogs()");
		} catch (IOException e) {
			Logger.f("backupCallLogs()");
			e.printStackTrace();
		}
	}

	public void backupDictionary() {
		Logger.s("backupDictionary()");
		try {
			preferencesHelper
					.setString(PREFS_L_WORDS, filesHelper.writeFile(
							dictionaryManager.readDictionary(),
							FILE_NAME_PREFIX_WORDS));
			preferencesHelper.setString(LAST_BACKUP_DATE,
					DateTime.today(TimeZone.getDefault()).toString());
			Logger.c("backupDictionary()");
		} catch (IOException e) {
			Logger.f("backupDictionary()");
			e.printStackTrace();
		}
	}

	public void restore() {
		restoreContacts(preferencesHelper.getString(PREFS_L_CONTACTS));
		restoreMessages(preferencesHelper.getString(PREFS_L_MESSAGES));
		restoreCallLogs(preferencesHelper.getString(PREFS_L_CALLS));
		restoreDictionary(preferencesHelper.getString(PREFS_L_WORDS));
	}

	public boolean restoreContacts(String fileName) {
		try {
			if (fileName.isEmpty())
				throw new NullPointerException();
			contactsManager.writeContacts(new XmlDataParseHelper(filesHelper
					.readFile(fileName)).getParser());
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
			messagesManager.writeMessages(new XmlDataParseHelper(filesHelper
					.readFile(fileName)).getParser());
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
			callsManager.writeCalls(new XmlDataParseHelper(filesHelper
					.readFile(fileName)).getParser());
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
			dictionaryManager.writeDictionary(new XmlDataParseHelper(
					filesHelper.readFile(fileName)).getParser());
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

	public class Calls_ {

		public static final String NUMBER = CallLog.Calls.NUMBER;
		public static final String TYPE = CallLog.Calls.TYPE;
		public static final String DATE = CallLog.Calls.DATE;
		public static final String DURATION = CallLog.Calls.DURATION;
		public static final String CALL_TAG = "call";
		public static final String CALLLOGS_TAG = "calllogs";
		public static final String DURATION_TAG = "duration";
		public static final String DATE_TAG = "date";
		public static final String TYPE_TAG = "type";
		public static final String NUMBER_TAG = "number";
		public static final String TOTAL_CALLS_TAG = "totalcalls";
		private final Uri URI = CallLog.Calls.CONTENT_URI;

		public String readCalls() {

			Cursor cs = resolver.query(URI, null, null, null, null);
			try {

				if (cs.moveToFirst()) {
					serializer.setOutput(writer);
					serializer.startDocument("UTF-8", true);
					serializer.startTag("", CALLLOGS_TAG);
					serializer.startTag("", TOTAL_CALLS_TAG);
					serializer.text(cs.getCount() + "");
					serializer.endTag("", TOTAL_CALLS_TAG);
					do {
						serializer.startTag("", CALL_TAG);
						serializer.startTag("", NUMBER_TAG);
						serializer
								.text(cs.getString(cs.getColumnIndex(NUMBER)));
						serializer.endTag("", NUMBER_TAG);
						serializer.startTag("", TYPE_TAG);
						serializer.text(cs.getString(cs.getColumnIndex(TYPE)));
						serializer.endTag("", TYPE_TAG);
						serializer.startTag("", DATE_TAG);
						serializer.text(cs.getString(cs.getColumnIndex(DATE)));
						serializer.endTag("", DATE_TAG);
						serializer.startTag("", DURATION_TAG);
						serializer.text(cs.getString(cs
								.getColumnIndex(DURATION_TAG)));
						serializer.endTag("", DURATION);
						serializer.endTag("", CALL_TAG);
					} while (cs.moveToNext());
					serializer.endTag("", CALLLOGS_TAG);
					serializer.endDocument();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				cs.close();
			}
			return writer.toString();
		}

		public void writeCalls(XmlPullParser parser)
				throws XmlPullParserException, IOException {
			ContentValues call = null;
			int totalCalls = 0, eventType = parser.getEventType();
			String tagName = "";
			Log.w("Developer", "Restoring call logs ...");
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					Log.w("Developer", "Reading backup file...");
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (tagName.equals(CALL_TAG)) {
						call = new ContentValues();
					} else if (tagName.equals(TOTAL_CALLS_TAG)) {
						totalCalls = Integer.parseInt(XmlDataParseHelper
								.readTag(parser, TOTAL_CALLS_TAG));
					} else if (tagName.equals(NUMBER_TAG)) {
						call.put(NUMBER,
								XmlDataParseHelper.readTag(parser, NUMBER_TAG));
					} else if (tagName.equals(TYPE_TAG)) {
						call.put(TYPE,
								XmlDataParseHelper.readTag(parser, TYPE_TAG));
					} else if (tagName.equals(DATE_TAG)) {
						call.put(DATE,
								XmlDataParseHelper.readTag(parser, DATE_TAG));
					} else if (tagName.equals(DURATION_TAG)) {
						call.put(DURATION, XmlDataParseHelper.readTag(parser,
								DURATION_TAG));
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equals(CALL_TAG)) {
						try {
							resolver.insert(URI, call);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					break;
				}
				eventType = parser.next();
			}
			Log.w("Developer", "File reading complete...");
			Log.w("Developer", "Call logs restore successfull...");
			Log.w("Developer", "Total " + totalCalls + " calls restored.");
		}

	}

	public class Contacts_ {
		private static final String COUNTRY_TAG = "country",
				TOTAL_CONTACTS_TAG = "totalcontacts",
				POSTCODE_TAG = "postcode", STATE_TAG = "state",
				CITY_TAG = "city", STREET_TAG = "street",
				ADDRESS_TAG = "address", EMAIL_TAG = "email",
				EMAILS_TAG = "emails", TYPE_TAG = "type",
				NUMBER_TAG = "number", NUMBERS_TAG = "numbers",
				NAME_TAG = "name", CONTACT_TAG = "contact",
				CONTACTS_TAG = "contacts";

		private final Uri URI = ContactsContract.Contacts.CONTENT_URI,
				PURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				EURI = ContactsContract.CommonDataKinds.Email.CONTENT_URI,
				AURI = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI;

		private static final String ID = ContactsContract.Contacts._ID,
				DNAME = ContactsContract.Contacts.DISPLAY_NAME,
				HPN = ContactsContract.Contacts.HAS_PHONE_NUMBER,
				CID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				EID = ContactsContract.CommonDataKinds.Email.CONTACT_ID,
				AID = ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID,
				PNUM = ContactsContract.CommonDataKinds.Phone.NUMBER,
				PTYPE = ContactsContract.CommonDataKinds.Phone.TYPE,
				EMAIL = ContactsContract.CommonDataKinds.Email.DATA,
				ETYPE = ContactsContract.CommonDataKinds.Email.TYPE,
				STREET = ContactsContract.CommonDataKinds.StructuredPostal.STREET,
				CITY = ContactsContract.CommonDataKinds.StructuredPostal.CITY,
				STATE = ContactsContract.CommonDataKinds.StructuredPostal.REGION,
				POSTCODE = ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
				COUNTRY = ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
				NCT = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
				DNM = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
				PCT = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
				ECT = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;

		private String id;

		public String readContacts() {
			Cursor pcs = null, ecs = null, acs = null, cs = resolver.query(URI,
					null, null, null, null);

			try {
				if (cs.moveToFirst()) {
					serializer.setOutput(writer);
					serializer.startDocument("UTF-8", true);
					serializer.startTag("", CONTACTS_TAG);
					serializer.startTag("", TOTAL_CONTACTS_TAG);
					serializer.text(cs.getCount() + "");
					serializer.endTag("", TOTAL_CONTACTS_TAG);
					while (cs.moveToNext()) {

						serializer.startTag("", CONTACT_TAG);
						id = cs.getString(cs.getColumnIndex(ID));

						serializer.startTag("", NAME_TAG);
						String name = cs.getString(cs.getColumnIndex(DNAME));
						if (name == null)
							name = "No name";
						serializer.text(name);
						serializer.endTag("", NAME_TAG);

						if (Integer.parseInt(cs.getString(cs
								.getColumnIndex(HPN))) > 0) {
							try {
								pcs = resolver.query(PURI, null, CID + " = ?",
										new String[] { id }, null);
								serializer.startTag("", NUMBERS_TAG);
								while (pcs.moveToNext()) {
									serializer.startTag("", NUMBER_TAG);
									serializer.attribute("", TYPE_TAG, pcs
											.getString(pcs
													.getColumnIndex(PTYPE)));
									serializer.text(pcs.getString(pcs
											.getColumnIndex(PNUM)));
									serializer.endTag("", NUMBER_TAG);
								}
								serializer.endTag("", NUMBERS_TAG);
							} finally {
								pcs.close();
							}
						}

						try {
							ecs = resolver.query(EURI, null, EID + " = ?",
									new String[] { id }, null);
							if (ecs.moveToFirst()) {
								serializer.startTag("", EMAILS_TAG);
								do {
									serializer.startTag("", EMAIL_TAG);
									serializer.attribute("", TYPE_TAG, ecs
											.getString(ecs
													.getColumnIndex(ETYPE)));
									serializer.text(ecs.getString(ecs
											.getColumnIndex(EMAIL)));
									serializer.endTag("", EMAIL_TAG);
								} while (ecs.moveToNext());
								serializer.endTag("", EMAILS_TAG);
							}
						} finally {
							ecs.close();
						}

						try {
							acs = resolver.query(AURI, null, AID + " = ?",
									new String[] { id }, null);
							if (acs.moveToFirst()) {
								serializer.startTag("", ADDRESS_TAG);
								do {
									serializer.startTag("", STREET_TAG);
									serializer.text(acs.getString(acs
											.getColumnIndex(STREET)));
									serializer.endTag("", STREET_TAG);
									serializer.startTag("", CITY_TAG);
									serializer.text(acs.getString(acs
											.getColumnIndex(CITY)));
									serializer.endTag("", CITY_TAG);
									serializer.startTag("", STATE_TAG);
									serializer.text(acs.getString(acs
											.getColumnIndex(STATE)));
									serializer.endTag("", STATE_TAG);
									serializer.startTag("", POSTCODE_TAG);
									serializer.text(acs.getString(acs
											.getColumnIndex(POSTCODE)));
									serializer.endTag("", POSTCODE_TAG);
									serializer.startTag("", COUNTRY_TAG);
									serializer.text(acs.getString(acs
											.getColumnIndex(COUNTRY)));
									serializer.endTag("", COUNTRY_TAG);
								} while (acs.moveToNext());
								serializer.endTag("", ADDRESS_TAG);
							}
						} finally {
							acs.close();
						}
						serializer.endTag("", CONTACT_TAG);
					}
					serializer.endTag("", CONTACTS_TAG);
					serializer.endDocument();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				cs.close();
			}
			return writer.toString();
		}

		public void writeContacts(XmlPullParser parser)
				throws XmlPullParserException, IOException {
			ArrayList<ContentProviderOperation> contact = null;
			int totalContacts = 0, eventType = parser.getEventType();
			String tagName = "";
			Log.w("Developer", "Restoring contacts...");
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					Log.w("Developer", "Reading backup file...");
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (tagName.equals(TOTAL_CONTACTS_TAG)) {
						totalContacts = Integer.parseInt(XmlDataParseHelper
								.readTag(parser, TOTAL_CONTACTS_TAG));
						Log.w("Developer", "File contains " + totalContacts
								+ " contacts...");
					}
					if (tagName.equals(CONTACT_TAG)) {
						contact = new ArrayList<ContentProviderOperation>();
						contact.add(ContentProviderOperation
								.newInsert(RawContacts.CONTENT_URI)
								.withValue(RawContacts.ACCOUNT_TYPE, NULL)
								.withValue(RawContacts.ACCOUNT_NAME, NULL)
								.build());
					} else if (tagName.equals(NAME_TAG)) {
						String name = XmlDataParseHelper.readTag(parser,
								NAME_TAG);
						contact.add(ContentProviderOperation
								.newInsert(ContactsContract.Data.CONTENT_URI)
								.withValueBackReference(Data.RAW_CONTACT_ID, 0)
								.withValue(Data.MIMETYPE, NCT)
								.withValue(DNM, name).build());
					} else if (tagName.equals(NUMBER_TAG)) {
						String type = parser.getAttributeValue(NULL, TYPE_TAG);
						String number = XmlDataParseHelper.readTag(parser,
								NUMBER_TAG);
						contact.add(ContentProviderOperation
								.newInsert(ContactsContract.Data.CONTENT_URI)
								.withValueBackReference(
										ContactsContract.Data.RAW_CONTACT_ID, 0)
								.withValue(ContactsContract.Data.MIMETYPE, PCT)
								.withValue(PNUM, number).withValue(PTYPE, type)
								.build());
					} else if (tagName.equals(EMAIL_TAG)) {
						String type = parser.getAttributeValue(NULL, TYPE_TAG);
						String email = XmlDataParseHelper.readTag(parser,
								EMAIL_TAG);
						contact.add(ContentProviderOperation
								.newInsert(ContactsContract.Data.CONTENT_URI)
								.withValueBackReference(
										ContactsContract.Data.RAW_CONTACT_ID, 0)
								.withValue(ContactsContract.Data.MIMETYPE, ECT)
								.withValue(EMAIL, email).withValue(ETYPE, type)
								.build());
					} else if (tagName.equals(ADDRESS_TAG)) {

					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equals(CONTACT_TAG)) {
						try {
							getContentResolver().applyBatch(
									ContactsContract.AUTHORITY, contact);
						} catch (RemoteException e) {
							e.printStackTrace();
						} catch (OperationApplicationException e) {
							e.printStackTrace();
						}
					}
					break;
				}
				eventType = parser.next();
			}

			Log.w("Developer", "File reading complete...");
			Log.w("Developer", "Contact restore successfull...");
			Log.w("Developer", "Total " + totalContacts + " contacts restored.");
		}
	}

	private class Messages_ {

		private static final String READ_TAG = "read", BODY_TAG = "body",
				NUMBER_TAG = "number", MESSAGE_TAG = "message",
				MESSAGES_TAG = "messages", NUM_TAG = "address",
				TYPE_TAG = "type", DATE_TAG = "date",
				TOTAL_MESSAGES_TAG = "totalmessages";

		private final Uri URI = Uri.parse("content://mms-sms/conversations"),
				MURI = Uri.parse("content://sms");

		public String readMessages() {
			Cursor cs = resolver.query(MURI, new String[] { "*" }, null, null,
					null);
			try {
				if (cs.moveToFirst()) {
					serializer.setOutput(writer);
					serializer.startDocument("UTF-8", true);
					serializer.startTag("", MESSAGES_TAG);
					serializer.startTag("", TOTAL_MESSAGES_TAG);
					serializer.text(cs.getCount() + "");
					serializer.endTag("", TOTAL_MESSAGES_TAG);
					while (cs.moveToNext()) {
						serializer.startTag("", MESSAGE_TAG);
						serializer.startTag("", NUMBER_TAG);
						serializer
								.text(cs.getString(cs.getColumnIndex(NUM_TAG)));
						serializer.endTag("", NUMBER_TAG);
						serializer.startTag("", BODY_TAG);
						serializer.text(cs.getString(cs
								.getColumnIndex(BODY_TAG)));
						serializer.endTag("", BODY_TAG);
						serializer.startTag("", DATE_TAG);
						serializer.text(cs.getString(cs
								.getColumnIndex(DATE_TAG)));
						serializer.endTag("", DATE_TAG);
						serializer.startTag("", TYPE_TAG);
						serializer.text(cs.getString(cs
								.getColumnIndex(TYPE_TAG)));
						serializer.endTag("", TYPE_TAG);
						serializer.startTag("", READ_TAG);
						serializer.text(cs.getString(cs
								.getColumnIndex(READ_TAG)));
						serializer.endTag("", READ_TAG);
						serializer.endTag("", MESSAGE_TAG);
					}
					serializer.endTag("", MESSAGES_TAG);
					serializer.endDocument();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				cs.close();
			}
			return writer.toString();
		}

		public void writeMessages(XmlPullParser parser)
				throws XmlPullParserException, IOException {
			ContentValues message = null;
			int totalMessages = 0, eventType = parser.getEventType();
			String tagName = "";
			Log.w("Developer", "Restoring message ...");
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					Log.w("Developer", "Reading backup file...");
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (tagName.equals(MESSAGE_TAG)) {
						message = new ContentValues();
					} else if (tagName.equals(TOTAL_MESSAGES_TAG)) {
						totalMessages = Integer.parseInt(XmlDataParseHelper
								.readTag(parser, TOTAL_MESSAGES_TAG));
					} else if (tagName.equals(NUMBER_TAG)) {
						message.put(NUMBER_TAG,
								XmlDataParseHelper.readTag(parser, NUMBER_TAG));
					} else if (tagName.equals(BODY_TAG)) {
						message.put(BODY_TAG,
								XmlDataParseHelper.readTag(parser, BODY_TAG));
					} else if (tagName.equals(DATE_TAG)) {
						message.put(DATE_TAG,
								XmlDataParseHelper.readTag(parser, DATE_TAG));
					} else if (tagName.equals(TYPE_TAG)) {
						message.put(TYPE_TAG,
								XmlDataParseHelper.readTag(parser, TYPE_TAG));
					} else if (tagName.equals(READ_TAG)) {
						message.put(READ_TAG,
								XmlDataParseHelper.readTag(parser, READ_TAG));
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equals(MESSAGE_TAG)) {
						try {
							resolver.insert(URI, message);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					break;
				}
				eventType = parser.next();
			}
			Log.w("Developer", "File reading complete...");
			Log.w("Developer", "Message restore successfull...");
			Log.w("Developer", "Total " + totalMessages + " messages restored.");
		}
	}

	private class Dictionary_ {

		private static final String APP_ID = UserDictionary.Words.APP_ID,
				FREQUENCY = UserDictionary.Words.FREQUENCY,
				WORD = UserDictionary.Words.WORD,
				LOCALE = UserDictionary.Words.LOCALE, LOCALE_TAG = "locale",
				FREQUENCY_TAG = "frequency", APPID_TAG = "appid",
				WORD_TEXT_TAG = "word", WORD_TAG = "words",
				DICTIONARY_TAG = "dictionary", TOTAL_WORDS_TAG = "total";
		private final Uri URI = UserDictionary.Words.CONTENT_URI;

		private String[] mProjection = { UserDictionary.Words.WORD,
				UserDictionary.Words.LOCALE, UserDictionary.Words.FREQUENCY,
				UserDictionary.Words.APP_ID };

		public String readDictionary() {
			Cursor cs = resolver.query(URI, mProjection, null, null, null);
			try {
				if (cs.moveToFirst()) {

					serializer.setOutput(writer);
					serializer.startDocument("UTF-8", true);
					serializer.startTag("", DICTIONARY_TAG);
					serializer.startTag("", TOTAL_WORDS_TAG);
					serializer.text(cs.getCount() + "");
					serializer.endTag("", TOTAL_WORDS_TAG);

					while (cs.moveToNext()) {
						serializer.startTag("", WORD_TAG);
						serializer.startTag("", APPID_TAG);
						serializer
								.text(cs.getString(cs.getColumnIndex(APP_ID)));
						serializer.endTag("", APPID_TAG);
						serializer.startTag("", WORD_TEXT_TAG);
						serializer.text(cs.getString(cs.getColumnIndex(WORD)));
						serializer.endTag("", WORD_TEXT_TAG);
						serializer.startTag("", FREQUENCY_TAG);
						serializer.text(cs.getString(cs
								.getColumnIndex(FREQUENCY)));
						serializer.endTag("", FREQUENCY_TAG);
						serializer.startTag("", LOCALE_TAG);
						serializer
								.text(cs.getString(cs.getColumnIndex(LOCALE)));
						serializer.endTag("", LOCALE_TAG);
						serializer.endTag("", WORD_TAG);
					}

					serializer.endTag("", DICTIONARY_TAG);
					serializer.endDocument();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				cs.close();
			}

			return writer.toString();
		}

		public void writeDictionary(XmlPullParser parser)
				throws XmlPullParserException, IOException {

			ContentValues word = null;
			int totalWords = 0, eventType = parser.getEventType();
			String tagName = "";
			Log.w("Developer", "Restoring dictionary ...");
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					Log.w("Developer", "Reading backup file...");
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (tagName.equals(WORD_TAG)) {
						word = new ContentValues();
					} else if (tagName.equals(TOTAL_WORDS_TAG)) {
						totalWords = Integer.parseInt(XmlDataParseHelper
								.readTag(parser, TOTAL_WORDS_TAG));
					} else if (tagName.equals(APPID_TAG)) {
						word.put(APP_ID,
								XmlDataParseHelper.readTag(parser, APPID_TAG));
					} else if (tagName.equals(WORD_TEXT_TAG)) {
						word.put(WORD, XmlDataParseHelper.readTag(parser,
								WORD_TEXT_TAG));
					} else if (tagName.equals(FREQUENCY_TAG)) {
						word.put(FREQUENCY, XmlDataParseHelper.readTag(parser,
								FREQUENCY_TAG));
					} else if (tagName.equals(LOCALE_TAG)) {
						word.put(LOCALE,
								XmlDataParseHelper.readTag(parser, LOCALE_TAG));
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equals(WORD_TAG)) {
						try {
							resolver.insert(URI, word);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					break;
				}
				eventType = parser.next();
			}
			Log.w("Developer", "File reading complete...");
			Log.w("Developer", "Dictionary restore successfull...");
			Log.w("Developer", "Total " + totalWords + " words restored.");
		}
	}
}
