package fs.fibu2.test.model;

import java.io.File;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.Journal;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.StartValueModel;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the class StartValueModel and associates
 * @author Simon Hampe
 *
 */
public class StartValueTest {
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "../";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		Locale.setDefault(Locale.GERMANY);
		
		try {
			final Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
			JFrame frame = new JFrame();
			JTable table = new JTable();
				table.setModel(new StartValueModel(j));
			frame.setContentPane(new JScrollPane(table));
			frame.pack();
			
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}