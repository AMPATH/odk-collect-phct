package org.odk.collect.android.functions;

import java.util.ArrayList;
import java.util.Vector;

import org.javarosa.core.model.condition.IFunctionHandler;
import org.odk.collect.android.database.HCTDbAdapter;
import org.odk.collect.android.logic.HCTSharedConstants;

import android.app.ListActivity;
import android.database.Cursor;

/**
 * Looks for a "store" call in XForm stores value in DB.
 * 
 * @author Samuel Mbugua (sthaiya@gmail.com)
 */

public class StoreValueFunction extends ListActivity implements IFunctionHandler{
	private static HCTDbAdapter mDbAdapter;
	
	public Object eval(Object[] args) {

		String tableName = (String) args[0];
		String id = (String) args [1];
		String fieldName = (String) args[2];
		String fieldValue = (String) args[3];
		String extra = (String) args[4];

		return storeValue(tableName, id, fieldName, fieldValue, extra);
	}

	public String getName() {
		return "store";
	}

	@SuppressWarnings("unchecked")
	public Vector getPrototypes() {
		Class[] prototypes = { String.class, String.class, String.class, String.class, String.class };
		Vector v = new Vector();
		v.add(prototypes);
		return v;
	}

	public boolean rawArgs() {
		// Auto-generated method stub
		return false;
	}

	public boolean realTime() {
		// Auto-generated method stub
		return false;
	}
    
    private  boolean storeValue(String tableName, String id, String fieldName, String fieldValue, String extra){
    	String item=tableName + ":" + id;
    	Cursor mCursor;
    	mDbAdapter=new HCTDbAdapter(HCTSharedConstants.dbCtx);
		mDbAdapter.open();
		mCursor = mDbAdapter.getAnyField(tableName, id, "id");

		if (mCursor==null){ //household or individual doesn't exist in Db (new entry)
			
			if (fieldName.equalsIgnoreCase(HCTSharedConstants.HOUSEHEAD)) {
				if (fieldValue.equalsIgnoreCase("self")) {
					mDbAdapter.insert(tableName, "id", id, fieldName, fieldValue);
					
					//store in temporary area
					if (HCTSharedConstants.tempDB== null) HCTSharedConstants.tempDB=new ArrayList<String>();
					HCTSharedConstants.tempDB.add(item);
				}
			}
			/* else
				mDbAdapter.updateField(tableName, "id", id, fieldName, fieldValue);*/
		}
		else {
			mCursor.close();
			if (fieldName.equalsIgnoreCase(HCTSharedConstants.HOUSEHEAD)) {
				if (fieldValue.equalsIgnoreCase("self")) {
					mCursor = mDbAdapter.getAnyField(tableName, id, fieldName);
					if (mCursor != null) {
						startManagingCursor(mCursor);
						String[] from = new String[] { HCTDbAdapter.KEY_HOUSEHOLD_HEAD };
						if (!from[0].equals(extra))
							return false;
					}
				}
			}
		}
 	
    	return true;
    }
    
    @SuppressWarnings("unused")
	private boolean inTemp(String item){
    	for (int i=0;i<HCTSharedConstants.tempDB.size();i++){
    		if (HCTSharedConstants.tempDB.get(i).equalsIgnoreCase(item))
    			return true;
    	}
		return false;
    }

}
