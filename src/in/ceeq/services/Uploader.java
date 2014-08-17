///**
// * 
// * @author Rachit Mishra
// * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
// *
// */
//
//package in.ceeq.services;
//
//import in.ceeq.commons.PhoneUtils;
//import in.ceeq.commons.PreferenceUtils;
//
//import java.util.ArrayList;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicNameValuePair;
//
//import android.app.IntentService;
//import android.content.Intent;
//
//public class Uploader extends IntentService {
//
//	public Uploader() {
//		super("ServiceUploader");
//	}
//
//	private static final String USERID = "userid";
//	private static final String USERNAME = "username";
//	private static final String PIN = "pinnumber";
//	private static final String REGISTRATION_DATE = "regdate";
//	private static final String SIM_NUMBER = "simnum";
//	private static final String MANUFACTURER = "manufacturer";
//	private static final String MODEL = "model";
//	private static final String IEMI_NUMBER = "ieminum";
//	private static final String GCM_ID = "gcmid";
//	private static final String DEVICE_ADMIN = "deviceadmin";
//	public static final String ACTION = "action";
//	private static final String LATITUDE = "latitude";
//	private static final String LONGITUDE = "longitude";
//	private static final String MESSAGE = "message";
//	private static final String BATTERY = "battery";
//
//	public enum UploadType {
//		NEW, DATA, BLIP, FEEDBACK, LOCATE
//	}
//
//	public static final String UPLOAD_STATUS_ACCOUNT = "upload_account_data";
//	public static final String UPLOAD_STATUS_DATA = "upload_user_data";
//	public static final String UPLOAD_STATUS_BLIP = "upload_blip";
//	public static final String UPLOAD_STATUS_FEEDBACK = "upload_feedback";
//	public static final String UPLOAD_STATUS_LOCATION = "upload_location";
//
//	public static final int HTTP_STATUS_SUCCESS = 200;
//	public static final int HTTP_STATUS_FAILURE = 404;
//	public static final boolean UPLOAD_SUCCESS = true;
//	public static final boolean UPLOAD_FAILURE = false;
//
//	private boolean userData, newAccount, newFeedback, newBlip, newLocation;
//	private UploadType uploadType;
//
//	@Override
//	protected void onHandleIntent(Intent intent) {
//		setupHelpers();
//		clearPendingUploads();
//		uploadType = (UploadType) intent.getExtras().get(ACTION);
//		uploadData(uploadType);
//		setUploadStatus();
//	}
//
//	private void clearPendingUploads() {
//		if (Utils.getBoolean(UPLOAD_STATUS_ACCOUNT)) {
//			uploadData(UploadType.NEW);
//		} else if (preferencesHelper.getBoolean(UPLOAD_STATUS_DATA)) {
//			uploadData(UploadType.DATA);
//		} else if (preferencesHelper.getBoolean(UPLOAD_STATUS_FEEDBACK)) {
//			uploadData(UploadType.FEEDBACK);
//		} else if (preferencesHelper.getBoolean(UPLOAD_STATUS_BLIP)) {
//			uploadData(UploadType.BLIP);
//		} else if (preferencesHelper.getBoolean(UPLOAD_STATUS_LOCATION)) {
//			uploadData(UploadType.LOCATE);
//		}
//	}
//
//	private void uploadData(UploadType uploadType) {
//		switch (uploadType) {
//		case NEW:
//			if (sendData(uploadRegistrationData())) {
//				newAccount = false;
//			}
//			break;
//		case DATA:
//			if (sendData(uploadBackupData())) {
//				userData = false;
//			}
//			break;
//		case FEEDBACK:
//			if (sendData(uploadFeedbackData())) {
//				newFeedback = false;
//			}
//			break;
//		case BLIP:
//			if (sendData(uploadBlipData())) {
//				newBlip = false;
//			}
//			break;
//		case LOCATE:
//			if (sendData(uploadLocationData())) {
//				newLocation = false;
//			}
//			break;
//		}
//
//	}
//
//	private void setUploadStatus() {
//		if (!newAccount)
//			preferencesHelper.setBoolean(UPLOAD_STATUS_ACCOUNT, false);
//		if (!userData)
//			preferencesHelper.setBoolean(UPLOAD_STATUS_DATA, false);
//		if (!newFeedback)
//			preferencesHelper.setBoolean(UPLOAD_STATUS_FEEDBACK, false);
//		if (!newBlip)
//			preferencesHelper.setBoolean(UPLOAD_STATUS_BLIP, false);
//		if (!newLocation)
//			preferencesHelper.setBoolean(UPLOAD_STATUS_LOCATION, false);
//
//	}
//
//	private boolean sendData(ArrayList<NameValuePair> data) {
//		try {
//			HttpClient httpclient = new DefaultHttpClient();
//			HttpPost httppost = new HttpPost("http://10.0.3.2/ceeq/users/");
//			httppost.setEntity(new UrlEncodedFormEntity(data));
//			HttpResponse response = httpclient.execute(httppost);
//			switch (response.getStatusLine().getStatusCode()) {
//			case HTTP_STATUS_SUCCESS:
//				return true;
//			default:
//				return false;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
//
//	private ArrayList<NameValuePair> uploadRegistrationData() {
//		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
//				10);
//		nameValuePairs.add(new BasicNameValuePair(USERID, preferencesHelper
//				.getString(PreferenceUtils.ACCOUNT_USER_ID)));
//		nameValuePairs.add(new BasicNameValuePair(USERNAME, preferencesHelper
//				.getString(PreferenceUtils.ACCOUNT_USER_NAME)));
//		nameValuePairs.add(new BasicNameValuePair(PIN, preferencesHelper
//				.getString(PreferenceUtils.PIN_NUMBER)));
//		nameValuePairs
//				.add(new BasicNameValuePair(
//						REGISTRATION_DATE,
//						preferencesHelper
//								.getString(PreferenceUtils.ACCOUNT_REGISTRATION_DATE)));
//		nameValuePairs.add(new BasicNameValuePair(SIM_NUMBER, preferencesHelper
//				.getString(PreferenceUtils.SIM_NUMBER)));
//		nameValuePairs.add(new BasicNameValuePair(MANUFACTURER, PhoneUtils
//				.get(PhoneUtils.MANUFACTURER, this)));
//		nameValuePairs.add(new BasicNameValuePair(MODEL, PhoneUtils
//				.get(PhoneUtils.MODEL, this)));
//		nameValuePairs.add(new BasicNameValuePair(IEMI_NUMBER, PhoneUtils
//				.get(PhoneUtils.IEMI, this)));
//		nameValuePairs.add(new BasicNameValuePair(GCM_ID, preferencesHelper
//				.getString(PreferenceUtils.GCM_REGISTRATION_ID)));
//		nameValuePairs
//				.add(new BasicNameValuePair(DEVICE_ADMIN, preferencesHelper
//						.getBoolean(PreferenceUtils.DEVICE_ADMIN_STATUS) + ""));
//		return nameValuePairs;
//	}
//
//	private ArrayList<NameValuePair> uploadBackupData() {
//		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
//				3);
//
//		return nameValuePairs;
//	}
//
//	private ArrayList<NameValuePair> uploadFeedbackData() {
//		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
//				3);
//		nameValuePairs.add(new BasicNameValuePair(USERID, preferencesHelper
//				.getString(PreferenceUtils.ACCOUNT_USER_ID)));
//		nameValuePairs.add(new BasicNameValuePair(USERNAME, preferencesHelper
//				.getString(PreferenceUtils.ACCOUNT_USER_NAME)));
//		nameValuePairs.add(new BasicNameValuePair(MESSAGE, preferencesHelper
//				.getString(PreferenceUtils.FEEDBACK_MESSAGE)));
//		return nameValuePairs;
//	}
//
//	private ArrayList<NameValuePair> uploadBlipData() {
//		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
//				3);
//		nameValuePairs.add(new BasicNameValuePair(LATITUDE, preferencesHelper
//				.getString(PreferenceUtils.LAST_LOCATION_LATITUDE)));
//		nameValuePairs.add(new BasicNameValuePair(LONGITUDE, preferencesHelper
//				.getString(PreferenceUtils.LAST_LOCATION_LONGITUDE)));
//		nameValuePairs.add(new BasicNameValuePair(BATTERY, PhoneUtils
//				.get(PhoneUtils.BATTERY_LEVEL, this)));
//		return nameValuePairs;
//	}
//
//	private ArrayList<NameValuePair> uploadLocationData() {
//		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
//				3);
//		nameValuePairs.add(new BasicNameValuePair(LATITUDE, preferencesHelper
//				.getString(PreferenceUtils.LAST_LOCATION_LATITUDE)));
//		nameValuePairs.add(new BasicNameValuePair(LONGITUDE, preferencesHelper
//				.getString(PreferenceUtils.LAST_LOCATION_LONGITUDE)));
//		nameValuePairs.add(new BasicNameValuePair(GCM_ID, preferencesHelper
//				.getString(PreferenceUtils.GCM_REGISTRATION_ID)));
//		return nameValuePairs;
//	}
//
//}
