package fs.fibu2.test.model;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TableModelEvent;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.data.Fsfibu2Constants;
import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.CategoryFilter;
import fs.fibu2.filter.DateFilter;
import fs.fibu2.filter.DefaultFilters;
import fs.fibu2.filter.EntryFilter;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.module.BilancialPane;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.BilancialTableModel;
import fs.fibu2.view.model.BilancialTreeModel;
import fs.fibu2.view.render.BilancialTree;
import fs.fibu2.view.render.BilancialTreeRenderer;
import fs.fibu2.view.render.MoneyCellRenderer;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the features of {@link BilancialTreeModel} and associated classes
 * 
 * @author Simon Hampe
 *
 */
public class BilancialTreeTest {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "../";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		Locale.setDefault(Locale.GERMANY);
		
		try {
			final Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
			j.addEntry(new Entry("bla",3,Currency.getInstance("EUR"),new GregorianCalendar(),Category.getRootCategory()//Category.getCategory(Category.getRootCategory(), "Fachschaft")
					,"bank_account",null,null));
			j.addEntry(new Entry("bla",34,Currency.getInstance("EUR"),new GregorianCalendar(),
					Category.getCategory(new Vector<String>(Arrays.asList("Fachschaft","Spiele","Mehrspiele"))),"bank_account",null,null));
			JFrame frame = new JFrame();
			
//			CategoryFilter cfilter = new CategoryFilter(Category.getCategory(Category.getRootCategory(), "Fachschaft"));
//			DateFilter dfilter = DefaultFilters.getYearFilter(2009);
//			StackFilter filter = new StackFilter(new Vector<EntryFilter>(Arrays.asList(dfilter)),(HashSet<EntryFilter>)null, (HashSet<EntryFilter>)null);
//			BilancialTreeModel model = new BilancialTreeModel(j,null,null);
//			BilancialTree tree = new BilancialTree(model);
//				
//			JTable table = new JTable();
//				table.setModel(new BilancialTableModel(tree));
//				tree.addTreeExpansionListener((BilancialTableModel)table.getModel());
//				tree.getModel().addTreeModelListener((BilancialTableModel)table.getModel());
//				table.setDefaultRenderer(Float.class, new MoneyCellRenderer(Fsfibu2Constants.defaultCurrency));
////			
//			frame.setLayout(new BorderLayout());
//			frame.add(tree,BorderLayout.WEST);
//			frame.add(new JScrollPane(table), BorderLayout.EAST);
			frame.add(new BilancialPane(j,null,null));
			frame.pack();
				
			frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
