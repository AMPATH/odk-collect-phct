package org.odk.collect.android;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ServerAuthenticate extends Activity {
	private static HttpClient mHttpClient;
	private AlertDialog mLoginDialog;
	private HttpGet httpGet;
	private static String mAuth;
	private static String serverUrl;
	private static String credentials="credentials";
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_authenticate);
		
		serverUrl = PreferenceManager.getDefaultSharedPreferences(this).getString("UploadServer", "Server is not set");
		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams,SharedConstants.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(my_httpParams, SharedConstants.CONNECTION_TIMEOUT);
		mHttpClient = new DefaultHttpClient(my_httpParams);
		httpGet = new HttpGet(serverUrl);
		createPasswordDialog();
	}

	/**
	 * Create the login dialog
	 */
	private void createPasswordDialog() {
		LayoutInflater li = LayoutInflater.from(this);
		final View v = li.inflate(R.layout.login_form, null);
		mLoginDialog = new AlertDialog.Builder(this).create();
		mLoginDialog.setTitle(getString(R.string.login_form));
		mLoginDialog.setView(v);
		v.setBackgroundColor(Color.WHITE);
		DialogInterface.OnClickListener DialogLogin = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case AlertDialog.BUTTON1:
					EditText username = (EditText) v.findViewById(R.id.username);
					username.setSingleLine();
					EditText password = (EditText) v.findViewById(R.id.password);
					password.setSingleLine();
					mAuth = username.getText().toString() + ":" + password.getText().toString();
					authenticate();
					break;
				case AlertDialog.BUTTON2: // cancel, do nothing
					dialog.dismiss();
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

	
	/**
	 * Authenticate user before attempting to submit files
	 */
	private void authenticate() {
		if (mAuth == null)
			createPasswordDialog();
		else {
			byte[] bytes = mAuth.getBytes();
			try {
				httpGet.setHeader("Authorization", "Basic " + new String(Base64.encodeBase64(bytes)));
				HttpResponse response = mHttpClient.execute(httpGet);
				Log.i("ServerAuthenticate", "Status Line >>" + response.getStatusLine());
				if (response.getStatusLine().getStatusCode()==200){
					// successful authentication
					Bundle b=new Bundle();
					b.putString(credentials, mAuth);
					Intent i=new Intent();
					i.putExtras(b);
					setResult(RESULT_OK,i);
			        finish();
				}
				else {
					// persist until authenticated or user cancels
					createPasswordDialog();
				}
			} catch (IOException e) {
				String msg = "An error occured contacting web server";
				Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
				e.printStackTrace();
				finish();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}