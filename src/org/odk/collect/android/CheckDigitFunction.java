package org.odk.collect.android;

import org.javarosa.core.model.condition.IFunctionHandler;
import java.util.Vector;

/**
 * Looks for a "checkdigit" call in XForm and return true or false for a correct check-digit
 * 
 * @author Samuel Mbugua (sthaiya@gmail.com)
 */

public class CheckDigitFunction implements IFunctionHandler {

	public Object eval(Object[] args) {

		String id = (String) args[0];

		return checkDigit(id.trim());
	}

	public String getName() {
		return "checkdigit";
	}

	@SuppressWarnings("unchecked")
	public Vector getPrototypes() {

		Class[] prototypes = { String.class };
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

	/**
	 * This method is borrowed from OpenMRS. It generates
	 * check digits for allowed alpha-numeric characters and uses them to validate 
	 * entered IDs
	 * 
	 * @param idWithCheckdigit
	 * @return true if the checkDigit is right otherwise false
	 */
	private boolean checkDigit(String idWithCheckdigit) {
		int checkDigit;
		//extract the provided check digit
		try {
			checkDigit = Integer.parseInt(idWithCheckdigit.substring(idWithCheckdigit.length() - 1));
		}
		catch (NumberFormatException e) {
			return false;
		}
		
		//remove a '-' if part of id
		if (idWithCheckdigit.lastIndexOf("-") != -1)
			idWithCheckdigit = idWithCheckdigit.substring(0, idWithCheckdigit.lastIndexOf("-")) + checkDigit;
		
		//trim the checkdigit from the id
		String idWithoutCheckdigit = idWithCheckdigit.substring(0, idWithCheckdigit.length() - 1);
		
		// remove leading or trailing whitespace, convert to upper-case
		idWithoutCheckdigit = idWithoutCheckdigit.trim().toUpperCase();

		// this will be a running total
		int sum = 0;

		// loop through digits from right to left
		for (int i = 0; i < idWithoutCheckdigit.length(); i++) {

			// set ch to "current" character to be processed
			char ch = idWithoutCheckdigit.charAt(idWithoutCheckdigit.length()
					- i - 1);

			// our "digit" is calculated using ASCII value - 48
			int digit = (int) ch - 48;

			// weight will be the current digit's contribution to
			// the running total
			int weight;
			if (i % 2 == 0) {

				// for alternating digits starting with the rightmost, we
				// use our formula this is the same as multiplying x 2 and
				// adding digits together for values 0 to 9. Using the
				// following formula allows us to gracefully calculate a
				// weight for non-numeric "digits" as well (from their
				// ASCII value - 48).
				weight = (2 * digit) - (int) (digit / 5) * 9;

			} else {

				// even-positioned digits just contribute their ascii
				// value minus 48
				weight = digit;

			}

			// keep a running total of weights
			sum += weight;

		}

		// avoid sum less than 10 (if characters below "0" allowed,
		// this could happen)
		sum = Math.abs(sum) + 10;

		// check digit is amount needed to reach next number
		// divisible by ten
		return checkDigit == (10 - (sum % 10)) % 10;
	}

}
