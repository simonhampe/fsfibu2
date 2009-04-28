package fs.fibu2.data.format;

import java.text.Format;
import java.text.ParseException;

/**
 * A {@link GivenFormatValidator} validates a string against a given format
 * @author Simon Hampe
 *
 */
public class GivenFormatValidator implements FormatValidator {
	
	private Format format;
	
	/**
	 * Constructs a validator which checks against the given format. If format == null, 
	 * any string is valid.
	 */
	public GivenFormatValidator(Format format) {
		this.format = format;
	}
	
	@Override
	public void validateFormat(String s) throws IllegalArgumentException {
		if(format != null) try {
			format.parseObject(s);
		}
		catch(ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
