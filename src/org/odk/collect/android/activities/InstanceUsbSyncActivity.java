package org.odk.collect.android.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.odk.collect.android.R;
import org.odk.collect.android.database.FileDbAdapter;
import org.odk.collect.android.logic.GlobalConstants;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Activity to upload completed forms.
 * 
 * @author Samuel Mbugua (sthaiya@gmail.com)
 * 
 */
public class InstanceUsbSyncActivity extends Activity {

    private int totalCount = -1;
    private ArrayList<String> instances;
    private static String t = "InstanceUsbSyncActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get instances to upload
        Intent i = getIntent();
        instances = i.getStringArrayListExtra(GlobalConstants.KEY_INSTANCES);
        if (instances != null) {
        	prepareSyncFiles();
        	totalCount = instances.size();
            
            // convert array list to an array
            String[] sa = instances.toArray(new String[totalCount]);
            putIntoUsbSyncDir(sa);
        }else {
            // nothing to upload
            return;
        }
    }
    
    private void prepareSyncFiles() {
    	// 
    	File dir=new File(GlobalConstants.USBSYNC_PATH);
    	if (dir.exists() && dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				file.delete();
			}
			
		}else
			dir.mkdirs();
    	
    }
    
    protected void putIntoUsbSyncDir(String... values) {
        ArrayList<String> uploadedIntances = new ArrayList<String>();
        for (String value : values) {
            // get instance file
            File dir = new File(value);

            // find all files in parent directory
            File[] files = dir.getParentFile().listFiles();
            if (files == null) 
                Log.e(t, "no files to upload");
            for (File file : files) {
                if (file.getName().endsWith(".xml") || file.getName().endsWith(".png") || file.getName().endsWith(".jpg")) {
                	copyfile(file.getAbsolutePath(), GlobalConstants.USBSYNC_PATH + "/" + file.getName());
                	uploadedIntances.add(file.getAbsolutePath());
                }
                else
                    Log.e(t, "unsupported file type, not adding file: " + file.getName());
            } 
        }
        uploadingComplete(uploadedIntances);
    }
    
    private static void copyfile(String srFile, String dtFile){
        try{
          File f1 = new File(srFile);
          File f2 = new File(dtFile);
          InputStream in = new FileInputStream(f1);
          
          //For Overwrite the file.
          OutputStream out = new FileOutputStream(f2);

          byte[] buf = new byte[1024];
          int len;
          while ((len = in.read(buf)) > 0){
            out.write(buf, 0, len);
          }
          in.close();
          out.close();
        }
        catch(FileNotFoundException e){
        	Log.e(t, "error copying file " + e.getMessage());
        }
        catch(IOException e){
        	Log.e(t, e.getMessage());      
        }
      }
    
    private void uploadingComplete(ArrayList<String> result) {

        int resultSize = result.size();
        if (resultSize == totalCount) {
            Toast.makeText(this, getString(R.string.upload_all_successful, totalCount),
                    Toast.LENGTH_SHORT).show();
            
        } else {
            String s = totalCount - resultSize + " of " + totalCount;
            Toast.makeText(this, getString(R.string.upload_some_failed, s), Toast.LENGTH_LONG)
                    .show();
        }

        // for each path, update the status
        FileDbAdapter fda = new FileDbAdapter(this);
        fda.open();
        for (int i = 0; i < resultSize; i++) {
        	Cursor c = fda.fetchFilesByPath(result.get(i), null);
        	if ( c != null) {
        		if (c.getString(c.getColumnIndex(FileDbAdapter.KEY_STATUS)).equals(FileDbAdapter.STATUS_COMPLETED))
        			fda.updateFile(result.get(i), FileDbAdapter.STATUS_SUBMITTED, null);
        	}
        }
        fda.close();
        finish();

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        return null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onResume() {

        super.onResume();
    }
 }
