package fs.fibu2.test;

import java.util.Currency;
import java.util.GregorianCalendar;

import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.view.model.JournalTableModel;

public class PreferencesTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Entry e = new Entry("bla",2,Currency.getInstance("EUR"),new GregorianCalendar(),Category.getRootCategory(),"cash_box",null,"bla");
		Entry f = new Entry("blu2",2,Currency.getInstance("EUR"),new GregorianCalendar(),Category.getRootCategory(),"cash_box",null,"bla");
		System.out.println(new JournalTableModel.TableModelComparator().compare(e,f));
		
	}

}
