package org.odk.collect.android;

import java.util.Vector;

import org.javarosa.core.model.condition.IFunctionHandler;

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
    	
    	//BUG, this is broken. so we hack around
    	if (true)
    	    return true;
    	
    	//TODO: Need a better way of doing this
    	//A saved form: No need checking
    	if (SharedConstants.savedForm)
    		return true;
    	
    	mDbAdapter=new HCTDbAdapter(SharedConstants.dbCtx);
		mDbAdapter.open();

		// Confirm if this is a new id
		// if new id: push into temp and if individual: set as current individual
		if (mDbAdapter.confirmNewID(idType,id) && confirmNewID(fullID) && !editingID(fullID)){
			SharedConstants.tempIDs.add(fullID);
			mDbAdapter.close();
			
			if (idType.equals(SharedConstants.INDIVIDUAL))
				SharedConstants.currentIndividual=fullID;
			return true;
		}
		
		/* An already existing ID in temp, this means its probably an edit
		 * Clear the id from temp since save will store it again
		 */
		else if (editingID(fullID)) {
			System.out.println("editing id");
			SharedConstants.tempIDs.remove(SharedConstants.tempIDs.indexOf(fullID));
			SharedConstants.currentIndividual=null;
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

    	if (SharedConstants.currentIndividual==id)
    		return true;
    	
    	return false;
    }
    
    /**
     * @param id
     * @return
     */
    private boolean confirmNewID(String id){

    	if (SharedConstants.tempIDs.contains(id))
    		return false;
    	
    	return true;
    }
}
