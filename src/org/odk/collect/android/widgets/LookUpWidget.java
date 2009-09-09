package org.odk.collect.android.widgets;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.HCTDbAdapter;
import org.odk.collect.android.PromptElement;
import org.odk.collect.android.R;
import org.odk.collect.android.SharedConstants;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A widget that for auto-complete text.
 * 
 * @author Samuel Mbugua (sthaiya@gmail.com)
 */
public class LookUpWidget extends LinearLayout implements IQuestionWidget {

	private AutoCompleteTextView mAutoCompleteView;
	private TextView mStringAnswer;
	private HCTDbAdapter mDbAdapter;
	private Cursor mIDCursor;
	private ListActivity lstActivity;

	public LookUpWidget(Context context) {
		super(context);
	}

	public void clearAnswer() {
		mAutoCompleteView.setText(null);
		mStringAnswer.setText(null);
	}
	

	public IAnswerData getAnswer() {
		String s = mAutoCompleteView.getText().toString();
		if (s == null || s.equals("")) {
			return null;
		} else {
			mDbAdapter.open();
			if (mDbAdapter.confirmNewID(SharedConstants.HOUSEHOLD, s)) {
				mDbAdapter.close();
				return null;
			} else {
				mDbAdapter.close();
				String fullID=SharedConstants.REVISIT + "," + s;
				
				for (int i=1;i<SharedConstants.tempIDs.size();i++){
		    		if (SharedConstants.tempIDs.get(i).startsWith(SharedConstants.REVISIT))
		    			SharedConstants.tempIDs.remove(i);  			
		    	}
				SharedConstants.tempIDs.add(fullID);
				
				return new StringData(s);
			}

		}
	}
	

	public void buildView(PromptElement prompt) {
		setOrientation(LinearLayout.VERTICAL);
		mAutoCompleteView = new AutoCompleteTextView(this.getContext());
		mStringAnswer = new TextView(getContext());
		mStringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_PT,8);

		String s = prompt.getAnswerText();
		if (s != null){
			mAutoCompleteView.setText(s);
			getPeopleInHousehold(s);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mAutoCompleteView.getContext(), R.layout.simple_dropdown_hct,getHouseholdIDs());
		mAutoCompleteView.setTextColor(Color.BLACK);
		mAutoCompleteView.setAdapter(adapter);
		mAutoCompleteView.setThreshold(1);
		mAutoCompleteView.setSingleLine();
		mAutoCompleteView.setHint("Type 1st letter for options");
		mAutoCompleteView.addTextChangedListener(textChecker);
		mAutoCompleteView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
       	 public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
       		 getPeopleInHousehold(mAutoCompleteView.getText().toString().toUpperCase());
       	 }
       });

		addView(mAutoCompleteView);

		// Create a read-only view for existing HCTIDs
		addView(mStringAnswer);
	}

	/**
	 * @return String[], all household IDs captured by this device
	 */
	private String[] getHouseholdIDs() {
		// Get all of the rows from the database specified table
		String[] allIDs;
		mDbAdapter = new HCTDbAdapter(SharedConstants.dbCtx);
		mDbAdapter.open();
		mIDCursor = mDbAdapter.getAllIDs();
		mDbAdapter.close();
		lstActivity = new ListActivity();
		lstActivity.startManagingCursor(mIDCursor);
		
		if (mIDCursor!=null){
			SQLiteCursor liteCursor = (SQLiteCursor) mIDCursor;
			CursorWindow cw = new CursorWindow(true);
			liteCursor.fillWindow(0, cw);
			allIDs = new String[cw.getNumRows()];
			for (int i = 0; i < cw.getNumRows(); i++) {
				allIDs[i] = cw.getString(i, 1);
			}
		}else{
			allIDs=new String[1];
			allIDs[0]="";
			mStringAnswer.setText("No Households");
			mAutoCompleteView.setEnabled(false);
		}
			

		return allIDs;
	}
	
	/**
	 * @return String[], all household IDs captured by this device
	 */
	private void getPeopleInHousehold(String household_id) {
		// Get all the people in specified household
		String[] hctIDs;
		mDbAdapter = new HCTDbAdapter(SharedConstants.dbCtx);
		mDbAdapter.open();
		mIDCursor = mDbAdapter.getHCTIDs(household_id);
		mDbAdapter.close();
		lstActivity = new ListActivity();
		lstActivity.startManagingCursor(mIDCursor);
		if (mIDCursor!=null){
			SQLiteCursor liteCursor = (SQLiteCursor) mIDCursor;
			CursorWindow cw = new CursorWindow(true);
			liteCursor.fillWindow(0, cw);
			hctIDs = new String[cw.getNumRows()];
			for (int i = 0; i < cw.getNumRows(); i++) {
				hctIDs[i] = cw.getString(i, 1);
			}
			populateHCTIds(hctIDs);
		}else {
			mStringAnswer.setText("No persons in household");
		}
		

	}

	
	/**
	 * Method to listen for changes in an editable
	 * 
	 * @returns a text-watcher
	 */
	final TextWatcher textChecker = new TextWatcher() {
		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			getPeopleInHousehold(mAutoCompleteView.getText().toString().toUpperCase());
		}
	};

	public void populateHCTIds(String[] hctList) {
		String ids="People in Household: \n";
		for (int i = 0; i < hctList.length; i++) {
			ids+=hctList[i]+"\n";
		}
		mStringAnswer.setText(ids);
	}
	
    public void setFocus() {
        
    }

}
