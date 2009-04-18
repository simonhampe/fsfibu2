package fs.fibu2.test.model;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.dom4j.tree.DefaultDocument;

import fs.fibu2.data.model.AbstractAccount;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.xml.FsfwDefaultReference;
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
		Account bA = AccountLoader.getAccount("bank_account");
		System.out.println(bA);
		Account cb = AccountLoader.getAccount("cash_box");
		System.out.println(cb);
		URL[] path =  { new URL("file:///home/hampe/workspace/fsfibu2/bin/fs/fibu2/examples/")};
		URLClassLoader loader = new URLClassLoader(path);
		Class<?> c = loader.loadClass("fs.fibu2.examples.SlushFund");
		AccountLoader.loadAccount(c);
		Account sf = AccountLoader.getAccount("slush_fund");
		System.out.println(sf);
		
		HashMap<String,String> ai = new HashMap<String, String>();
		ai.put("invoice", "G 17");
		ai.put("statement","4");
		Entry e = new Entry("<Getränkeeinnahmen>", -140.00f, Currency.getInstance("EUR"),new GregorianCalendar(2009,0,1),
				Category.getCategory(new Vector<String>(Arrays.asList("Getr. & Süßes", "Einnahmen"))),
				"bank_account", ai, "bla");
		Entry f = new Entry(e.getConfiguration());
		DefaultDocument doc = new DefaultDocument();
		doc.setRootElement(e.getConfiguration());
		System.out.println(XMLToolbox.getDocumentAsPrettyString(doc));
		System.out.println(f);
		
		sf.verifyEntry(f);
		
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
