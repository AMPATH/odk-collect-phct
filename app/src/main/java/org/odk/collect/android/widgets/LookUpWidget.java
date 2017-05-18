package org.odk.collect.android.widgets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.logic.HCTSharedConstants;
import org.odk.collect.android.logic.PromptElement;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A widget for auto-complete lookup text.
 * 
 * @author Samuel Mbugua (sthaiya@gmail.com)
 */
public class LookUpWidget extends LinearLayout implements IQuestionWidget {

	private ArrayList<String>  villageAdapter,al;
	private TextView mStringAnswer;
	private AutoCompleteTextView mAutoCompleteView;
	
    public LookUpWidget(Context context) {
        super(context);
    }

    public void clearAnswer() {
    	mAutoCompleteView.setText(null);
    	mStringAnswer.setText(null);
    }


    public IAnswerData getAnswer() {
        String vil = mAutoCompleteView.getText().toString();
        String s=getVillageAnswer(vil);
        if (s == null || s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }


    public void buildView(PromptElement prompt) {
    	setOrientation(LinearLayout.VERTICAL);
        mAutoCompleteView=new AutoCompleteTextView(this.getContext());
        mStringAnswer = new TextView(getContext());
        mStringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_PT, GlobalConstants.APPLICATION_FONTSIZE);
        
        String s = (String) prompt.getAnswerObject();
        if (s != null) {
        	mAutoCompleteView.setText(s.substring(0, s.indexOf(",")));
        	getVillages();
        	setLocations(s.substring(0, s.indexOf(",")));
        }
        
        if (getVillages() != null){
        	ArrayAdapter<String>adapter =new ArrayAdapter<String>(mAutoCompleteView.getContext(),R.layout.simple_dropdown_hct,getVillages());
        	mAutoCompleteView.setAdapter(adapter);
        	mAutoCompleteView.setHint("Type 1st letter to get options");
        	mAutoCompleteView.setTextColor(Color.BLACK);
            mAutoCompleteView.setThreshold(1);
            mAutoCompleteView.setSingleLine();
            mAutoCompleteView.addTextChangedListener(textChecker);
            mAutoCompleteView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            	 public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
            		 setLocations(mAutoCompleteView.getText().toString().toUpperCase());
            	 }
            });
        }else {
        	mAutoCompleteView.setHint("Villages not Loaded");
        	mAutoCompleteView.setEnabled(false);
        }
       	
    	
       
        addView(mAutoCompleteView);
        
        //Create a read-only view for location, sub-location, division and district
        addView(mStringAnswer);
            
  
    }
    
	/**
     * Method to listen for changes in an editable
     * @returns a textwatcher
     */
    final TextWatcher textChecker = new TextWatcher() {
        public void afterTextChanged(Editable s) {}

        public void beforeTextChanged(CharSequence s, int start, int count,
                        int after) {}

        public void onTextChanged(CharSequence s, int start, int before,
                        int count) {
        	setLocations(mAutoCompleteView.getText().toString().toUpperCase());
        }
    };
    
    public String getVillageAnswer(String item){
    	if (al!=null){
    		int itemIndex=al.indexOf(item);
	    	if (itemIndex != -1)
	    		return villageAdapter.get(itemIndex);
    	}
    	return null;
  	
    }
    
    public void setLocations(String item){
    	String village="",sublocation="",location="",division="",district="";
    	if (al!=null){
	    	int itemIndex=al.indexOf(item);
	    	if (itemIndex != -1){
	    		village=villageAdapter.get(itemIndex);
		    	//First get the district
	    		if (village.indexOf(",") != village.lastIndexOf(",") && village.indexOf(",")!=-1)
	    			district = village.substring(village.lastIndexOf(",")+1);
		    	
	    		//then the division
	    		if (village.indexOf(",") != village.lastIndexOf(",") && village.indexOf(",")!=-1){
	    			village=village.substring(0,village.lastIndexOf(","));
	    			division = village.substring(village.lastIndexOf(",")+1);
	    		}
		    	
		    	//then the location
	    		if (village.indexOf(",") != village.lastIndexOf(",") && village.indexOf(",")!=-1){
	    			village=village.substring(0,village.lastIndexOf(","));
	    			location = village.substring(village.lastIndexOf(",")+1);
	    		}
		    	
		    	//then the sublocation
	    		if (village.indexOf(",") != village.lastIndexOf(",") && village.indexOf(",")!=-1){
	    			village=village.substring(0,village.lastIndexOf(","));
		    		sublocation = village.substring(village.lastIndexOf(",")+1);
	    		}
	    	}else {
	    		district=division=location=sublocation="";
	    	}
    	}
	    mStringAnswer.setText("Sublocation:- " + sublocation + "\nLocation:- " + location + 
	    						"\nDivision:- " + division + "\nDistrict:- " + district);
    }
    
    /**
     * Opens a csv file of format "[string],[string],[string],[string]" and
     * @return A string array of names of villages
     */
    public String [] getVillages() {
    	String villages [];
    	String filePath = HCTSharedConstants.SPECIAL_FILES_PATH + "villages.csv";
		//...check if villages file exists
		File villageFile=new File(filePath);
		if (!villageFile.exists())
			return null;			

		al = new ArrayList<String>();
		villageAdapter = new ArrayList<String>();
		String line = null;
		try {
			BufferedReader input =  new BufferedReader(new FileReader(villageFile));
			try {
				while (( line = input.readLine()) != null){
					line=line.toUpperCase();
					villageAdapter.add(line);
					if (line.indexOf(",")!=-1)
						al.add(line.substring(0,line.indexOf(",")));
					else
						al.add(line);
				}
			}
			finally {
				 input.close();
			 }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    villages = (String []) al.toArray (new String [al.size ()]);
		return villages;
    }


	public void setFocus(Context context) {
        // Put focus on text input field and display soft keyboard if appropriate.
        this.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(this, 0);
    }

}
