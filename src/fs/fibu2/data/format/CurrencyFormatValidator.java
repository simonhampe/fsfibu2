package fs.fibu2.data.format;

import java.util.Currency;

import fs.fibu2.lang.Fsfibu2StringTableMgr;

/**
 * Checks, whether a string is a valid currency ISO code. There is only one instance of this filter.
 * @author Simon Hampe
 *
 */
public class CurrencyFormatValidator implements FormatValidator {

	@Override
	public void validateFormat(String s) throws IllegalArgumentException {
		try {
			Currency.getInstance(s);
		}
		catch(Exception e) {
			throw new IllegalArgumentException(Fsfibu2StringTableMgr.getString("fs.fibu2.filter.CurrencyFilter.error"));
		}
	}
}
