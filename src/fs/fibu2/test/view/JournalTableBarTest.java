package fs.fibu2.test.view;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.Journal;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.JournalTableModel;
import fs.fibu2.view.render.JournalTable;
import fs.fibu2.view.render.JournalTableBar;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the class JournalTableBar
 * @author Simon Hampe
 *
 */
public class JournalTableBarTest {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "../";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		Locale.setDefault(Locale.GERMANY);
		
		try {
			final Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
			JournalTable t = new JournalTable(new JournalTableModel(j,null,true,true));
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());
			JournalTableBar bar = new JournalTableBar(t);
			bar.setFloatable(false);
			frame.add(bar, BorderLayout.NORTH);
			frame.add(new JScrollPane(t), BorderLayout.CENTER);
			frame.setSize(frame.getMaximumSize());
			frame.setVisible(true);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
