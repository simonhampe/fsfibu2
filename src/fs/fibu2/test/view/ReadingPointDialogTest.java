package fs.fibu2.test.view;

import java.io.File;
import java.util.Locale;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.Journal;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.render.ReadingPointDialog;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

public class ReadingPointDialogTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "../";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		Locale.setDefault(Locale.GERMANY);
		
		try {
			final Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
			ReadingPointDialog diag = ReadingPointDialog.getInstance(j);
			diag.setVisible(true);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
