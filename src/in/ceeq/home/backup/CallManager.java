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
import android.provider.CallLog;
import android.util.Log;
import android.util.Xml;

public class CallManager implements DataManager {

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
		private ContentResolver resolver;
		private XmlSerializer serializer;
		private StringWriter writer;
		
		public CallManager(Context context){
			resolver = context.getContentResolver();
			serializer = Xml.newSerializer();
			writer = new StringWriter();
		}
		
		public String read() {

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

		public void write(XmlPullParser parser)
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
						totalCalls = Integer.parseInt(Utils
								.readTag(parser, TOTAL_CALLS_TAG));
					} else if (tagName.equals(NUMBER_TAG)) {
						call.put(NUMBER,
								Utils.readTag(parser, NUMBER_TAG));
					} else if (tagName.equals(TYPE_TAG)) {
						call.put(TYPE,
								Utils.readTag(parser, TYPE_TAG));
					} else if (tagName.equals(DATE_TAG)) {
						call.put(DATE,
								Utils.readTag(parser, DATE_TAG));
					} else if (tagName.equals(DURATION_TAG)) {
						call.put(DURATION, Utils.readTag(parser,
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