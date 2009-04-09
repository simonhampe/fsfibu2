package fs.fibu2.test.model;

import java.util.Arrays;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.dom4j.tree.DefaultDocument;

import fs.fibu2.data.error.EntryVerificationException;
import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.data.model.AbstractAccount;
import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLDirectoryTree;
import fs.xml.XMLToolbox;

/**
 * Tests features of the entry and account classes
 * @author Simon Hampe
 *
 */
public class EntryTest {

	public static void main(String[] args) {
		try {
		BasicConfigurator.configure();
		FsfwDefaultReference.setFsfwDirectory("/home/talio/eclipse/workspace/fsframework/");
		AbstractAccount account = new AbstractAccount();
		System.out.println(account);
		
		HashMap<String,String> ai = new HashMap<String, String>();
		ai.put("invoice", "G 17");
		Entry e = new Entry("Getränkeeinnahmen", -140.00f, Currency.getInstance("EUR"),new GregorianCalendar(2009,0,1),
				Category.getCategory(new Vector<String>(Arrays.asList("Getr. & Süßes", "Einnahmen"))),
				new AbstractAccount(), ai, "bla");
		Entry f = new Entry(e.getConfiguration());
		System.out.println(Fsfibu2DateFormats.getEntryDateFormat().format(f.getDate().getTime()));
		DefaultDocument doc = new DefaultDocument();
		doc.setRootElement(e.getConfiguration());
		System.out.println(XMLToolbox.getDocumentAsPrettyString(doc));
		System.out.println(f);
		
		//f.getAccount().verifyEntry(f);
		
		}
//		catch(EntryVerificationException ve) {
//			System.out.println("Faulty entry: ");
//			System.out.println(ve.getListOfFaultyFields());
//			System.out.println(ve.getFaultDescriptions());
//			System.out.println(ve.getListOfCriticality());
//		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
