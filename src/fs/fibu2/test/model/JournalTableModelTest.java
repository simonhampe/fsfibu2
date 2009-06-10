package fs.fibu2.test.model;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.log4j.BasicConfigurator;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.EntrySeparator;
import fs.fibu2.data.model.Journal;
import fs.fibu2.examples.SlushFund;
import fs.fibu2.filter.DefaultFilters;
import fs.fibu2.filter.EntryFilter;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.JournalTableModel;
import fs.fibu2.view.render.JournalTableRenderer;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the class {@link JournalTableModel}
 * @author Simon Hampe
 *
 */
public class JournalTableModelTest {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "../";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		Locale.setDefault(Locale.GERMANY);
		
		try {
			Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
//			AccountLoader.setAccount("slush_fund", new SlushFund());
//			HashMap<String, String> falseMap = new HashMap<String, String>();
//			falseMap.put("invoice", "F 4");
//			Entry e = new Entry("Bla",23,Currency.getInstance("EUR"),new GregorianCalendar(),Category.getRootCategory(),"slush_fund",falseMap,null);
//			j.addEntry(e);
			
			EntryFilter filter = DefaultFilters.getYearFilter(2009);
			
			long time1 = System.currentTimeMillis();
			JournalTableModel model = new JournalTableModel(j,null,true,true,false);
			long time2 = System.currentTimeMillis();
			System.out.println("Time for model calculation: " + (time2-time1));
			System.out.println("Model size: " + model.getRowCount());
			
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			JTable table = new JTable();
				table.setModel(model);
				table.setDefaultRenderer(Object.class, new JournalTableRenderer(model,Currency.getInstance("EUR")));
				table.setIntercellSpacing(new Dimension(0,0));
				table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
			JScrollPane pane = new JScrollPane(table);
			frame.pack();
			frame.add(pane);
			frame.setSize(frame.getMaximumSize());
			time1 = System.currentTimeMillis();
			frame.setVisible(true);
			time2 = System.currentTimeMillis();
			System.out.println("Time for drawing: " + (time2-time1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static void printModel(JournalTableModel model) {
		for(int i = 0; i < model.getRowCount(); i++) {
			Object o = model.getValueAt(i, 0);
			if(o instanceof Entry) {
				System.out.println("Entry " + ((Entry)o).getName());
			}
			if(o instanceof EntrySeparator) {
				System.out.println("Separator " + ((EntrySeparator)o).getName());
			}
		}
	}

}
