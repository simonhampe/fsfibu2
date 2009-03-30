package fs.fibu2.test.model;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import fs.fibu2.data.model.EntryValue;

/**
 * This class tests all the features of Entry and associated classes
 * @author Simon Hampe
 *
 */
public class EntryTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Create a few currency values
		EntryValue v1 = new EntryValue(0,Currency.getInstance("EUR"));
		System.out.println(v1);
	}

}
