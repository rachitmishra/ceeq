
//		switch (dialogType) {
//			break;
//		case DEVICE_ADMIN_DIALOG:
//			alertDialogBuilder.setView(getView(DEVICE_ADMIN_DIALOG)).setPositiveButton(getPositiveButtonString(), this);
//			break;
//		case WIPE_DEVICE_DIALOG:
//
//			break;
//		case WIPE_EXTERNAL_STORAGE_DIALOG:
//			alertDialogBuilder.setView(getView(WIPE_EXTERNAL_STORAGE_DIALOG)).setPositiveButton(
//					getPositiveButtonString(), this);
//			break;
//		case WIPE_EXTERNAL_STORAGE_AND_DEVICE_DIALOG:
//			alertDialogBuilder.setView(getView(WIPE_EXTERNAL_STORAGE_AND_DEVICE_DIALOG)).setPositiveButton(
//					getPositiveButtonString(), this);
//			break;
//		default:
//			alertDialogBuilder.setSingleChoiceItems(getChoices(), NONE, this);
//			break;
//		}
//		alertDialogBuilder.setNegativeButton(R.string.cancel, this).create().show();
//	}
//
//		case DEVICE_ADMIN_DIALOG:
//			alertDialogBuilder.setView(getView(DEVICE_ADMIN_DIALOG));

//
//	public int getTitle() {
//		switch (dialogType) {
//		case DEVICE_ADMIN_DIALOG:
//			return R.string.dialog_title_device_admin;
//		default:
//			return R.string.dialog_title_wipe_device;
//		}
//	}
//
//	}
//
//	public View getView(int dialogType) {
//		this.dialogType = dialogType;
//		inflater = activity.getLayoutInflater();
//		switch (dialogType) {
//
//		case DEVICE_ADMIN_DIALOG:
//			return inflater.inflate(R.layout.dialog_device_admin, null);
//
//
//		case PROTECT_DIALOG:
//			protectMeView = inflater.inflate(R.layout.dialog_protect_me, null);
//			// Button facebookConnect = (Button) protectMeView
//			// .findViewById(R.id.facebook_login);
//			LinearLayout socialBox = (LinearLayout) protectMeView.findViewById(R.id.social_box);
//			if (Utils.getBooleanPrefs(context, Utils.FACEBOOK_CONNECT_STATUS)) {
//				socialBox.setVisibility(View.GONE);
//			} else {
//				socialBox.setVisibility(View.VISIBLE);
//			}
//			// facebookConnect.setOnClickListener(new OnClickListener() {
//			// @Override
//			// public void onClick(View v) {
//			// Home.this.connectFacebook();
//			// }
//			// });
//			EditText distressMessage = (EditText) protectMeView.findViewById(R.id.distressMessage);
//			String storedMessage = Utils.getStringPrefs(context, Utils.DISTRESS_MESSAGE);
//			if (!storedMessage.isEmpty())
//				distressMessage.setText(storedMessage);
//			return protectMeView;

//
//	@Override
//	public void onClick(DialogInterface dialog, int which) {
//		deviceAdminComponentName = new ComponentName(context, DeviceAdministrationReceiver.class);
//
//		switch (which) {
//		case Dialog.BUTTON_POSITIVE:
//			switch (dialogType) {
//		case DEVICE_ADMIN_DIALOG:
//			activity.startActivityForResult(
//					new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(
//							DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponentName).putExtra(
//							DevicePolicyManager.EXTRA_ADD_EXPLANATION, activity.getString(R.string.help_note_25)),
//					DEVICE_ADMIN_ACTIVATION_REQUEST);
//			break;

//		case WIPE_DEVICE_DIALOG:
//			if (Utils.getBooleanPrefs(context, Utils.DEVICE_ADMIN_STATUS)) {
//			} else
//				showDialog(DEVICE_ADMIN_DIALOG);
//			break;
//		case WIPE_EXTERNAL_STORAGE_DIALOG:
//
//			break;
//		case WIPE_EXTERNAL_STORAGE_AND_DEVICE_DIALOG:
//			if (!Utils.getBooleanPrefs(context, Utils.DEVICE_ADMIN_STATUS))
//				showDialog(DEVICE_ADMIN_DIALOG);
//			break;
//		default:
//			break;
//		}
//	}
