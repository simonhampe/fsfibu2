package fs.fibu2.test.model;

import java.io.File;
import java.util.Arrays;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.tree.DefaultDocument;

import fs.fibu2.data.model.Category;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.CategoryListModel;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the class journal
 * @author Simon Hampe
 *
 */
public class JournalTest {

	public static void main(String[] args) {
		try {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.TRACE);
			Fsfibu2DefaultReference.setFsfibuDirectory("/home/talio/eclipse/workspace/fsfibu2/");
			FsfwDefaultReference.setFsfwDirectory("/home/talio/eclipse/workspace/fsframework/");
			Document d = XMLToolbox.loadXMLFile(new File("examples/journal.xml"));
			Journal j = new Journal(d.getRootElement());
			DefaultDocument d2 = new DefaultDocument();
			d2.setRootElement(j.getConfiguration());
			System.out.println(XMLToolbox.getDocumentAsPrettyString(d2));
			CategoryListModel model = new CategoryListModel(j);
			j.addEntry(new Entry("Bla",140,Currency.getInstance(Locale.getDefault()),new GregorianCalendar(),Category.getCategory(new Vector<String>(Arrays.asList("Bla","blu"))),"cash_box",new HashMap<String, String>(),"bla"));
			System.out.println(model.getSize());
			System.out.println(model.getElementAt(0));
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
