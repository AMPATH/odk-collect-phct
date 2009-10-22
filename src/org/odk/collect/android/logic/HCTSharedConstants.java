/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.logic;

import java.util.ArrayList;
import java.util.List;

import org.odk.collect.android.database.HCTDbAdapter;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;

/**
 * The constants used in AMPATH HCT Implementation.
 * 
 * @author Samuel Mbugua sthaiya@gmail.com
 * 
 */
public class HCTSharedConstants {

	public static List<String> reviews = null;
	public static List<String> tempDB = null;	
	//temporary storage for current entered ID to prevent double entry of IDs
	public static List<String> tempIDs;
	
	private static HCTDbAdapter mDbAdapter;

	public static final String HOUSEHOLD = "household";
	public static final String GIVENNAME = "givenname";
	public static final String MIDDLENAME = "middlename";
	public static final String FAMILYNAME = "familyname";
	public static final String INDIVIDUAL = "individual";
	public static final String HOUSEHEAD = "HeadID";
	public static final String SPECIAL_FILES_PATH = "/sdcard/odk/specialfiles/";
	public static final String UPLOADER_FILE = "ProcessFileUpload.jsp";

	public static String currentIndividual = null;
	public static String givenname=null;
	public static String middlename=null;
	public static String familyname=null;
	public static String householdId= null;
	public static String householdHeadId = null;

	public static Context dbCtx;
	public static Context alertCtx;


	//TODO variables for unique function 
	//Need a better way of doing this 
	public static boolean savedForm = false;
	public static String savedFormName = null;
	public static boolean finalizing = false;

	/**
	 * Saves entered IDs to database to avoid double entry
	 * 
	 * @param path
	 */
	public static void saveIDs() {
		String strTemp = "";
		saveNames();
		mDbAdapter = new HCTDbAdapter(dbCtx);
		mDbAdapter.open();
		// remove any duplicates, just in case 
		for (int i = 0; i < tempIDs.size(); i++) {
			if (strTemp == tempIDs.get(i))
				tempIDs.remove(i);
			else
				strTemp = tempIDs.get(i);
		}
		
		//and set household-id
		for (int i = 0; i < tempIDs.size(); i++) {
			String table = tempIDs.get(i).substring(0,tempIDs.get(i).indexOf(":"));
			String id = tempIDs.get(i).substring(tempIDs.get(i).indexOf(":") + 2);
			if (table.equals(HOUSEHOLD))
				strTemp = id;
		}

		// And then write the id's into the database.
		for (int i = 0; i < tempIDs.size(); i++) {
			String table = tempIDs.get(i).substring(0,tempIDs.get(i).indexOf(":"));
			String id = tempIDs.get(i).substring(tempIDs.get(i).indexOf(":") + 2);

			if (table.equals(HOUSEHOLD)) {
				if (mDbAdapter.confirmNewID(table,id))
					mDbAdapter.insertHousehold(table, id, householdHeadId, null); 
			}

			if (table.equals(INDIVIDUAL)) {
				String name=null;
				//get individual names
				for (int j = 0; j < tempDB.size(); j++) {
					if (tempDB.get(j).startsWith(id))
						name=tempDB.get(j).substring(tempDB.get(j).indexOf(" "));		
				}
				mDbAdapter.insertIndividual(table, id, strTemp,name);
			}
		}
		mDbAdapter.close();
	}
	
	public static void saveNames() {
		String nameTemplate="";
		if (currentIndividual != null && currentIndividual != "") {
			nameTemplate=currentIndividual.substring(currentIndividual.indexOf(" ")+1);
			if (givenname != null && givenname != "")
				nameTemplate=nameTemplate + " " + givenname;
			if (middlename != null && middlename != "")
				nameTemplate=nameTemplate + " " + middlename;
			if (familyname != null && familyname != "")
				nameTemplate=nameTemplate + " " + familyname;
			if (tempDB==null)tempDB=new ArrayList<String>();
			tempDB.add(nameTemplate);
			familyname=givenname=middlename=null;
		}
	}
	
	public static void cleanUp(){
		if (tempIDs != null) tempIDs.clear();
		if (tempDB != null) tempDB.clear();
		if (reviews != null) reviews.clear();
		currentIndividual=householdHeadId=givenname=
		middlename=familyname=householdId=	savedFormName=null;
		
		savedForm=finalizing=false;
	}
	
	/**
	 * @return String[], all persons IDs in this household
	 */
	public static String getPeopleInHousehold() {
		// Get all the people in specified household
		String household_id = householdId==null?null:householdId.substring(householdId.indexOf(":") + 1);
		String hctIDs;
		HCTDbAdapter mDbAdapter = new HCTDbAdapter(dbCtx);
		mDbAdapter.open();
		Cursor mIDCursor = mDbAdapter.getHouseholdPersons(household_id.trim());
		mDbAdapter.close();
		ListActivity lstActivity = new ListActivity();
		lstActivity.startManagingCursor(mIDCursor);
		if (mIDCursor!=null){
			SQLiteCursor liteCursor = (SQLiteCursor) mIDCursor;
			CursorWindow cw = new CursorWindow(true);
			liteCursor.fillWindow(0, cw);
			hctIDs="";
			for (int i = 0; i < cw.getNumRows(); i++) {
				hctIDs = hctIDs + cw.getString(i, 1) + ":  " + cw.getString(i, 2) + "\n";
			}
		}else 
			hctIDs="No persons in household";
		return hctIDs;
	}
}
