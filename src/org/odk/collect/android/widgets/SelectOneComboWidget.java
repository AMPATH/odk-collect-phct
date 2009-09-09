package org.odk.collect.android.widgets;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.util.OrderedHashtable;
import org.odk.collect.android.PromptElement;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * SelectOneComboWidgets handles select-one fields using combo buttons.
 * 
 * @author Samuel Mbugua (sthaiya@gmail.com)
 */

public class SelectOneComboWidget extends Spinner implements IQuestionWidget {

    OrderedHashtable mItems;


    public SelectOneComboWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
        this.clearAnimation();
    }


    public IAnswerData getAnswer() {
        int i = this.getSelectedItemPosition() + 1;
        if (i == -1) {
            return null;
        } else {
            String s = (String) mItems.elementAt(i - 1);
            return new SelectOneData(new Selection(s));
        }
    }


    @SuppressWarnings("unchecked")
	public void buildView(final PromptElement prompt) {
        mItems = prompt.getSelectItems();

        String s = null;
        if (prompt.getAnswerValue() != null) {
            s = ((Selection) prompt.getAnswerObject()).getValue();
        }

        if (prompt.getSelectItems() != null) {
            OrderedHashtable h = prompt.getSelectItems();
            Enumeration e = h.keys();
            String k = null;

            //Create spinner
            List<String> lst=new ArrayList<String>();
            int selectedItem=0;
            while (e.hasMoreElements()) {
            	k = (String) e.nextElement();
                lst.add(k);
                if (k.equals(s)&& s!=null)
                	selectedItem=lst.indexOf(k);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_dropdown_item_1line, lst);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.setAdapter(adapter);
            this.setSelection(selectedItem);
            this.requestFocus();

        }
    }
    
    public void setFocus() {
        
    }

}
