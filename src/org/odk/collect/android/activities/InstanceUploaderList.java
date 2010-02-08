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

package org.odk.collect.android.activities;

import java.util.ArrayList;

import org.odk.collect.android.R;
import org.odk.collect.android.database.FileDbAdapter;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.preferences.UserPreferences;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores
 * the path to selected form for use by {@link MainMenu}.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */


// TODO long click form for submission log
// TODO support individual submits
public class InstanceUploaderList extends ListActivity {

    private static final int MENU_UPLOAD_ALL = Menu.FIRST;
    private static final int MENU_PREFS=Menu.FIRST + 1;
    private static final int MENU_UPLOAD_USB=Menu.FIRST + 2;

    private SimpleCursorAdapter mInstances;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // buildView takes place in resume
    }


    /**
     * Retrieves instance information from {@link FileDbAdapter}, composes and
     * displays each row.
     */
    private void buildView() {

        // get all mInstances that match the status.
        FileDbAdapter fda = new FileDbAdapter(this);
        fda.open();
        Cursor c = fda.fetchFilesByNotType(FileDbAdapter.TYPE_INSTANCE, FileDbAdapter.STATUS_SUBMITTED);
        startManagingCursor(c);

        String[] data = new String[] {FileDbAdapter.KEY_DISPLAY, FileDbAdapter.KEY_META};
        int[] view = new int[] {android.R.id.text1, android.R.id.text2};

        // render total instance view
        mInstances =
                new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, c, data, view);
        setListAdapter(mInstances);
        if (c.getCount() > 0) {
            setListAdapter(mInstances);
        } else {
            finish();
        }

        // set title
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.send_data));

        // cleanup
        fda.close();
    }


    private void uploadAllData() {

        // paths to upload
        ArrayList<String> allInstances = new ArrayList<String>();

        Cursor c = null;

        for (int i = 0; i < mInstances.getCount(); i++) {
            c = (Cursor) getListAdapter().getItem(i);
            String s = c.getString(c.getColumnIndex(FileDbAdapter.KEY_FILEPATH));
            allInstances.add(s);
        }

        if (c != null) {
            c.close();
        }

        // bundle intent with upload files
        Intent i = new Intent(this, InstanceUploaderActivity.class);
        i.putExtra(GlobalConstants.KEY_INSTANCES, allInstances);
        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_UPLOAD_ALL, 0, R.string.send_data).setIcon(
                R.drawable.ic_menu_send);
        menu.add(0, MENU_UPLOAD_USB, 0, "Sync Over USB").setIcon(
                R.drawable.ic_menu_send);
        menu.add(0, MENU_PREFS, 0, getString(R.string.user_preferences)).setIcon(
                android.R.drawable.ic_menu_preferences);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_UPLOAD_ALL:
                uploadAllData();
                break;
            case MENU_UPLOAD_USB:
                uploadOverUsb();
                break;
            case MENU_PREFS:
                createPreferencesMenu();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void uploadOverUsb() {
    	// paths to upload
        ArrayList<String> allInstances = new ArrayList<String>();

        Cursor c = null;

        for (int i = 0; i < mInstances.getCount(); i++) {
            c = (Cursor) getListAdapter().getItem(i);
            String s = c.getString(c.getColumnIndex(FileDbAdapter.KEY_FILEPATH));
            System.out.println(s);
            allInstances.add(s);
        }

        if (c != null) {
            c.close();
        }
        
        Intent i = new Intent(this, InstanceUsbSyncActivity.class);
        i.putExtra(GlobalConstants.KEY_INSTANCES, allInstances);
        startActivity(i);
	}


	private void createPreferencesMenu() {
        Intent i = new Intent(this, UserPreferences.class);
        startActivity(i);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        buildView();
    }
}