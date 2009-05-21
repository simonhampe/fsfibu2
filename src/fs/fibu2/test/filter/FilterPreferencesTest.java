package fs.fibu2.test.filter;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.apache.log4j.BasicConfigurator;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.data.model.BankAccount;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.AccountFilter;
import fs.fibu2.filter.AccountInformationFilter;
import fs.fibu2.filter.AdditionalInformationFilter;
import fs.fibu2.filter.CategoryFilter;
import fs.fibu2.filter.DateFilter;
import fs.fibu2.filter.EntryFilter;
import fs.fibu2.filter.NameFilter;
import fs.fibu2.filter.ValueFilter;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.AccountInformation;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the serializing facilites of {@link EntryFilter} classes via {@link Preferences}.
 * @author Simon Hampe
 *
 */
public class FilterPreferencesTest {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "/home/talio/eclipse/workspace/";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		
		try {
			Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
		
		
			Preferences node = Preferences.userRoot();
			node.node("filters");
			
			//NameFilter filter = new NameFilter("a","z");
			//AccountInformationFilter filter = new AccountInformationFilter(new AccountInformation("statement",new BankAccount().getFieldNames().get("statement"),null),"bla");
			//CategoryFilter filter = new CategoryFilter("z","a",3);
			//AccountFilter filter = new AccountFilter("a","z");
			//AdditionalInformationFilter filter = new AdditionalInformationFilter("aah","bla");
			//DateFilter filter = new DateFilter(Selection.RANGE, new GregorianCalendar(), new GregorianCalendar(),new GregorianCalendar(),".*");
			ValueFilter filter = new ValueFilter(-13,123);
			
			filter.insertMyPreferences(node);
			
			EntryFilter filter2 = filter.createMeFromPreferences(node.node("filter"));
			
			JFrame mainFrame = new JFrame();
			
			mainFrame.add(filter2.getEditor(j));
			
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainFrame.pack();
			mainFrame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
