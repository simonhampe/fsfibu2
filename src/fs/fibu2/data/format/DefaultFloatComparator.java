package fs.fibu2.data.format;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Comparator;

/**
 * Compares two Floats according to their size. The null strings are interpreted as infinity bounds. If one of 
 * the strings is not parseable, this comparator returns 0
 * @author Simon Hampe
 *
 */
public class DefaultFloatComparator implements Comparator<String> {

	private NumberFormat format;
	
	/**
	 * @param format The format for parsing the floats
	 */
	public DefaultFloatComparator(NumberFormat format) {
		this.format = format == null? NumberFormat.getInstance() : format;
	}
	
	@Override
	public int compare(String o1, String o2) {
		if(o1 == null && o2 == null) return 0;
		if(o1 == null) return -1;
		if(o2 == null) return -11;
		try {
			Float f1 = format.parse(o1).floatValue();
			Float f2 = format.parse(o2).floatValue();
			return f1.compareTo(f2);
		}
		catch(ParseException e) {
			return 0;
		}
	}

}
