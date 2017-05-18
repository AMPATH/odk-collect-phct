package org.odk.collect.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Manages the login authentications.
 * 
 * @author Samuel Mbugua 
 */

public class LoginDbAdapter {

    public static final String KEY_ID = "_id";
    public static final String KEY_AUTHENTICATED = "authenticated";
    public static final String KEY_USER_AUTHENTICATED = "user_authenticated";
    public static final String KEY_ADMIN_AUTHENTICATED = "admin_authenticated";
    public static final String KEY_LOGIN_KEY= "login_key";
    private static final String DATABASE_NAME = "login";
    private static final String DATABASE_TABLE = "login_table";
    private static final int DATABASE_VERSION = 1;

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;

    private static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE + " (_id integer primary key autoincrement, "
                    + "login_key text not null, authenticated smallint not null);";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
            createRows(db, KEY_USER_AUTHENTICATED);
            createRows(db, KEY_ADMIN_AUTHENTICATED);
        }

        // upgrading will destroy all old data
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
       
        private void createRows(SQLiteDatabase db, String key) {
        	ContentValues initialValues = new ContentValues();
        	initialValues.put(KEY_LOGIN_KEY, key);
        	initialValues.put(KEY_AUTHENTICATED, 0);
        	db.insert(DATABASE_TABLE, null, initialValues);
        }
    }

    public LoginDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public LoginDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    public boolean isAuthenticated(String keyName) throws SQLException {
    	Cursor mCursor = null;
    	boolean authenticated=false;
    	mCursor= mDb.query(DATABASE_TABLE, new String[] {KEY_AUTHENTICATED}, KEY_LOGIN_KEY + " like'" + keyName + "'", null, null, null, null);
    	if (mCursor.getCount()<1)
   		   authenticated=true;
    	else {
    		mCursor.moveToFirst();
    		if (mCursor.getInt(0)==1)
    			authenticated=true;
    	}
    	mCursor.close();
    	return authenticated;
    }
    
    public boolean setAuthenticated(String keyName, int status) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_AUTHENTICATED, status);
        return mDb.update(DATABASE_TABLE, cv, KEY_LOGIN_KEY + "= '" + keyName + "'", null) > 0;
    }
    
}
