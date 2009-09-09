package org.odk.collect.android;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ServerPreferences extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {
	private static HttpClient mHttpClient;
	private ProgressDialog mProgressDialog;
	private static String serverUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.server_preferences);
        Button doneButton =new Button(this);
        doneButton.setText("      Done     ");
        doneButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
    			setResult(RESULT_OK);
    	        finish();
            }
        });
        
        Button checkButton =new Button(this);
        checkButton.setText(" Test Server Connectivity ");
        checkButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	serverUrl = PreferenceManager.getDefaultSharedPreferences(ServerPreferences.this).getString("UploadServer", null);
    			if (serverUrl != null)
    				connect();
            }
        });
        
        RelativeLayout layout = new RelativeLayout(this);
        layout.setLayoutParams( new  ViewGroup.LayoutParams( RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT ) );
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params1.addRule(RelativeLayout.CENTER_HORIZONTAL);
        
        params2.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(checkButton, params2);
        layout.addView(doneButton, params1);
        
        addContentView(layout, params1);
        updateSummary();
    }


    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("UploadServer")) {
            updateSummary();
        }
    }


    private void updateSummary() {
        EditTextPreference etp =
                (EditTextPreference) this.getPreferenceScreen().findPreference("UploadServer");
        etp.setSummary(etp.getText());
    }
    
	
	private void connect() {
		
		mProgressDialog = new ProgressDialog(this);
		DialogInterface.OnClickListener geopointButtonListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// on cancel, stop connection
				mHttpClient.getConnectionManager().shutdown();
				finish();
			}
		};

		mProgressDialog.setCancelable(false);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setMessage("Contacting web server. . . ");
		mProgressDialog.setButton(this.getString(R.string.cancel),
				geopointButtonListener);
		mProgressDialog.show();

		Thread thread=new Thread(new testServer());
		thread.start();
		
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Toast.makeText(ServerPreferences.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
		}
	};

	class testServer implements Runnable{
		private String message;

		public void run() {
			HttpParams my_httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(my_httpParams,SharedConstants.CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(my_httpParams, SharedConstants.CONNECTION_TIMEOUT);

			mHttpClient = new DefaultHttpClient(my_httpParams);
			HttpGet httpGet = new HttpGet(serverUrl);

			try {
				HttpResponse response= mHttpClient.execute(httpGet);
				
				if (response.getStatusLine().getStatusCode()==200 ||
						response.getStatusLine().getStatusCode()==401)
					message = "Successfuly contacted Server";
				else {
					Log.i("testServer",response.getStatusLine().toString());
					message=response.getStatusLine().toString();
				}
				Thread.sleep(1000);
			} catch (SocketTimeoutException e) {
				message = "Could not connect to server: Connection Timeout";
			} catch (IOException e) {
				message = "An error occured contacting web server";
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mProgressDialog.dismiss();
			Message msg=Message.obtain();
			msg.obj = message;
			handler.sendMessage(msg);
		}
		
	}

}
