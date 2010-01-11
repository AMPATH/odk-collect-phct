package org.odk.collect.android.preferences;

import org.odk.collect.android.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;

public class UserPreferences extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	public static String KEY_SERVER = "server";
	public static String KEY_USERNAME = "username";
	public static String KEY_PASSWORD = "password";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.user_preferences);
		setTitle(getString(R.string.app_name) + " > " + getString(R.string.user_preferences));
		updatePassword();
		showServer();
		showUsername();

		Button doneButton = new Button(this);
		doneButton.setText("Done");
		doneButton.setPadding(40, 0, 40, 0);
		doneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});

		RelativeLayout layout = new RelativeLayout(this);
		layout.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);

		layout.addView(doneButton, params);

		addContentView(layout, params);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(KEY_PASSWORD)) {
			updatePassword();
		}
	}

	private void updatePassword() {
		EditTextPreference etp = (EditTextPreference) this
				.getPreferenceScreen().findPreference(KEY_PASSWORD);
		etp.setText(etp.getText());
	}
	
	private void showServer() {
		EditTextPreference etp = (EditTextPreference) this
				.getPreferenceScreen().findPreference(KEY_SERVER);
		etp.setSummary(etp.getText());
	}
	
	private void showUsername() {
		EditTextPreference etp = (EditTextPreference) this
				.getPreferenceScreen().findPreference(KEY_USERNAME);
		etp.setSummary(etp.getText());
	}
}
