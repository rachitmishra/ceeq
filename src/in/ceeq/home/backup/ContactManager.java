package in.ceeq.home.backup;

import in.ceeq.commons.Utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

public class ContactManager implements DataManager {

	private static final String COUNTRY_TAG = "country";
	private static final String TOTAL_CONTACTS_TAG = "totalcontacts";
	private static final String POSTCODE_TAG = "postcode";
	private static final String STATE_TAG = "state";
	private static final String CITY_TAG = "city";
	private static final String STREET_TAG = "street";
	private static final String ADDRESS_TAG = "address";
	private static final String EMAIL_TAG = "email";
	private static final String EMAILS_TAG = "emails";
	private static final String TYPE_TAG = "type";
	private static final String NUMBER_TAG = "number";
	private static final String NUMBERS_TAG = "numbers";
	private static final String NAME_TAG = "name";
	private static final String CONTACT_TAG = "contact";
	private static final String CONTACTS_TAG = "contacts";

	private final Uri URI = ContactsContract.Contacts.CONTENT_URI;
	private final Uri PURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	private final Uri EURI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
	private final Uri AURI = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI;

	private static final String ID = ContactsContract.Contacts._ID;
	private static final String DNAME = ContactsContract.Contacts.DISPLAY_NAME;
	private static final String HPN = ContactsContract.Contacts.HAS_PHONE_NUMBER;
	private static final String CID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
	private static final String PNUM = ContactsContract.CommonDataKinds.Phone.NUMBER;
	private static final String PTYPE = ContactsContract.CommonDataKinds.Phone.TYPE;
	private static final String EMAIL = ContactsContract.CommonDataKinds.Email.DATA;
	private static final String ETYPE = ContactsContract.CommonDataKinds.Email.TYPE;
	private static final String STREET = ContactsContract.CommonDataKinds.StructuredPostal.STREET;
	private static final String CITY = ContactsContract.CommonDataKinds.StructuredPostal.CITY;
	private static final String STATE = ContactsContract.CommonDataKinds.StructuredPostal.REGION;
	private static final String POSTCODE = ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE;
	private static final String COUNTRY = ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY;
	private static final String NCT = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
	private static final String DNM = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME;
	private static final String PCT = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
	private static final String ECT = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;

	private String id;
	private ContentResolver resolver;
	private XmlSerializer serializer;
	private StringWriter writer;

	public ContactManager(Context context) {
		resolver = context.getContentResolver();
		serializer = Xml.newSerializer();
		writer = new StringWriter();
	}

	public String read() {
		Cursor pcs = null, ecs = null, acs = null, cs = resolver.query(URI, null, null, null, null);

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
					if (TextUtils.isEmpty(name)) {
						name = "No name";
					}
					serializer.text(name);
					serializer.endTag("", NAME_TAG);

					int hasPhoneNumber = cs.getInt(cs.getColumnIndex(HPN));
					if (hasPhoneNumber > 0) {
						try {
							pcs = resolver.query(PURI, null, CID + " = ?", new String[] { id }, null);
							serializer.startTag("", NUMBERS_TAG);
							while (pcs.moveToNext()) {
								String number = pcs.getString(pcs.getColumnIndex(PNUM));
								if (!TextUtils.isEmpty(number)) {
									serializer.startTag("", NUMBER_TAG);
									String type = pcs.getString(pcs.getColumnIndex(PTYPE));
									if (!TextUtils.isEmpty(type)) {
										serializer.attribute("", TYPE_TAG, type);
									}
									serializer.text(number);
									serializer.endTag("", NUMBER_TAG);
								}
							}
							serializer.endTag("", NUMBERS_TAG);
						} finally {
							pcs.close();
						}
					}
					
					try {
						ecs = resolver.query(EURI, null, CID + " = ?", new String[] { id }, null);

						serializer.startTag("", EMAILS_TAG);
						while (ecs.moveToNext()) {
							String email = ecs.getString(ecs.getColumnIndex(EMAIL));
							if (!TextUtils.isEmpty(email)) {
								serializer.startTag("", EMAIL_TAG);
								String type = ecs.getString(ecs.getColumnIndex(ETYPE));
								if (!TextUtils.isEmpty(type)) {
									serializer.attribute("", TYPE_TAG, type);
								}
								serializer.text(email);
								serializer.endTag("", EMAIL_TAG);
							}
						}
						serializer.endTag("", EMAILS_TAG);

					} finally {
						ecs.close();
					}

					try {
						acs = resolver.query(AURI, null, CID + " = ?", new String[] { id }, null);
						if (acs.moveToFirst()) {
							serializer.startTag("", ADDRESS_TAG);
							do {

								String street = acs.getString(acs.getColumnIndex(STREET));
								if (!TextUtils.isEmpty(street)) {
									serializer.startTag("", STREET_TAG);
									serializer.text(street);
									serializer.endTag("", STREET_TAG);
								}

								String city = acs.getString(acs.getColumnIndex(CITY));
								if (!TextUtils.isEmpty(city)) {
									serializer.startTag("", CITY_TAG);
									serializer.text(city);
									serializer.endTag("", CITY_TAG);
								}

								String state = acs.getString(acs.getColumnIndex(STATE));
								if (!TextUtils.isEmpty(state)) {
									serializer.startTag("", STATE_TAG);
									serializer.text(state);
									serializer.endTag("", STATE_TAG);
								}

								String postCode = acs.getString(acs.getColumnIndex(POSTCODE));
								if (!TextUtils.isEmpty(postCode)) {
									serializer.startTag("", POSTCODE_TAG);
									serializer.text(postCode);
									serializer.endTag("", POSTCODE_TAG);
								}

								String country = acs.getString(acs.getColumnIndex(COUNTRY));
								if (!TextUtils.isEmpty(country)) {
									serializer.startTag("", COUNTRY_TAG);
									serializer.text(country);
									serializer.endTag("", COUNTRY_TAG);
								}
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

	public void write(XmlPullParser parser) throws XmlPullParserException, IOException {
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
					totalContacts = Integer.parseInt(Utils.readTag(parser, TOTAL_CONTACTS_TAG));
					Log.w("Developer", "File contains " + totalContacts + " contacts...");
				}
				if (tagName.equals(CONTACT_TAG)) {
					contact = new ArrayList<ContentProviderOperation>();
					contact.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
							.withValue(RawContacts.ACCOUNT_TYPE, null).withValue(RawContacts.ACCOUNT_NAME, null)
							.build());
				} else if (tagName.equals(NAME_TAG)) {
					String name = Utils.readTag(parser, NAME_TAG);
					contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(Data.RAW_CONTACT_ID, 0).withValue(Data.MIMETYPE, NCT)
							.withValue(DNM, name).build());
				} else if (tagName.equals(NUMBER_TAG)) {
					String type = parser.getAttributeValue(null, TYPE_TAG);
					String number = Utils.readTag(parser, NUMBER_TAG);
					contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(ContactsContract.Data.MIMETYPE, PCT).withValue(PNUM, number)
							.withValue(PTYPE, type).build());
				} else if (tagName.equals(EMAIL_TAG)) {
					String type = parser.getAttributeValue(null, TYPE_TAG);
					String email = Utils.readTag(parser, EMAIL_TAG);
					contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(ContactsContract.Data.MIMETYPE, ECT).withValue(EMAIL, email)
							.withValue(ETYPE, type).build());
				} else if (tagName.equals(ADDRESS_TAG)) {

				}
				break;
			case XmlPullParser.END_TAG:
				tagName = parser.getName();
				if (tagName.equals(CONTACT_TAG)) {
					try {
						resolver.applyBatch(ContactsContract.AUTHORITY, contact);
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