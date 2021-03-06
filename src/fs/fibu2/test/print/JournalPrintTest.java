package fs.fibu2.test.print;

import java.io.File;
import java.util.Locale;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.data.format.Fsfibu1Converter;
import fs.fibu2.data.model.Journal;
import fs.fibu2.print.JournalPrintDialog;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.fibu2.view.model.JournalTableModel;
import fs.xml.FsfwDefaultReference;
import fs.xml.XMLToolbox;

/**
 * Tests the class JournalPageable
 * @author Simon Hampe
 *
 */
public class JournalPrintTest {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String basedir = "../";
		Fsfibu2DefaultReference.setFsfibuDirectory(basedir + "fsfibu2/");
		FsfwDefaultReference.setFsfwDirectory(basedir + "fsframework/");
		Locale.setDefault(Locale.GERMANY);
		
		try {
			final Journal j = Fsfibu1Converter.convertFsfibu1Journal(XMLToolbox.loadXMLFile(new File(basedir + "/fsfibu/KassenbuchAb2008.xml")));
			JournalTableModel model = new JournalTableModel(j,null,true,true);
			
//			PrinterJob job = PrinterJob.getPrinterJob();
//			HashPrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
//				set.add(OrientationRequested.LANDSCAPE);
//			job.printDialog(set);
//			PageFormat format = job.getPageFormat(set);	
//			
//			job.setPageable(new JournalPageable("Kassenbuch 10.4.08 - 11.4.09","Kassenwart: Sebastian Jung, Cornelia Rottner; Kassenprüfer: Stephan Oberfranz, Irgend Jemand",model,format,10,true	));
//			
//			job.print(set);
			JournalPrintDialog diag = new JournalPrintDialog(model);
			diag.setVisible(true);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
