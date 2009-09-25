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

import java.util.List;

import org.odk.collect.android.database.HCTDbAdapter;

import android.content.Context;

/**
 * The constants used in AMPATH HCT Implementation.
 * 
 * @author Samuel Mbugua sthaiya@gmail.com
 * 
 */
public class HCTSharedConstants {

	public static List<String> reviews = null;
	public static List<String> tempDB = null;
	private static HCTDbAdapter mDbAdapter;

	public static final String HOUSEHOLD = "household";
	public static final String INDIVIDUAL = "individual";
	public static final String HOUSEHEAD = "HeadID";

	/**
	 * temporary storage for current entered ID to prevent double entry of IDs
	 */
	public static List<String> tempIDs;
	public static String currentIndividual = null;
	public static String householdHeadId = null;

	public static Context dbCtx;
	public static Context alertCtx;

	/**
	 * Special files storage path
	 */
	public static final String SPECIAL_FILES_PATH = "/sdcard/odk/specialfiles/";

	// TODO: silly hack
	public static boolean savedForm = false;

	/**
	 * Saves entered IDs to database to avoid double entry
	 * 
	 * @param path
	 */
	public static void saveIDs(String path) {
		String strTemp = "";
		mDbAdapter = new HCTDbAdapter(dbCtx);
		mDbAdapter.open();
		// remove any duplicates, just in case
		for (int i = 0; i < tempIDs.size(); i++) {
			if (strTemp == tempIDs.get(i))
				tempIDs.remove(i);
			else
				strTemp = tempIDs.get(i);
		}

		// And then write the ids into the database.
		for (int i = 0; i < tempIDs.size(); i++) {
			String table = tempIDs.get(i).substring(0,
					tempIDs.get(i).indexOf(","));
			String id = tempIDs.get(i).substring(
					tempIDs.get(i).indexOf(",") + 1);

			if (table.equals(HOUSEHOLD)) {
				strTemp = id;
				mDbAdapter.insertID(table, id, householdHeadId, path, null); 
			}

			if (table.equals(INDIVIDUAL))
				mDbAdapter.insertID(table, id, strTemp);
		}
		
		if (reviews != null)
			reviews.clear();
		mDbAdapter.close();
	}
}
