package fs.fibu2.test.model;

import java.io.File;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.data.model.ReadingPoint;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.event.YearSeparatorListener;
import fs.fibu2.view.model.YearSeparators;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the {@link YearSeparators} class
 * @author Simon Hampe
 *
 */
public class YearsSeparatorTest {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "/home/talio/eclipse/workspace/";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		
		try {
			Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
			final YearSeparators ys = YearSeparators.getInstance(j);
			ys.addYearSeparatorListener(new YearSeparatorListener() {
				@Override
				public void separatorAdded(Journal source,
						ReadingPoint yearSeparator) {
					printList(ys.getNecessarySeparators());
				}
				@Override
				public void separatorRemoved(Journal source,
						ReadingPoint yearSeparator) {
					printList(ys.getNecessarySeparators());
				}
			});
			printList(ys.getNecessarySeparators());
			Entry e = new Entry("",0,Currency.getInstance("EUR"),new GregorianCalendar(2007,0,3),Category.getRootCategory(),"cash_box",null,"");
			Entry f = new Entry("",0,Currency.getInstance("EUR"),new GregorianCalendar(2011,0,3),Category.getRootCategory(),"cash_box",null,"");
			j.addEntry(e);
			j.addEntry(f);
			j.removeEntry(e);
			j.removeEntry(f);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void printList(Vector<ReadingPoint> list) {
		System.out.println("Printing list: ");
		for(ReadingPoint rp : list) {
			System.out.print(rp.getName() + ", " + Fsfibu2DateFormats.getEntryDateFormat().format(rp.getReadingDay().getTime()) + "; ");
		}
		System.out.println();
	}

}
