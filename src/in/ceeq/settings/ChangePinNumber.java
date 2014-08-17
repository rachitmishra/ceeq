package in.ceeq.settings;

import in.ceeq.R;
import in.ceeq.commons.Utils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

class ChangePinNumber extends DialogPreference {

	private EditText oldPinNumber;
	private EditText newPinNumber;
	private Context context;

	public ChangePinNumber(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPersistent(false);
		setDialogLayoutResource(R.layout.dialog_new_pin);
		this.context = context;
	}

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
		Button positive = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
		positive.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (oldPinNumber.getText().toString().equals(Utils.getStringPrefs(context, Utils.PIN_NUMBER))
						& newPinNumber.length() >= 6) {
					Toast.makeText(context, "Great, New PIN saved.", Toast.LENGTH_SHORT).show();
					Utils.setStringPrefs(context, Utils.PIN_NUMBER, newPinNumber.getText().toString());
					((AlertDialog) getDialog()).dismiss();
				} else if (oldPinNumber.length() == 0) {
					Toast.makeText(context, "Please, Enter old PIN.", Toast.LENGTH_SHORT).show();
				} else if (newPinNumber.length() == 0) {
					Toast.makeText(context, "Please, Enter new PIN.", Toast.LENGTH_SHORT).show();
				} else if (newPinNumber.length() <= 6) {
					Toast.makeText(context, "New PIN should be of 6 digits.", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, "Sorry, Incorrect old PIN.", Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	@Override
	protected void onBindDialogView(View v) {
		newPinNumber = (EditText) v.findViewById(R.id.newPinNumber);
		oldPinNumber = (EditText) v.findViewById(R.id.oldPinNumber);

		oldPinNumber.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence text, int arg1, int arg2, int arg3) {

			}

			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {
				if (text.length() >= 6 & text.toString().equals(Utils.getStringPrefs(context, Utils.PIN_NUMBER))) {
					oldPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_yes, 0);
				} else {
					oldPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_no, 0);
				}

			}

		});

		newPinNumber.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() < 6) {
					newPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_no, 0);
				} else {
					newPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_yes, 0);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence text, int arg1, int arg2, int arg3) {

			}

			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {
				if (text.length() >= 6 & text.toString().equals(Utils.getStringPrefs(context, Utils.PIN_NUMBER))) {
					newPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_yes, 0);
				} else {
					newPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_no, 0);
				}

			}

		});
	}

}