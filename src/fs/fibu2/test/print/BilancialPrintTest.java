package fs.fibu2.test.print;

import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.Arrays;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Vector;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttribute;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.print.BilancialPageable;
import fs.fibu2.print.BilancialPrintConfiguration;
import fs.fibu2.print.BilancialPrintConfiguration.PrintPolicy;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.BilancialTreeModel;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

public class BilancialPrintTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "../";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		Locale.setDefault(Locale.GERMANY);
		
		try {
			final Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
			j.addEntry(new Entry("bla",34,Currency.getInstance("EUR"),new GregorianCalendar(),
					Category.getCategory(new Vector<String>(Arrays.asList("Fachschaft","Spiele","Mehrspiele"))),"bank_account",null,null));
			j.addEntry(new Entry("bla",3,Currency.getInstance("EUR"),new GregorianCalendar(),Category.getRootCategory()//Category.getCategory(Category.getRootCategory(), "Fachschaft")
					,"bank_account",null,null));
			PrinterJob job = PrinterJob.getPrinterJob();
			HashPrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
//			PageFormat format = job.getPageFormat(set);
			job.printDialog();
			BilancialTreeModel model = new BilancialTreeModel(j,null,null);
			model.setMask(Category.getCategory(Category.getRootCategory(), "Fachschaft"), "hurraaa");
			BilancialPrintConfiguration config = new BilancialPrintConfiguration(11,"Bilanz 2009",model,job,job.defaultPage(),PrintPolicy.NO_CONSTRAINT);
			job.setPageable(new BilancialPageable(config));
			job.print();
		}
		catch(Exception e) {
			System.out.println("AAAH");
			e.printStackTrace();
		}
	}

}
