package in.ceeq.home;

import in.ceeq.R;
import in.ceeq.about.AboutApplicationFragment;
import in.ceeq.commons.GooglePlusActivity;
import in.ceeq.commons.Utils;
import in.ceeq.help.HelpActivity;
import in.ceeq.home.NavigationDrawerFragment.NavigationDrawerCallbacks;
import in.ceeq.home.about.AboutDeviceFragment;
import in.ceeq.home.backup.BackupFragment;
import in.ceeq.home.dashboard.DashboardFragment;
import in.ceeq.home.security.SecurityFragment;
import in.ceeq.privacy.DataPrivacyFragment;
import in.ceeq.settings.SettingsActivity;
import org.apache.http.protocol.HTTP;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.widget.Toast;

public class HomeActivity extends FragmentActivity implements NavigationDrawerCallbacks {

	private boolean exit = false;
	private NavigationDrawerFragment mNavigationDrawerFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(
				R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		getSupportFragmentManager().beginTransaction().replace(R.id.container, DashboardFragment.getInstance(), "Home")
				.commit();
	}

	public void setupGoogleCloudMessaging() {
		GcmRegistrationTask.run(this);
	}

	public void checkPlayServices() {
		if (!Utils.enabled(Utils.PLAY_SERVICES, this)) {
			startActivity(new Intent(this, GooglePlusActivity.class).putExtra(GooglePlusActivity.FROM,
					GooglePlusActivity.HOME));
			this.finish();
		}
	}

	@Override
	public void onBackPressed() {
		if (exit)
			HomeActivity.this.finish();
		else {
			Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
			exit = true;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					exit = false;
				}
			}, 3 * 1000);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		Fragment fragment = null;
		switch (position) {
		case 1:
			fragment = AboutDeviceFragment.getInstance();
			break;
		case 3:
			fragment = DashboardFragment.getInstance();
			break;
		case 4:
			fragment = BackupFragment.getInstance();
			break;
		case 5:
			fragment = SecurityFragment.getInstance();
			break;
		case 7:
			fragment = DataPrivacyFragment.getInstance();
			break;
		case 8:
			sendSupportMail();
			break;
		case 9:
			fragment = AboutApplicationFragment.getInstance();
			break;
		case 10:
			rateApplication();
			break;
		case 11:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case 12:
			startActivity(new Intent(this, HelpActivity.class));
			break;
		case 13:
			shareApplication();
			break;
		}
		if (fragment != null) {
			getSupportFragmentManager().beginTransaction().addToBackStack("Home");
			getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "Home").commit();
		}
	}

	private void sendSupportMail() {
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
				getString(R.string.ceeq_support_email), null));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Suggestion/Bugs");
		emailIntent.putExtra(Intent.EXTRA_TEXT,
				"[Ceeq Support \n User: " + Utils.getStringPrefs(this, Utils.ACCOUNT_USER_ID) + "]");
		startActivity(Intent.createChooser(emailIntent, "Write to Us"));
	}

	private void rateApplication() {
		Intent rateIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.ceeq_play_link)));
		startActivity(rateIntent);
	}

	private void shareApplication() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType(HTTP.PLAIN_TEXT_TYPE);
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Ceeq Mobile Application - Device Security and Backup");
		shareIntent.putExtra(Intent.EXTRA_TEXT,
				"Hello, install Ceeq for free, enjoy your new security partner for Android.  \n\n");
		shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.ceeq_play_link));
		startActivity(Intent.createChooser(shareIntent, "Share to"));
	}
}