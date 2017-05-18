package org.odk.collect.android.preferences;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.UrlUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class GlobalPreferences extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    public final static String KEY_SERVER = "server";
    public final static String KEY_PASSWORD = "password";
    public final static String KEY_USERNAME = "username";
    public final static String KEY_APPLICATION_PASSWORD = "application_password";
    public final static String KEY_ADMIN_PASSWORD = "admin_password";
    private static String adminPassword;
    private static boolean pressedCancel=false;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		adminPassword=sp.getString(KEY_ADMIN_PASSWORD, null);
			
		addPreferencesFromResource(R.xml.global_preferences);
		setTitle(getString(R.string.app_name) + " > " + getString(R.string.global_preferences));
		updateServer();
		updateUsername();
		
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
		layout.setLayoutParams( new  ViewGroup.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		
		layout.addView(doneButton, params);
		
		addContentView(layout, params);
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
        } else if (key.equals(KEY_APPLICATION_PASSWORD)) {
        	updatePasswordApplication();
        }else if (key.equals(KEY_ADMIN_PASSWORD)){;
    		if (!pressedCancel) {
    			if (adminPassword==null)
    				adminPassword=getString(R.string.default_admin_password);
    			updateAdminPassword();
    		}
    		else
    			pressedCancel=false;
        }
    }

    private void updateServer() {
    	EditTextPreference etp =
            (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_SERVER);
	    String s = etp.getText();
	    if (UrlUtils.isValidUrl(s)) {
            etp.setText(s);
            etp.setSummary(s);
        } else {
            etp.setText((String) etp.getSummary());
           Toast.makeText(getApplicationContext(), getString(R.string.url_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUsername() {
        EditTextPreference etp =
                (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_USERNAME);
        etp.setSummary(etp.getText());
    }
    
    private void updatePasswordApplication() {
    	CheckBoxPreference chkBoxPreference= 
    		(CheckBoxPreference) this.getPreferenceScreen().findPreference(KEY_APPLICATION_PASSWORD);
        if (adminPassword==null) {
        	chkBoxPreference.setChecked(false);
            Toast.makeText(getApplicationContext(), getString(R.string.application_password_admin_error), Toast.LENGTH_SHORT).show();
            return;
        }
        if (sp.getString(KEY_PASSWORD, null) == null) {
        	chkBoxPreference.setChecked(false);
            Toast.makeText(getApplicationContext(), getString(R.string.application_password_user_error), Toast.LENGTH_SHORT).show();
        }
    }
 
    private void updateAdminPassword() {
    	final FrameLayout fl = new FrameLayout(this);
    	final EditText input = new EditText(this);
    	input.setSingleLine();
    	input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
    	input.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());

    	fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

    	new AlertDialog.Builder(this)
    	     .setView(fl)
    	     .setTitle(getString(R.string.current_admin_password))
    	     .setPositiveButton("OK", new DialogInterface.OnClickListener(){
    	          public void onClick(DialogInterface d, int which) {
    	               d.dismiss();
    	               if (adminPassword.equals(input.getText().toString()))
    	            	   Toast.makeText(GlobalPreferences.this, R.string.successful_password_change, Toast.LENGTH_SHORT).show();
    	               else {
    	            	   SharedPreferences.Editor editor = sp.edit();
                           editor.putString(KEY_ADMIN_PASSWORD, adminPassword);
                           editor.commit();
                           Toast.makeText(GlobalPreferences.this, getString(R.string.error_changing_password) + " " 
                        		   + getString(R.string.invalid_password), Toast.LENGTH_SHORT).show();
    	        		}
    	          }
    	     })
    	     .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
    	          public void onClick(DialogInterface d, int which) {
    	        	  pressedCancel=true;
    	        	  d.dismiss();
    	        	  SharedPreferences.Editor editor = sp.edit();
    	        	  editor.putString(KEY_ADMIN_PASSWORD, adminPassword);
    	        	  editor.commit();
    	        	  Toast.makeText(GlobalPreferences.this, R.string.error_changing_password, Toast.LENGTH_SHORT).show();
   
    	          }
    	     }).create().show();
    }
}