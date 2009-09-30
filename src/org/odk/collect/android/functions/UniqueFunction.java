package org.odk.collect.android.functions;

import java.util.Vector;

import org.javarosa.core.model.condition.IFunctionHandler;
import org.odk.collect.android.database.HCTDbAdapter;
import org.odk.collect.android.logic.HCTSharedConstants;

/**
 * Looks for a "unique" call in XForm and return true or false for a unique entry
 * 
 * @author Samuel Mbugua (sthaiya@gmail.com)
 */

public class UniqueFunction implements IFunctionHandler {
	private static HCTDbAdapter mDbAdapter;
	
	public Object eval(Object[] args) {

		String fieldName = (String) args[0];
		String fieldValue = (String) args[1];

		return confirmNewID(fieldName, fieldValue);
	}

	public String getName() {
		return "unique";
	}

	@SuppressWarnings("unchecked")
	public Vector getPrototypes() {

		Class[] prototypes = { String.class, String.class };
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
    
    private  boolean confirmNewID(String idType, String id){
    	String fullID=idType + "," + id;
    	
    	//TODO: Need a better way of doing this two checks
    	
    	//A saved form: No need checking
    	if (HCTSharedConstants.savedForm) {
    		if (idType.equals(HCTSharedConstants.INDIVIDUAL))
				HCTSharedConstants.currentIndividual=fullID;
			if (idType.equals(HCTSharedConstants.HOUSEHOLD))
				HCTSharedConstants.householdId="Household: " + id;
    		return true;
    	}
    		
    	
    	//Finalizing a form: No need checking
    	if (HCTSharedConstants.finalizing)
    		return true;
    	
    	mDbAdapter=new HCTDbAdapter(HCTSharedConstants.dbCtx);
		mDbAdapter.open();

		// Confirm if this is a new id
		// if new id: push into temp and if individual: set as current individual
		if (mDbAdapter.confirmNewID(idType,id) && !inTemp(fullID) && !editingID(fullID)){
			HCTSharedConstants.tempIDs.add(fullID);
			mDbAdapter.close();
			
			if (idType.equals(HCTSharedConstants.INDIVIDUAL))
				HCTSharedConstants.currentIndividual=fullID;
			if (idType.equals(HCTSharedConstants.HOUSEHOLD))
				HCTSharedConstants.householdId="Household: " + id;
			return true;
		}
		
		/* An already existing ID in temp, this means its probably an edit
		 * Clear the id from temp since save will store it again
		 */
		else if (editingID(fullID)) {
			if (inTemp(fullID))
				HCTSharedConstants.tempIDs.remove(HCTSharedConstants.tempIDs.indexOf(fullID));
			HCTSharedConstants.currentIndividual=null;
			mDbAdapter.close();
			return true;
		}
	
		mDbAdapter.close();
		return false;
    }
    
    /**
     * @param id
     * @return
     */
    private boolean editingID(String id){
    	if (HCTSharedConstants.currentIndividual != null && 
    			HCTSharedConstants.currentIndividual.equalsIgnoreCase(id))
    		return true;
    	
    	return false;
    }
    
    /**
     * @param id
     * @return
     */
    private boolean inTemp(String id){
    	if (HCTSharedConstants.tempIDs.contains(id))
    		return true;
    	
    	return false;
    }
}
