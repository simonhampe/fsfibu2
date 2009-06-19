package fs.fibu2.test.view;

import java.io.File;
import java.io.IOException;
import java.util.Currency;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.log4j.BasicConfigurator;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.Journal;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.AccountTableModel;
import fs.fibu2.view.model.JournalTableModel;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the {@link AccountTableModel} test.
 * @author Simon Hampe
 *
 */
public class AccountTableTest {


	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "../";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		Locale.setDefault(Locale.GERMANY);
		
		Journal j;
		try {
			j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
		
			JournalTableModel model = new JournalTableModel(j,null,true,true,false);
			
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			JTable table = new JTable();
				table.setModel(new AccountTableModel(model,null,null, Currency.getInstance("EUR")));
			
			JScrollPane pane = new JScrollPane(table);
			frame.add(pane);
			frame.pack();
			
			frame.setVisible(true);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
