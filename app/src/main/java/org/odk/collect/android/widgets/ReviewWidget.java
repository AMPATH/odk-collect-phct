package org.odk.collect.android.widgets;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.javarosa.core.model.data.IAnswerData;
import org.odk.collect.android.logic.HCTSharedConstants;
import org.odk.collect.android.logic.PromptElement;

import android.content.Context;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Widget that allows user review important information marker as review;
 * 
 * @author Samuel Mbugua (sthaiya@gmail.com)
 */
public class ReviewWidget extends LinearLayout implements IQuestionWidget {


    private TextView mStringAnswer;

    public ReviewWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
    }


    public IAnswerData getAnswer() {
        return null;
     }


    public void buildView(PromptElement prompt) {
        this.setOrientation(LinearLayout.VERTICAL);
        String str="",yearOfBirth=null;
        mStringAnswer = new TextView(getContext());
        List<String> s = HCTSharedConstants.reviews;
        if (s != null){
	        if (!s.isEmpty()) {
	        	ListIterator<String> li=s.listIterator();
	        	String item;
	        	String[] vals;
	        	while (li.hasNext()) {
	        		item=li.next();
	        		vals=item.split(",");
	        		if (vals[0].equalsIgnoreCase("year of birth")){
	        			Double dbl= Double.valueOf(vals[1]);
	        			int ageInMonths=dbl.intValue();
	
	        			Calendar cal = Calendar.getInstance();
	        			cal.setTime(new Date());
	        			
	        			//convert age to days
	        			ageInMonths*=-30.4375;
	        			
	        			// add (actually subtract) number of days
	         			cal.add(Calendar.DATE ,ageInMonths);
	        			Date birthDate = cal.getTime();
	        			
	        			//format the date to meet our requirements,
	        			SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
	        			yearOfBirth = formatter.format(birthDate);
	        			str += vals[0] + " = " + yearOfBirth + "\n";
	        		}
	        		else {
	        			item=item.replace(",", " = ");
	        			str += item + "\n";
	        		}
	        	}
	        }
         	mStringAnswer.setText(str);
            mStringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
        }
        this.addView(mStringAnswer);
   }
    
    public void setFocus() {
        
    }


	public void setFocus(Context context) {
		
	}

} 