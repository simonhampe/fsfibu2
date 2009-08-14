package fs.fibu2.test.print;

import java.io.File;
import java.util.Arrays;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JDialog;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.print.BilancialPrintDialog;
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
			BilancialTreeModel model = new BilancialTreeModel(j,null,null);
			BilancialPrintDialog diag = new BilancialPrintDialog(model);
			diag.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			diag.setVisible(true);
//			PrinterJob job = PrinterJob.getPrinterJob();
//			HashPrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
//			job.printDialog(set);
//			PageFormat format = job.getPageFormat(set);	
//			
//			model.setMask(Category.getCategory(Category.getRootCategory(), "Fachschaft"), "hurraaa");
//			BilancialPrintConfiguration config = new BilancialPrintConfiguration(11,"Bilanz 2009",model,job,format,PrintPolicy.PRESERVE_UNIT);
//			job.setPageable(new BilancialPageable(config));
//			job.print(set);
//			System.out.println(job.getPrintService().getName());
		}
		catch(Exception e) {
			System.out.println("AAAH");
			e.printStackTrace();
		}
	}

}
