package org.odk.collect.android.database;

import org.odk.collect.android.logic.HCTSharedConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple Database helper for keeping track of already used IDs
 * Can be used by any external class.
 */
public class HCTDbAdapter {

    public static final String KEY_ID = "id";
    public static final String KEY_HOUSEHOLD_HEAD = "headid";
    public static final String KEY_HOUSEHOLD_LOCATION = "location";
    public static final String KEY_HOUSEHOLD_ID = "householdid";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "HCTDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private static final String DATABASE_NAME = "hct";
    private static final int DATABASE_VERSION = 2;
    
    /**
     * Database tables creation sql statements
     */
    private static final String TABLE_CREATE_HOUSEHOLD =
    	"create table " + HCTSharedConstants.HOUSEHOLD + " (" + KEY_ROWID + " integer primary key autoincrement, "
    			+ KEY_ID + " text not null, " + KEY_HOUSEHOLD_HEAD + " text, " + KEY_HOUSEHOLD_LOCATION + " text);";
    private static final String TABLE_CREATE_HCT =
        "create table " + HCTSharedConstants.INDIVIDUAL + " (" + KEY_ROWID + " integer primary key autoincrement, "
        + KEY_ID + " text not null, " + KEY_HOUSEHOLD_ID + " text not null);";
    
    private final Context mCtx;	

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(TABLE_CREATE_HOUSEHOLD);
            db.execSQL(TABLE_CREATE_HCT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + HCTSharedConstants.HOUSEHOLD);
            db.execSQL("DROP TABLE IF EXISTS " + HCTSharedConstants.INDIVIDUAL);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public HCTDbAdapter(Context ctx) {
        this.mCtx =ctx ;
    }

    /**
     * Open the hct database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public HCTDbAdapter open() throws SQLException {
	   mDbHelper = new DatabaseHelper(mCtx);
	   mDb = mDbHelper.getWritableDatabase();
    
        return this;
    }
    
    /**
     * Close the database
     */
    public void close() {
        mDbHelper.close();
    }


    /**
     * Store an ID into the database
     * @param tableName
     * @param id
     * @param parent_id
     */
    public void insertID(String tableName, String id, String parent_id) {
        ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_ID, id);
    	initialValues.put(KEY_HOUSEHOLD_ID, parent_id);
    	mDb.insert(tableName, null, initialValues);
    }
    

	/**
	 * @param table
	 * @param id
	 * @param householdHeadId
	 * @param location
	 */
	public void insertID(String tableName, String id, String headId, String location) {
		ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_ID, id);
    	initialValues.put(KEY_HOUSEHOLD_HEAD, headId);
    	initialValues.put(KEY_HOUSEHOLD_LOCATION, location);
    	mDb.insert(tableName, null, initialValues);
	}
	
	
    /**
     * Return a Cursor over the specified field
     *  @param household_id
     *  @return Cursor 
     */
    public Cursor getAnyField(String tableName, String id, String fieldName) {
    	Cursor mCursor = null;
    	mCursor= mDb.query(true, tableName, new String[] {KEY_ROWID, fieldName}, KEY_ID + " like '" + id + "'", null, null, null, null, null);
        if (mCursor.getCount()<1){
        	mCursor.close();
        	return null;
        }
        return mCursor;
    }

    /**
     * Delete the id given
     * 
     * @param tableName name of table to delete from
     * @param rowId id of householdID to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteID(String tableName, String rowId) {

        return mDb.delete(tableName, KEY_ID + " like '" + rowId + "'", null) > 0;
    }

    /**
     * Return a Cursor over the list of all ids in the database
     *  @return Cursor over all ids
     */
    public Cursor getAllIDs() {
    	Cursor mCursor = null;
    	mCursor= mDb.query(HCTSharedConstants.HOUSEHOLD, new String[] {KEY_ROWID, KEY_ID}, null, null, null, null, null);
        if (mCursor.getCount()<1){
        	mCursor.close();
        	return null;
        }
        return mCursor;
    }
    
    /**
     * Return a Cursor over the list of all hct_ids in the specified household
     *  @param household_id
     *  @return Cursor 
     */
    public Cursor getHCTIDs(String household_id) {
    	Cursor mCursor = null;
    	mCursor= mDb.query(true, HCTSharedConstants.INDIVIDUAL, new String[] {KEY_ROWID, KEY_ID}, KEY_HOUSEHOLD_ID + " like '" + household_id + "'", null, null, null, null, null);
    	if (mCursor.getCount()<1){
        	mCursor.close();
        	return null;
        }
        return mCursor;
    }

    /**
     * Return a Cursor positioned at the ID given
     * 
     * @param tableName
     * @param itemId id of note to retrieve
     * @return true if id is not found
     * @throws SQLException
     */
    public boolean confirmNewID(String tableName, String itemId) throws SQLException {
    	Cursor mCursor = null;
    	boolean newId=false;
    	mCursor = mDb.query(true, tableName, new String[] {KEY_ROWID, KEY_ID}, KEY_ID + " like'" + itemId + "'", null, null, null, null, null);
    	if (mCursor.getCount()<1)
   		   newId=true;
    	mCursor.close();
    	return newId;
    }


    /**
     * @param tableName table to perform update on
     * @param rowName item to update
     * @param rowValue
     * @param fieldName field to update
     * @param fieldValue
     * @return true/false
     */
    public void insert(String tableName, String rowName,String rowValue, String fieldName, String fieldValue) {
        ContentValues args = new ContentValues();
        args.put(rowName, rowValue);
    	args.put(fieldName, fieldValue);
 
    	mDb.insert(tableName, null, args);
		//return mDb.update(tableName, args, rowName + "=" + rowValue, null) > 0;
    }
}
