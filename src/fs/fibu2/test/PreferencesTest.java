package fs.fibu2.test;

import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.MediaName;

/**
 * Theoretically I wrote this class to test the {@link Preferences} class - but didn't really need it, so I'm using it for small one-time tests.
 * @author Simon Hampe
 *
 */
public class PreferencesTest {

	/**
	 * @param args
	 * @throws BackingStoreException 
	 */
	public static void main(String[] args) throws BackingStoreException {
		//PrinterJob.getPrinterJob().pageDialog(new PageFormat());
		HashPrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
			set.add(MediaName.ISO_A4_WHITE);
		PrinterJob.getPrinterJob().printDialog(set);
		
			
	}

}
