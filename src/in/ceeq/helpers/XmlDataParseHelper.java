package in.ceeq.helpers;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class XmlDataParseHelper {

	private XmlPullParser parser;
	private static final String NULL = null;

	/**
	 * 
	 * @param in
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public XmlDataParseHelper(InputStream in) throws XmlPullParserException,
			IOException, IllegalArgumentException {
		try {
			parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return XmlPullParser
	 */
	public XmlPullParser getParser() {
		return parser;
	}

	/**
	 * 
	 * @param parser
	 * @param tag
	 * @return String
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static String readTag(XmlPullParser parser, String tag)
			throws IOException, XmlPullParserException {
		String tagData = "";
		parser.require(XmlPullParser.START_TAG, NULL, tag);
		if (parser.next() == XmlPullParser.TEXT) {
			tagData = parser.getText();
			parser.nextTag();
		}
		parser.require(XmlPullParser.END_TAG, NULL, tag);
		return tagData;
	}

	/**
	 * 
	 * @param parser
	 * @param tag
	 * @param attributeName
	 * @return String
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static String readAttribute(XmlPullParser parser, String tag,
			String attributeName) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NULL, tag);
		String attributeData = parser.getAttributeValue(NULL, attributeName);
		parser.nextTag();
		parser.require(XmlPullParser.END_TAG, NULL, tag);
		return attributeData;
	}

}
