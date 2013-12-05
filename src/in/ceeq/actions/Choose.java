package in.ceeq.actions;

import android.app.Activity;
import android.content.Intent;
import android.provider.ContactsContract;

public class Choose {

	public static final int CONTACT_ACTIVATION_REQUEST = 9012;
	private Activity activity;
	public Choose(Activity activity) {
		this.activity = activity;
	}

	public static Choose getInstance(Activity activity) {
		return new Choose(activity);
	}

	public void contact() {
		Intent chooseContact = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
		chooseContact
				.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		activity.startActivityForResult(chooseContact,
				CONTACT_ACTIVATION_REQUEST);
	}
}
