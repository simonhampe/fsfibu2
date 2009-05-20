package fs.fibu2.test.filter;

import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.data.model.BankAccount;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.AccountInformationFilter;
import fs.fibu2.filter.CategoryFilter;
import fs.fibu2.filter.EntryFilter;
import fs.fibu2.filter.NameFilter;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.AccountInformation;
import fs.xml.FsfwDefaultReference;

/**
 * Tests the serializing facilites of {@link EntryFilter} classes via {@link Preferences}.
 * @author Simon Hampe
 *
 */
public class FilterPreferencesTest {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "/home/hampe/workspace/";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		
		
		Preferences node = Preferences.userRoot();
		node.node("filters");
		
		//NameFilter filter = new NameFilter("a","z");
		AccountInformationFilter filter = new AccountInformationFilter(new AccountInformation("statement",new BankAccount().getFieldNames().get("statement"),null),"bla");
		//CategoryFilter filter = new CategoryFilter("z","a",3);
		filter.insertMyPreferences(node);
		
		EntryFilter filter2 = filter.createMeFromPreferences(node.node("filter"));
		
		JFrame mainFrame = new JFrame();
		
		mainFrame.add(filter2.getEditor(new Journal()));
		
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.pack();
		mainFrame.setVisible(true);
		
	}

}
