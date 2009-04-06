package fs.fibu2.test.model;

import java.util.Arrays;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.data.model.AbstractAccount;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.xml.FsfwDefaultReference;

/**
 * Tests features of the entry and account classes
 * @author Simon Hampe
 *
 */
public class EntryTest {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		FsfwDefaultReference.setFsfwDirectory("/home/hampe/workspace/fsframework/");
		AbstractAccount account = new AbstractAccount();
		System.out.println(account);
		
		Entry e = new Entry("Getränkeeinnahmen", 140.00f, Currency.getInstance("EUR"),new GregorianCalendar(),
				Category.getCategory(new Vector<String>(Arrays.asList("Getr. & Süßes", "Einnahmen"))),
				new AbstractAccount(), null, "bla");
		
		
	}

}
