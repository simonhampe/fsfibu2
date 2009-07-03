package fs.fibu2.test;

import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dom4j.tree.DefaultDocument;

import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLToolbox;

/**
 * Theoretically I wrote this class to test the {@link Preferences} class - but didn't really need it, so I'm using it for small one-time tests.
 * @author Simon Hampe
 *
 */
public class PreferencesTest {

	/**
	 * @param args
	 * @throws BackingStoreException 
	 */
	public static void main(String[] args) throws BackingStoreException {
		Journal j = new Journal();
		j.setName("bla");
		j.setDescription("blabla");
		HashMap<String, String> accInf = new HashMap<String, String>();
			accInf.put("invoice", "F 7");
		j.addEntry(new Entry("Name",13,Currency.getInstance("EUR"),new GregorianCalendar(),Category.getRootCategory(),"bank_account",accInf,"jaja"));
		j.addReadingPoint(new ReadingPoint("rp",new GregorianCalendar()));
		DefaultDocument doc = new DefaultDocument();
		try {
			doc.setRootElement(j.getConfiguration());
			XMLToolbox.saveXML(doc, "test.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
