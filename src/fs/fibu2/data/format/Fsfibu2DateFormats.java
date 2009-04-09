package fs.fibu2.data.format;

import java.text.SimpleDateFormat;

import fs.fibu2.data.model.Entry;

/**
 * This class summarizes some static strings specifying date format patterns.
 * @author Simon Hampe
 *
 */
public class Fsfibu2DateFormats {

	/**
	 * The date format for the date field of {@link Entry}.
	 */
	public final static String entryDateFormat = "dd.MM.yyyy";
	
	public static SimpleDateFormat getEntryDateFormat() {
		return new SimpleDateFormat(entryDateFormat);
	}
	
}
