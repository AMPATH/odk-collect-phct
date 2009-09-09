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
 * WARRANTIES OR CONDITIONS OF ANY KIND, either expAress or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenu}.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */


/* TODO: It'd be great to be able to long click on a form and see a log of 
 * successful and unsuccessful submission and servers they were submitted to.
 * 
 */
public class DataUploader extends Activity {

	private static final int SERVER_AUTHENTICATE=0;
	private static final int SERVER_PREFERENCES=1;
	
    private static final int MENU_SET_SERVER = Menu.FIRST;
    private static String serverUrl; 



    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(this.getLocalClassName(), "called onCreate");

        setContentView(R.layout.data_uploader);

       PreferenceManager.setDefaultValues(this, R.xml.server_preferences, false);
       serverUrl = PreferenceManager.getDefaultSharedPreferences(this).getString(
					"UploadServer", "Server is not set");
       ((TextView) findViewById(R.id.upload_url)).setText(serverUrl);
    		   
       
       // create the send data button
       ((Button) findViewById(R.id.upload_data))
               .setOnClickListener(new OnClickListener() {
                   public void onClick(View v) {
                	   Intent i = new Intent(getApplicationContext(), ServerAuthenticate.class);
                       startActivityForResult(i, SERVER_AUTHENTICATE);              	   
                   }
               });
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_SET_SERVER, 0, "Set Server").setIcon(R.drawable.ic_menu_preferences);
        return true;
    }

 
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SET_SERVER:
                Intent launchPreferencesIntent =
                        new Intent().setClass(this, ServerPreferences.class);
                startActivityForResult(launchPreferencesIntent,SERVER_PREFERENCES);
                return true;
         }
        return super.onMenuItemSelected(featureId, item);
    }
    
    /**
     * Upon return, check intent for data needed.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_CANCELED) {
            // The request was canceled, so do nothing.
            return;
        }

        switch (requestCode) {
             case SERVER_AUTHENTICATE:
            	 String s = intent.getStringExtra("credentials");
            	 Intent i = new Intent(getApplicationContext(), UploaderActivity.class);
                 i.putExtra("credentials", s);
                 startActivity(i);
             case SERVER_PREFERENCES:
            	 serverUrl = PreferenceManager.getDefaultSharedPreferences(this).getString(
     					"UploadServer", "Server is not set");
            	 ((TextView) findViewById(R.id.upload_url)).setText(serverUrl);
        }
    }

}
