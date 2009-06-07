package fs.fibu2.data.format;

import java.text.NumberFormat;
import java.util.Currency;

/**
 * Provides a creation method for a {@link NumberFormat}, which formats numbers to two fraction digits with a locale-specific separator and a 
 * given currency symbol.
 * @author Simon Hampe
 *
 */
public class DefaultCurrencyFormat {

	public static NumberFormat getFormat(Currency currency) {
		NumberFormat format = NumberFormat.getCurrencyInstance();
			format.setCurrency(currency);
			format.setMinimumFractionDigits(2);
			format.setMaximumFractionDigits(2);
		return format;
	}
	
}
