package fs.fibu2.data.format;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import fs.fibu2.data.model.Entry;

/**
 * This class summarizes some static strings specifying date format patterns. There is a method for retrieving the associated {@link SimpleDateFormat} and
 * for parsing a {@link GregorianCalendar} object from a string for each format. 
 * @author Simon Hampe
 *
 */
public class Fsfibu2DateFormats {

	/**
	 * The date format for the date field of {@link Entry}.
	 */
	public final static String entryDateFormat = "dd.MM.yyyy";
	
	/**
	 * The date format for fsfibu1 reading point dates
	 */
	public final static String fsfibu1RPFormat = "yyyy-MM-dd'T'HH:mm:ss";

	/**
	 * The date format for fsibu1 entry dates
	 */
	public final static String fsfibu1EntryFormat = "yyyy-MM-dd";
	
	public static SimpleDateFormat getEntryDateFormat() {
		return new SimpleDateFormat(entryDateFormat);
	}
	
	public static GregorianCalendar parseEntryDateFormat(String source) throws ParseException {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(getEntryDateFormat().parse(source));
		return c;
	}
	
	public static SimpleDateFormat getFsfibu1RPFormat() {
		return new SimpleDateFormat(fsfibu1RPFormat);
	}
	
	public static GregorianCalendar parseFsfibu1RPFormat(String source) throws ParseException {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(getFsfibu1RPFormat().parse(source));
		return c;
	}
	
	public static SimpleDateFormat getFsfibu1EntryFormat() {
		return new SimpleDateFormat(fsfibu1EntryFormat);
	}
	
	public static GregorianCalendar parseFsfibu1EntryFormat(String source) throws ParseException {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(getFsfibu1EntryFormat().parse(source));
		return c;
	}
	
}
