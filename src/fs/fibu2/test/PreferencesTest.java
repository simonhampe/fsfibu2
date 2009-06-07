package fs.fibu2.test;

import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.prefs.Preferences;

import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.view.model.TableModelComparator;

/**
 * Theoretically I wrote this class to test the {@link Preferences} class - but didn't really need it, so I'm using it for small one-time tests.
 * @author Simon Hampe
 *
 */
public class PreferencesTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Entry e = new Entry("bla",2,Currency.getInstance("EUR"),new GregorianCalendar(),Category.getRootCategory(),"cash_box",null,"bla");
		Entry f = new Entry("blu2",2,Currency.getInstance("EUR"),new GregorianCalendar(),Category.getRootCategory(),"cash_box",null,"bla");
		System.out.println((new TableModelComparator()).compare(e,f));
		
	}

}
