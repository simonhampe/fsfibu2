package fs.fibu2.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JTable;

import org.dom4j.tree.DefaultDocument;

import fs.fibu2.data.format.Fsfibu2DateFormats;
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
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy");
		try {
			Date d = format.parse("13.7.89");
			System.out.println(Fsfibu2DateFormats.getEntryDateFormat().format(d));
			d = Fsfibu2DateFormats.getEntryDateFormat().parse("13.7.09");
			System.out.print(Fsfibu2DateFormats.getEntryDateFormat().format(d));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
