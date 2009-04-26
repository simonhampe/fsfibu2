package fs.fibu2.data.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * This format formats numbers just like any {@link NumberFormat}. The only difference is, that the empty string is 
 * interpreted as "infinity" and parsed as null.
 * @author Simon Hampe
 *
 */
public class NumberInfinityFormat extends NumberFormat {

	private NumberFormat format;
	
	/**
	 * Creates a number format formatting according to the standard NumberFormat
	 */
	public NumberInfinityFormat() {
		this(null);
	}
	
	/**
	 * Creates a NumberInfinityFormat
	 * @param format Any normal number string will be parsed by this format. If null, a new instance will be created
	 */
	public NumberInfinityFormat(NumberFormat format) {
		this.format = format == null? NumberFormat.getInstance() : format;
	}
	
	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo,
			FieldPosition pos) {
		return format.format(obj, toAppendTo, pos); 
	}

	@Override
	public StringBuffer format(double number, StringBuffer toAppendTo,
			FieldPosition pos) {
		return format.format(number, toAppendTo, pos);
	}

	@Override
	public StringBuffer format(long number, StringBuffer toAppendTo,
			FieldPosition pos) {
		return format.format(number, toAppendTo, pos);
	}

	@Override
	public Number parse(String source, ParsePosition parsePosition) {
		if(source != null && source.equals("")) return null;
		else return format.parse(source, parsePosition);
	}
}
