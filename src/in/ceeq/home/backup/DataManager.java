package in.ceeq.home.backup;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public interface DataManager {
	public String read();
	public void write(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException;
}
