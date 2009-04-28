package fs.fibu2.data.format;


/**
 * A class implementing this interface can validate if a string conforms to a certain format, such as a date format.
 * @author Simon Hampe
 *
 */
public interface FormatValidator {

	/**
	 * Checks whether s conforms to a certain format
	 * @throws IllegalArgumentException TODO
	 */
	public void validateFormat(String s) throws IllegalArgumentException;
	
}
