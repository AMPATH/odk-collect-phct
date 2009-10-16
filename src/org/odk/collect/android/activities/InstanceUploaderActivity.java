/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.activities;

import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.odk.collect.android.R;
import org.odk.collect.android.database.FileDbAdapter;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.logic.HCTSharedConstants;
import org.odk.collect.android.preferences.ServerPreferences;
import org.odk.collect.android.tasks.InstanceUploaderTask;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Activity to upload completed forms.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * 
 */
public class InstanceUploaderActivity extends Activity implements InstanceUploaderListener {

    private final static int PROGRESS_DIALOG = 1;
    private final static String KEY_TOTALCOUNT = "totalcount";
    private ProgressDialog mProgressDialog;

    private InstanceUploaderTask mInstanceUploaderTask;
    private int totalCount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get instances to upload
        Intent i = getIntent();
        ArrayList<String> instances = i.getStringArrayListExtra(GlobalConstants.KEY_INSTANCES);
        if (instances == null) {
            // nothing to upload
            return;
        }
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String url = settings.getString(ServerPreferences.KEY_SERVER, getString(R.string.default_server));
        String authCredentials = settings.getString(ServerPreferences.KEY_USERNAME, getString(R.string.username)) + 
    			":" + settings.getString(ServerPreferences.KEY_PASSWORD, getString(R.string.password));
        
        if (authenticate(url,authCredentials)){
	        mInstanceUploaderTask = (InstanceUploaderTask) getLastNonConfigurationInstance();
	        if (mInstanceUploaderTask == null) {
	
	            // setup dialog and upload task
	            showDialog(PROGRESS_DIALOG);
	            mInstanceUploaderTask = new InstanceUploaderTask();
	            mInstanceUploaderTask.setUploadServer(url + "/" + HCTSharedConstants.UPLOADER_FILE);
	            mInstanceUploaderTask.setAuthCredentials(authCredentials);
	            totalCount = instances.size();
	            
	            // convert array list to an array
	            String[] sa = instances.toArray(new String[totalCount]);
	            mInstanceUploaderTask.execute(sa);
	
		    }
        }

    }


    // TODO: if uploadingComplete() when activity backgrounded, won't work.
    public void uploadingComplete(ArrayList<String> result) {

        int resultSize = result.size();
        boolean success = false;
        if (resultSize == totalCount) {
            Toast.makeText(this, getString(R.string.upload_all_successful, totalCount),
                    Toast.LENGTH_SHORT).show();
            
            success=true;
        } else {
            String s = totalCount - resultSize + " of " + totalCount;
            Toast.makeText(this, getString(R.string.upload_some_failed, s), Toast.LENGTH_LONG)
                    .show();
        }

        Intent in = new Intent();
        in.putExtra(GlobalConstants.KEY_SUCCESS, success);
        setResult(RESULT_OK, in);
        
        // for each path, update the status
        FileDbAdapter fda = new FileDbAdapter(this);
        fda.open();
        for (int i = 0; i < resultSize; i++) {
            fda.updateFile(result.get(i), FileDbAdapter.STATUS_SUBMITTED, null);
        }
        fda.close();
        finish();

    }

    public void progressUpdate(int progress, int total) {
        mProgressDialog.setMax(total);
        mProgressDialog.setProgress(progress);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mInstanceUploaderTask.setUploaderListener(null);
                                finish();
                            }
                        };
                mProgressDialog.setTitle(getString(R.string.uploading_data));
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMax(0);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
        }
        return null;
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        totalCount = savedInstanceState.getInt(KEY_TOTALCOUNT);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_TOTALCOUNT, totalCount);
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        return mInstanceUploaderTask;
    }


    @Override
    protected void onDestroy() {
        if (mInstanceUploaderTask !=null)
    		mInstanceUploaderTask.setUploaderListener(null);
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        if (mInstanceUploaderTask != null) {
            mInstanceUploaderTask.setUploaderListener(this);
        }
        super.onResume();
    }
    
      /**
	 * Authenticate user before attempting to submit files
	 */
	private boolean authenticate(String serverUrl, String authCredentials) {
		// configure connection
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, GlobalConstants.CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, GlobalConstants.CONNECTION_TIMEOUT);
        HttpClientParams.setRedirecting(params, false);
        HttpGet httpGet = new HttpGet(serverUrl);

        // setup client
        DefaultHttpClient httpclient = new DefaultHttpClient(params);
		
        if (authCredentials == null)
			authCredentials="";

		byte[] bytes = authCredentials.getBytes();
		try {
			httpGet.setHeader("Authorization", "Basic " + new String(Base64.encodeBase64(bytes)));
			HttpResponse response = httpclient.execute(httpGet);
			if (response.getStatusLine().getStatusCode()== 401){
				// Authentication Required but supplied wrong credentials
				Toast.makeText(this, R.string.upload_error, Toast.LENGTH_LONG).show();
				return false;
			}

		}catch (Exception e){
			e.printStackTrace();
		}
		return true;
	}


}
