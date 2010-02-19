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
	
	private boolean confirmNewIndividual(String idType, String id){
		String fullId=idType + ": " + id;
		if (HCTSharedConstants.currentIndividual==null){
			//  check a saved form
			if (HCTSharedConstants.savedForm) {
				// ensure id is not changed 
				if (mDbAdapter.confirmNewID(idType,id))
					return false;
				else
					return true;
			}
			//A new individual being created
			if (mDbAdapter.confirmNewID(idType,id) && !inTemp(fullId)){
				HCTSharedConstants.tempIDs.add(fullId);
				HCTSharedConstants.currentIndividual=fullId;
				return true;
			}
		}
		else {
			//probably a swipe back
			HCTSharedConstants.tempIDs.remove(HCTSharedConstants.currentIndividual);
			HCTSharedConstants.tempIDs.add(fullId);
			HCTSharedConstants.currentIndividual=fullId;
			return true;
		}
		return false;
	}
	
	private boolean confirmNewHousehold(String idType, String id){
		boolean unique=false;
		String fullId=idType + ": " + id;
		
		//if a saved form then current household is one saved in database
		if (HCTSharedConstants.savedFormName != null && HCTSharedConstants.householdId ==null)
			HCTSharedConstants.householdId=HCTSharedConstants.savedFormName;
		
		if (HCTSharedConstants.householdId==null){
			//A new household being created
			if (mDbAdapter.confirmNewID(idType,id) && !inTemp(fullId)){
				HCTSharedConstants.tempIDs.add(fullId);
				HCTSharedConstants.householdId=fullId;
				unique= true;
			}
		}
		else {
			//probably a swipe back
			HCTSharedConstants.tempIDs.remove(HCTSharedConstants.householdId);
			HCTSharedConstants.tempIDs.add(fullId);
			HCTSharedConstants.householdId=fullId;
			unique=true;
		}
		if (HCTSharedConstants.savedForm && HCTSharedConstants.savedFormName != null){
			if (!HCTSharedConstants.savedFormName.equalsIgnoreCase(fullId)){
				String table = HCTSharedConstants.savedFormName.substring(0, HCTSharedConstants.savedFormName.indexOf(":"));
				String idNum = HCTSharedConstants.savedFormName.substring(HCTSharedConstants.savedFormName.indexOf(":") + 2);
				if (mDbAdapter.deleteID(table, idNum))
					unique=true;
			}else
				unique=true;
		}
		return unique;
	}
	
    private  boolean confirmNewID(String idType, String id){
    	boolean isNew=false;
    	//Finalizing a form
    	if (HCTSharedConstants.finalizing) {
    		if (HCTSharedConstants.savedForm && idType.equalsIgnoreCase(HCTSharedConstants.HOUSEHOLD)
    				&& (HCTSharedConstants.householdId==null || HCTSharedConstants.householdId.trim()==""))
    			isNew = confirmNewHousehold(idType, id);
    		else
    			isNew = true;
    	}
    		
    	
    	//Initialize database connection
    	mDbAdapter=new HCTDbAdapter(HCTSharedConstants.dbCtx);
		mDbAdapter.open();
		
		//EITHER: confirm new household
		if (idType.equals(HCTSharedConstants.HOUSEHOLD))
			isNew = confirmNewHousehold(idType, id);
		
		//OR: confirm new individual
		if (idType.equals(HCTSharedConstants.INDIVIDUAL))
			isNew = confirmNewIndividual(idType, id);
    	
		mDbAdapter.close();
		return isNew;
    }
    
    private boolean inTemp(String id){
    	if (HCTSharedConstants.tempIDs.contains(id))
    		return true;
    	
    	return false;
    }
}