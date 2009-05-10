package fs.fibu2.test.filter;

import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;

import org.apache.log4j.BasicConfigurator;
import org.dom4j.Document;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.AccountFilter;
import fs.fibu2.data.model.Journal;
import fs.fibu2.filter.CategoryFilter;
import fs.fibu2.filter.ValueFilter;
import fs.fibu2.filter.StandardFilterComponent.Selection;
import fs.fibu2.resource.Fsfibu2DefaultReference;
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
			String basedir = "/home/talio/eclipse/workspace/";
			Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
			FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
			JFrame mainFrame = new JFrame();
			
			Document d = XMLToolbox.loadXMLFile(new File("examples/journal.xml"));
			//Journal j = new Journal(d.getRootElement());
			Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File("/home/talio/eclipse/workspace/fsfibu/KassenbuchAb2008.xml")));
			
			//NameFilter filter = new NameFilter(Selection.REGEX,"S.*",null);
			//ValueFilter filter = new ValueFilter(Selection.RANGE,0,0.3f,14.2f,null);
			//CategoryFilter filter = new CategoryFilter();
			AccountFilter filter = new AccountFilter();
			
			mainFrame.add(filter.getEditor(j));
			
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainFrame.pack();
			mainFrame.setVisible(true);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
