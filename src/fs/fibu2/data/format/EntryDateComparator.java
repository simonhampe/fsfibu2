package fs.fibu2.data.format;

import java.text.ParseException;
import java.util.Comparator;
import java.util.GregorianCalendar;

/**
 * Compares two {@link GregorianCalendar} objects - but only the day, month, year fields. All further information is ignored.
 * The null dates are regarded as infinity bounds. It returns 0 for strings that cannot be converted to a GregorianCalendar object via the
 * {@link Fsfibu2DateFormats#getEntryDateFormat()} format
 * @author Simon Hampe
 *
 */
public class EntryDateComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		GregorianCalendar d1 = new GregorianCalendar();
		GregorianCalendar d2 = new GregorianCalendar();
		try {
			d1.setTime(Fsfibu2DateFormats.getEntryDateFormat().parse(o1));
			d2.setTime(Fsfibu2DateFormats.getEntryDateFormat().parse(o2));
		}
		catch(ParseException e) { return 0;}
		
		return compare(d1, d2);
	}

	public int compare(GregorianCalendar d1, GregorianCalendar d2) {
		if(d1 == null && d2 == null) return 0;
		if(d1 == null || d2 == null) return -1;
		
		int thisyear = d1.get(GregorianCalendar.YEAR);
		int thatyear = d2.get(GregorianCalendar.YEAR);
		if(thisyear < thatyear) return -1;
		if(thisyear > thatyear) return 1;
		
		int thismonth = d1.get(GregorianCalendar.MONTH);
		int thatmonth = d2.get(GregorianCalendar.MONTH);
		if(thismonth < thatmonth) return -1;
		if(thismonth > thatmonth) return 1;
		
		int thisday = d1.get(GregorianCalendar.DAY_OF_MONTH);
		int thatday = d2.get(GregorianCalendar.DAY_OF_MONTH);
		if(thisday < thatday) return -1;
		if(thisday > thatday) return 1;
		return 0;
	}
	
}
