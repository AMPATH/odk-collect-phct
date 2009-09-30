package org.odk.collect.android.activities;

import org.odk.collect.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AdminAuthenticateActivity extends Activity {
	private static String adminPassword;
	private Intent i;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin_authenticate);

		adminPassword = PreferenceManager.getDefaultSharedPreferences(this).getString("admin_password", null);
		authenticate();
	}

	/**
	 * Create the login dialog
	 */

	private void authenticate() {
		AlertDialog mLoginDialog;
		i = new Intent();
		LayoutInflater li = LayoutInflater.from(this);
		final View v = li.inflate(R.layout.login_form, null);
		mLoginDialog = new AlertDialog.Builder(this).create();
		mLoginDialog.setTitle(getString(R.string.login_form));
		mLoginDialog.setView(v);
		v.setBackgroundColor(Color.WHITE);
		DialogInterface.OnClickListener DialogLogin = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case AlertDialog.BUTTON1:
					EditText password = (EditText) v.findViewById(R.id.password);
					password.setSingleLine();
					if (!adminPassword.equals(password.getText().toString())) {
						Toast.makeText(AdminAuthenticateActivity.this,"Wrong Password to restricted Area!", Toast.LENGTH_SHORT).show();
						setResult(RESULT_CANCELED, i);
					}
					else
						setResult(RESULT_OK, i);
					dialog.dismiss();
					finish();
					break;
				case AlertDialog.BUTTON2: // cancel, do nothing
					dialog.dismiss();
					setResult(RESULT_CANCELED, i);
					finish();
					break;
				}
			}
		};
		mLoginDialog.setCancelable(false);
		mLoginDialog.setButton(getString(R.string.ok), DialogLogin);
		mLoginDialog.setButton2(getString(R.string.cancel), DialogLogin);
		mLoginDialog.show();
	}
}