package org.odk.collect.android.activities;

import org.odk.collect.android.R;
import org.odk.collect.android.database.LoginDbAdapter;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.preferences.GlobalPreferences;
import org.odk.collect.android.preferences.UserPreferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class AuthenticateActivity extends Activity {
	
    private static final int USER_PREFERENCES = Menu.FIRST;
    private static final int GLOBAL_PREFERENCES = Menu.FIRST + 1;
    private static final String KEY_APPLICATION_PASSWORD="application_password";
    private static final String KEY_PASSWORD="password";
    private static final String KEY_ADMIN_PASSWORD="password";
    private static String userPassword;
    private static String adminPassword; 
    private static SharedPreferences sp;

	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.aunthenticate);
    	
    	sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	GlobalConstants.isPassworded =sp.getBoolean(KEY_APPLICATION_PASSWORD,false);
    	userPassword=sp.getString(KEY_PASSWORD, null);
    	adminPassword=sp.getString(KEY_ADMIN_PASSWORD, null);

		if (GlobalConstants.isPassworded)
			checkPassword();
		else {
			Intent i = new Intent(this, MainMenuActivity.class);
			startActivity(i);
			finish();
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, USER_PREFERENCES, 0, getString(R.string.user_preferences)).setIcon(
                android.R.drawable.ic_menu_preferences);
        menu.add(0, GLOBAL_PREFERENCES, 0, getString(R.string.global_preferences)).setIcon(
                android.R.drawable.ic_menu_preferences);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case USER_PREFERENCES:
                createUserPreferencesMenu();
                return true;
            case GLOBAL_PREFERENCES:
            	createGlobalPreferencesMenu();
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void createUserPreferencesMenu() {
        Intent i = new Intent(this, UserPreferences.class);
        startActivity(i);
    }
    private void createGlobalPreferencesMenu() {
    	Intent i = new Intent(this, GlobalPreferences.class);
    	if (GlobalConstants.isPassworded) {
	    	if (GlobalConstants.isAdminAuthenticated) 
		        startActivity(i);
	    	else
	    		Toast.makeText(this, R.string.access_denied, Toast.LENGTH_SHORT).show();
    	}else
    		startActivity(i);
    }
    
    private void checkPassword() {
    	final LinearLayout layout = new LinearLayout(this);
    	final EditText password = new EditText(this);
    	final CheckBox adminCheck = new CheckBox(this);
    	
    	layout.setOrientation(LinearLayout.VERTICAL);
    	password.setSingleLine();
    	password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
    	password.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
    	adminCheck.setText(R.string.admin_login);
    	adminCheck.setTextColor(Color.WHITE);
    	layout.addView(password);
    	layout.addView(adminCheck);
    	
    	new AlertDialog.Builder(this)
    	     .setView(layout)
    	     .setTitle(R.string.login)
    	     .setPositiveButton("OK", new DialogInterface.OnClickListener(){
    	          public void onClick(DialogInterface d, int which) {
    	               d.dismiss();
    	               if (confirmPassword(password.getText().toString(), adminCheck.isChecked())) {
	    	            	Intent i = new Intent(getBaseContext(), MainMenuActivity.class);
		       				startActivity(i);
		       				finish();
    	               }
    	               else {
                           Toast.makeText(AuthenticateActivity.this, R.string.login_error, Toast.LENGTH_SHORT).show();
                           finish();
    	        		}
    	          }
    	     })
    	     .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
    	          public void onClick(DialogInterface d, int which) {
    	        	  //do nothing
    	        	  finish();
    	          }
    	     }).create().show();
    }
    
    private boolean confirmPassword(String password, boolean isAdmin) {
    	boolean success=false;
    	LoginDbAdapter loginDbAdapter =new LoginDbAdapter(this);
    	loginDbAdapter.open();
    	
    	//clear both authentication keys
    	loginDbAdapter.setAuthenticated("admin_authenticated", 0);
    	loginDbAdapter.setAuthenticated("user_authenticated", 0);
    	GlobalConstants.isAdminAuthenticated=false;
    	
    	if (isAdmin) {
    		//Check admin password
    		if (password.equals(adminPassword) || adminPassword==null) {
    			loginDbAdapter.setAuthenticated("admin_authenticated", 1);
    			GlobalConstants.isAdminAuthenticated=true;
    			success=true;
    		}else
    			Log.d("Debug", adminPassword + "error");
    	}else {
    		// Check user password
    		if (password.equals(userPassword)) {
    			loginDbAdapter.setAuthenticated("user_authenticated", 1);
    			success=true;
    		}
    	}
    	loginDbAdapter.close();
		return success;
	}
}