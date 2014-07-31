package in.ceeq.exceptions;

import java.io.IOException;

public class ExternalStorageNotFoundException extends IOException {

	private static final long serialVersionUID = -6203316041370436518L;

	public ExternalStorageNotFoundException() {
		super("ExternalStorageNotFound");
	}

	public ExternalStorageNotFoundException(String detailMessage) {
		super("External Storage is not available on the device");
	}

}
