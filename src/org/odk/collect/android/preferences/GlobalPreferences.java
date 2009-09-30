package org.odk.collect.android.preferences;

import org.odk.collect.android.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class GlobalPreferences extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    public static String KEY_SERVER = "server";
    public static String KEY_USERNAME = "username";
    public static String KEY_PASSWORD = "password";
    public static String KEY_ADMIN_PASSWORD = "admin_password";
    private static String adminPassword;
    private static boolean editAdminPassword=false;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	adminPassword=sp.getString(KEY_ADMIN_PASSWORD, null);
        
        addPreferencesFromResource(R.xml.global_preferences);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.preferences));
        updateServer();
        updateUsername();
        updatePassword();
        
        Button doneButton =new Button(this);
        doneButton.setText("Done");
        doneButton.setPadding(40, 0, 40, 0);
        doneButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
    			setResult(RESULT_OK);
    	        finish();
            }
        });
        
        RelativeLayout layout = new RelativeLayout(this);
        layout.setLayoutParams( new  ViewGroup.LayoutParams( RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT ) );
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params1.addRule(RelativeLayout.CENTER_HORIZONTAL);
        
        layout.addView(doneButton, params1);
        
        addContentView(layout, params1);
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
        if (key.equals(KEY_SERVER)) {
            updateServer();
        } else if (key.equals(KEY_USERNAME)) {
            updateUsername();
        } else if (key.equals(KEY_PASSWORD)) {
            updatePassword();
        } else if (key.equals(KEY_ADMIN_PASSWORD)){
        	if (adminPassword != null && !editAdminPassword)
        		updateAdminPassword();
        }
    }


    private void updateServer() {
        EditTextPreference etp =
                (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_SERVER);
        String s = etp.getText();
        if (s.endsWith("/")) {
            s = s.substring(0, s.lastIndexOf("/"));
        }
        etp.setSummary(s);
    }


    private void updateUsername() {
        EditTextPreference etp =
                (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_USERNAME);
        etp.setSummary(etp.getText());
    }


    private void updatePassword() {
        EditTextPreference etp =
                (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_PASSWORD);
        //etp.setSummary(etp.getText());
        etp.setText(etp.getText());
    }
    
 
    private void updateAdminPassword() {
    	editAdminPassword=false;
    	final FrameLayout fl = new FrameLayout(this);
    	final EditText input = new EditText(this);

    	fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

    	new AlertDialog.Builder(this)
    	     .setView(fl)
    	     .setTitle("Current Admin Passowrd")
    	     .setPositiveButton("OK", new DialogInterface.OnClickListener(){
    	          @Override
    	          public void onClick(DialogInterface d, int which) {
    	               d.dismiss();
    	               if (adminPassword.equals(input.getText().toString()))
    	            	   Toast.makeText(GlobalPreferences.this, "Admin password successfully changed!!", Toast.LENGTH_SHORT).show();
    	               else {    
    	            	   SharedPreferences.Editor editor = sp.edit();
                           editor.putString(KEY_ADMIN_PASSWORD, adminPassword);
                           editor.commit();
                           Toast.makeText(GlobalPreferences.this, "Wrong Password, Admin password was not changed!", Toast.LENGTH_SHORT).show();
    	        		}
    	          }
    	     })
    	     .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
    	          @Override
    	          public void onClick(DialogInterface d, int which) {
    	        	  editAdminPassword=true;
    	        	  d.dismiss();
    	        	  SharedPreferences.Editor editor = sp.edit();
    	        	  editor.putString(KEY_ADMIN_PASSWORD, adminPassword);
    	        	  editor.commit();
    	        	  Toast.makeText(GlobalPreferences.this, "Admin password was not changed!", Toast.LENGTH_SHORT).show();
   
    	          }
    	     }).create().show();
    }
}
