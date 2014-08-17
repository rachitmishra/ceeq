package in.ceeq.home.backup;

import in.ceeq.commons.Utils;

import java.io.IOException;
import java.io.StringWriter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.util.Xml;

public class MessageManager implements DataManager {

		private static final String READ_TAG = "read", BODY_TAG = "body",
				NUMBER_TAG = "number", MESSAGE_TAG = "message",
				MESSAGES_TAG = "messages", NUM_TAG = "address",
				TYPE_TAG = "type", DATE_TAG = "date",
				TOTAL_MESSAGES_TAG = "totalmessages";

		private final Uri URI = Uri.parse("content://mms-sms/conversations"),
				MURI = Uri.parse("content://sms");
		private ContentResolver resolver;
		private XmlSerializer serializer;
		private StringWriter writer;
		
		public MessageManager(Context context){
			resolver = context.getContentResolver();
			serializer = Xml.newSerializer();
			writer = new StringWriter();
		}
		public String read() {
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

		public void write(XmlPullParser parser)
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
						totalMessages = Integer.parseInt(Utils
								.readTag(parser, TOTAL_MESSAGES_TAG));
					} else if (tagName.equals(NUMBER_TAG)) {
						message.put(NUMBER_TAG,
								Utils.readTag(parser, NUMBER_TAG));
					} else if (tagName.equals(BODY_TAG)) {
						message.put(BODY_TAG,
								Utils.readTag(parser, BODY_TAG));
					} else if (tagName.equals(DATE_TAG)) {
						message.put(DATE_TAG,
								Utils.readTag(parser, DATE_TAG));
					} else if (tagName.equals(TYPE_TAG)) {
						message.put(TYPE_TAG,
								Utils.readTag(parser, TYPE_TAG));
					} else if (tagName.equals(READ_TAG)) {
						message.put(READ_TAG,
								Utils.readTag(parser, READ_TAG));
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
