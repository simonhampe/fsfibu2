package fs.fibu2.test.model;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.Account;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.BilancialInformation;
import fs.fibu2.view.model.BilancialMapping;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;


/**
 * This class tests the features of the classes {@link BilancialInformation} and {@link BilancialMapping}
 * @author Simon Hampe
 *
 */
public class BilancialTest {

	public static void main(String[] args) {
		//Basic init
		BasicConfigurator.configure();
		String basedir = "../";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		Locale.setDefault(Locale.GERMANY);
		
		//Create and test a bilancial information object
		BilancialInformation info = new BilancialInformation();
		printInformation(info);
		Entry e = new Entry("Bla",10,Currency.getInstance("EUR"),new GregorianCalendar(),Category.getCategory(new Vector<String>(Arrays.asList("Bla","Blu"))),
				"bank_account",null,null);
		info = info.increment(e);
		printInformation(info);
		Entry f = new Entry("Bla",10,Currency.getInstance("EUR"),new GregorianCalendar(),Category.getCategory(new Vector<String>(Arrays.asList("Bla","Bli"))),
				"cash_box",null,null);
		info = info.increment(f);
		printInformation(info);
		
		try {
			info = new BilancialInformation(info);
			printInformation(info);
			Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
			info = new BilancialInformation(j);
			printInformation(info);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private static void printInformation(BilancialInformation info) {
		System.out.println("Bilancial information: ");
		System.out.println(" Overall sum: " + info.getOverallSum());
		for(Account a : info.getAccountMappings().keySet()) {
			System.out.println(" " + a.getName() + ": " + info.getAccountMappings().get(a));
		}
		for(Category c : info.getCategoryMappings().keySet()) {
			System.out.println(" " + c.tail + ": " + info.getCategoryMappings().get(c));
		}
	}
	
}
