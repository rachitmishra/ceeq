package in.ceeq.settings;

import in.ceeq.R;
import in.ceeq.commons.Utils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

class ChangeDistressMessage extends DialogPreference {

	private EditText newMessage;
	private Context context;

	public ChangeDistressMessage(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPersistent(false);
		setDialogLayoutResource(R.layout.dialog_new_message);
		this.context = context;
	}

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
		Button positive = ((AlertDialog) getDialog())
				.getButton(DialogInterface.BUTTON_POSITIVE);
		positive.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (newMessage.length() > 0) {
					Toast.makeText(context,
							"Great, New distress message saved.",
							Toast.LENGTH_SHORT).show();
					Utils.setStringPrefs(context, Utils.DISTRESS_MESSAGE, newMessage
							.getText().toString());
					((AlertDialog) getDialog()).dismiss();
				} else {
					Toast.makeText(context,
							"Please, Enter new distress message.",
							Toast.LENGTH_SHORT).show();
				}
			}

		});
	}
}
