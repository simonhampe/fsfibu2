package fs.fibu2.test.filter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Arrays;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.examples.SlushFund;
import fs.fibu2.filter.CategoryFilter;
import fs.fibu2.filter.EntryFilter;
import fs.fibu2.filter.EntryFilterEditor;
import fs.fibu2.filter.NameFilter;
import fs.fibu2.filter.StackFilter;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.gui.GUIToolbox;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the filter classes
 * @author Simon Hampe
 *
 */
public class FilterTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BasicConfigurator.configure();
			String basedir = "/home/hampe/workspace/";
			Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
			FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
			JFrame mainFrame = new JFrame();
			
			//Document d = XMLToolbox.loadXMLFile(new File("examples/journal.xml"));
			//Journal j = new Journal(d.getRootElement());
			Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
			//final Journal j = new Journal();
			AccountLoader.setAccount("slush_fund", new SlushFund());
			j.addEntry(new Entry("bla",0,Currency.getInstance(Locale.GERMANY),new GregorianCalendar(),Category.getCategory(new Vector<String>(Arrays.asList("Bla","Blu"))),"slush_fund",
					null,""));
			
			
			
			//NameFilter filter = new NameFilter(Selection.REGEX,"S.*",null);
			//ValueFilter filter = new ValueFilter(Selection.RANGE,0,0.3f,14.2f,null);
			//CategoryFilter filter = new CategoryFilter();
			//AccountFilter filter = new AccountFilter(AccountLoader.getAccount("cash_box"));
			//AccountInformationFilter filter = new AccountInformationFilter();
			//AdditionalInformationFilter filter = new AdditionalInformationFilter();
			//DateFilter filter = new DateFilter();
			//EntryFilter filter = FilterLoader.getFilter("ff2filter_account");
			Vector<EntryFilter> filterlist = new Vector<EntryFilter>();
				filterlist.add(new NameFilter());
				filterlist.add(new CategoryFilter(Category.getCategory(Category.getRootCategory(), "Bla")));
			StackFilter filter = new StackFilter(filterlist,null,null);
			
			EntryFilterEditor editor = filter.getEditor(j);
			JPanel fillPanel = new JPanel();
			GridBagLayout gbl = new GridBagLayout();
			mainFrame.setLayout(gbl);
			GridBagConstraints gbc = GUIToolbox.buildConstraints(1, 0, 1, 1);
			gbc.weighty = 100;
			GridBagConstraints gbc2 = GUIToolbox.buildConstraints(0, 0, 1, 1);
			gbc2.weightx = 100;
			gbl.setConstraints(fillPanel, gbc2);
			gbl.setConstraints(editor, gbc);
			mainFrame.add(fillPanel);
			mainFrame.add(editor);
			
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainFrame.pack();
			mainFrame.setSize(mainFrame.getMaximumSize());
			mainFrame.setVisible(true);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
