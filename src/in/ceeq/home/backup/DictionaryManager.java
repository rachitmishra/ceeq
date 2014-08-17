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
import android.provider.UserDictionary;
import android.util.Log;
import android.util.Xml;

public class DictionaryManager implements DataManager{

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
	private ContentResolver resolver;
	private XmlSerializer serializer;
	private StringWriter writer;
	
	public DictionaryManager(Context context){
		resolver = context.getContentResolver();
		serializer = Xml.newSerializer();
		writer = new StringWriter();
	}
	public String read() {
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

	public void write(XmlPullParser parser)
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
					totalWords = Integer.parseInt(Utils
							.readTag(parser, TOTAL_WORDS_TAG));
				} else if (tagName.equals(APPID_TAG)) {
					word.put(APP_ID,
							Utils.readTag(parser, APPID_TAG));
				} else if (tagName.equals(WORD_TEXT_TAG)) {
					word.put(WORD, Utils.readTag(parser,
							WORD_TEXT_TAG));
				} else if (tagName.equals(FREQUENCY_TAG)) {
					word.put(FREQUENCY, Utils.readTag(parser,
							FREQUENCY_TAG));
				} else if (tagName.equals(LOCALE_TAG)) {
					word.put(LOCALE,
							Utils.readTag(parser, LOCALE_TAG));
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