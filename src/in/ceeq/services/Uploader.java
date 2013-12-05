/**
 * 
 * @author Rachit Mishra
 * @licence The MIT License (MIT) Copyright (c) <2013> <Rachit Mishra> 
 *
 */

package in.ceeq.services;

import android.app.IntentService;
import android.content.Intent;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

import in.ceeq.helpers.PhoneHelper;
import in.ceeq.helpers.PhoneHelper.Phone;
import in.ceeq.helpers.PreferencesHelper;

public class Uploader extends IntentService {

	public Uploader() {
		super("ServiceUploader");
	}

	private static final String USERID = "userid";
	private static final String USERNAME = "username";
	private static final String PIN = "pinnumber";
	private static final String REGISTRATION_DATE = "regdate";
	private static final String SIM_NUMBER = "simnum";
	private static final String MANUFACTURER = "manufacturer";
	private static final String MODEL = "model";
	private static final String IEMI_NUMBER = "ieminum";
	private static final String GCM_ID = "gcmid";
	private static final String DEVICE_ADMIN = "deviceadmin";
	private static final String ACTION = "action";

	public enum UploadType {
		NEW, DATA, BLIP, FEEDBACK
	}

	public static final String UPLOAD_STATUS_ACCOUNT = "upload_account_data";
	public static final String UPLOAD_STATUS_DATA = "upload_user_data";
	public static final String UPLOAD_STATUS_BLIP = "upload_blip";
	public static final String UPLOAD_STATUS_FEEDBACK = "upload_feedback";

	public static final int HTTP_STATUS_SUCCESS = 200;
	public static final int HTTP_STATUS_FAILURE = 404;
	public static final boolean UPLOAD_SUCCESS = true;
	public static final boolean UPLOAD_FAILURE = false;

	private PreferencesHelper preferencesHelper;
	private PhoneHelper phoneHelper;
	private boolean userData, newAccount, newFeedback, newBlip;
	private UploadType uploadType;

	@Override
	public void onCreate() {
		preferencesHelper = PreferencesHelper.getInstance(this);
		phoneHelper = PhoneHelper.getInstance(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		clearPendingUploads();
		uploadType = (UploadType) intent.getExtras().get(ACTION);
		switch (uploadType) {
			case NEW :
				if (sendData(uploadRegistrationData())) {
					newAccount = false;
				}
				break;
			case DATA :
				if (sendData(uploadBackupData())) {
					userData = false;
				}
				break;
			case FEEDBACK :
				if (sendData(uploadFeedbackData())) {
					newFeedback = false;
				}
				break;
			case BLIP :
				if (sendData(uploadBlipData())) {
					newBlip = false;
				}
				break;
		}

		setUploadStatus();
	}

	private void clearPendingUploads() {

	}

	private void setUploadStatus() {
		if (!newAccount)
			preferencesHelper.setBoolean(UPLOAD_STATUS_ACCOUNT, false);
		if (!userData)
			preferencesHelper.setBoolean(UPLOAD_STATUS_DATA, false);
		if (!newFeedback)
			preferencesHelper.setBoolean(UPLOAD_STATUS_FEEDBACK, false);
		if (!newBlip)
			preferencesHelper.setBoolean(UPLOAD_STATUS_BLIP, false);

	}

	private boolean sendData(ArrayList<NameValuePair> data) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://10.0.3.2/ceeq/users/");
			httppost.setEntity(new UrlEncodedFormEntity(data));
			HttpResponse response = httpclient.execute(httppost);
			switch (response.getStatusLine().getStatusCode()) {
				case HTTP_STATUS_SUCCESS :
					return true;
				default :
					return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private ArrayList<NameValuePair> uploadRegistrationData() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
				10);
		nameValuePairs.add(new BasicNameValuePair(USERID, preferencesHelper
				.getString(PreferencesHelper.ACCOUNT_USER_ID)));
		nameValuePairs.add(new BasicNameValuePair(USERNAME, preferencesHelper
				.getString(PreferencesHelper.ACCOUNT_USER_NAME)));
		nameValuePairs.add(new BasicNameValuePair(PIN, preferencesHelper
				.getString(PreferencesHelper.PIN_NUMBER)));
		nameValuePairs
				.add(new BasicNameValuePair(
						REGISTRATION_DATE,
						preferencesHelper
								.getString(PreferencesHelper.ACCOUNT_REGISTRATION_DATE)));
		nameValuePairs.add(new BasicNameValuePair(SIM_NUMBER, preferencesHelper
				.getString(PreferencesHelper.SIM_NUMBER)));
		nameValuePairs.add(new BasicNameValuePair(MANUFACTURER, phoneHelper
				.getData(Phone.MANUFACTURER)));
		nameValuePairs.add(new BasicNameValuePair(MODEL, phoneHelper
				.getData(Phone.MODEL)));
		nameValuePairs.add(new BasicNameValuePair(IEMI_NUMBER, phoneHelper
				.getData(Phone.IEMI)));
		nameValuePairs.add(new BasicNameValuePair(GCM_ID, preferencesHelper
				.getString(PreferencesHelper.GCM_REGISTRATION_ID)));
		nameValuePairs
				.add(new BasicNameValuePair(DEVICE_ADMIN, preferencesHelper
						.getBoolean(PreferencesHelper.DEVICE_ADMIN_STATUS) + ""));
		return nameValuePairs;
	}

	private ArrayList<NameValuePair> uploadBackupData() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
				3);

		return nameValuePairs;
	}

	private ArrayList<NameValuePair> uploadFeedbackData() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
				3);
		nameValuePairs.add(new BasicNameValuePair(USERID, preferencesHelper
				.getString(PreferencesHelper.ACCOUNT_USER_ID)));
		nameValuePairs.add(new BasicNameValuePair(USERNAME, preferencesHelper
				.getString(PreferencesHelper.ACCOUNT_USER_NAME)));
		nameValuePairs.add(new BasicNameValuePair("message", preferencesHelper
				.getString("feedbackMessage")));
		return nameValuePairs;
	}

	private ArrayList<NameValuePair> uploadBlipData() {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
				3);

		return nameValuePairs;
	}

}
