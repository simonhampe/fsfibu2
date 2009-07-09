package fs.fibu2.test.view;

import java.io.File;
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;

import fs.event.DataRetrievalListener;
import fs.fibu2.account.SlushFund;
import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.AccountLoader;
import fs.fibu2.data.model.Entry;
import fs.fibu2.data.model.Journal;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.render.EntryDialog;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the entry dialog
 * @author Simon Hampe
 *
 */
public class EntryDialogTest {

	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "../";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		Locale.setDefault(Locale.GERMANY);
		
		try {
			final Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
			AccountLoader.setAccount("slush_fund", new SlushFund());
			Entry e = new Vector<Entry>(j.getEntries()).get(0);
			EntryDialog diag = new EntryDialog(null,j,e);
			diag.addDataRetrievalListener(new DataRetrievalListener() {
				@Override
				public void dataReady(Object source, Object data) {
					if(data == null) System.out.println("null");
					else System.out.println(((Entry)data).toString());
				}
			});
			diag.setVisible(true);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
